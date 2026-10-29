package com.nordlyse.wordfrequency.keystrike;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeystrikeMatcher {

    static final double ACCEPT_ABOVE = 95.0;

    private final NgramTransitionStore store;
    private final Clock clock;

    public MatchReport profile() {
        return report(store.hasSamples(), false, 0, false, false, List.of());
    }

    public MatchReport score(MatchAttempt attempt) {
        if (!store.hasSamples()) {
            return report(false, false, 0, false, false, List.of());
        }
        List<KeystrikeRequest> keys = attempt == null || attempt.keys() == null ? List.of() : attempt.keys();
        List<Tick> kept = keptKeys(keys);
        String sentence = attempt == null ? null : attempt.sentence();
        if (sentence != null && !sentence.isBlank() && !sameSentence(kept, sentence)) {
            return report(true, false, 0, false, false, List.of());
        }
        if (kept.size() < 2) {
            return report(true, false, 0, false, true, List.of());
        }
        double total = 0;
        int compared = 0;
        List<PaceCompare> steps = new ArrayList<>();
        for (int end = 1; end < kept.size(); end++) {
            Optional<PaceCompare> pair = pairCompare(kept, end);
            if (pair.isPresent()) {
                steps.add(pair.get());
                total += closeness(pair.get().attemptMillis(), pair.get().meanMillis());
                compared++;
            }
            int longest = Math.min(NgramAccumulator.MAX_ORDER, end + 1);
            for (int order = 3; order <= longest; order++) {
                Optional<NgramTransition> stored = store.find(order, sequence(kept, end, order));
                if (stored.isEmpty() || stored.get().seenCount() == 0) {
                    continue;
                }
                total += timingScore(steps(kept, end, order), stored.get().stepMillis());
                compared++;
            }
        }
        if (compared == 0) {
            log.info("Scored a sentence at {} percent", 0.0);
            return report(true, true, 0, false, true, List.of());
        }
        double shown = Math.round((100.0 * total / compared) * 10.0) / 10.0;
        log.info("Scored a sentence at {} percent", shown);
        return report(true, true, shown, shown > ACCEPT_ABOVE, true, steps);
    }

    private static MatchReport report(
            boolean ready,
            boolean scored,
            double matchPercent,
            boolean accepted,
            boolean aligned,
            List<PaceCompare> steps) {
        return new MatchReport(ready, scored, matchPercent, accepted, aligned, steps);
    }

    private static boolean sameSentence(List<Tick> kept, String sentence) {
        return fold(typed(kept)).equals(fold(sentence));
    }

    private static String typed(List<Tick> kept) {
        StringBuilder builder = new StringBuilder();
        for (Tick tick : kept) {
            builder.append(tick.symbol());
        }
        return builder.toString();
    }

    private static String fold(String text) {
        StringBuilder builder = new StringBuilder();
        text.codePoints().forEach(codePoint -> builder.appendCodePoint(Character.toLowerCase(codePoint)));
        return builder.toString().trim().replaceAll("\\s+", " ");
    }

    private List<Tick> keptKeys(List<KeystrikeRequest> keys) {
        List<Tick> kept = new ArrayList<>();
        for (KeystrikeRequest key : keys) {
            KeyParser.Parsed parsed = KeyParser.parse(key, clock);
            if (parsed.undo()) {
                if (!kept.isEmpty()) {
                    kept.remove(kept.size() - 1);
                }
                continue;
            }
            if (!parsed.erroneous()) {
                kept.add(new Tick(parsed.symbol(), parsed.pressedAt().toEpochMilli()));
            }
        }
        return kept;
    }

    private Optional<PaceCompare> pairCompare(List<Tick> kept, int end) {
        Optional<NgramTransition> stored = store.find(2, sequence(kept, end, 2));
        if (stored.isEmpty() || stored.get().seenCount() == 0 || stored.get().stepMillis().length == 0) {
            return Optional.empty();
        }
        return Optional.of(new PaceCompare(
                kept.get(end - 1).symbol(),
                kept.get(end).symbol(),
                steps(kept, end, 2)[0],
                stored.get().stepMillis()[0]));
    }

    private static double timingScore(double[] samples, double[] means) {
        int length = Math.min(samples.length, means.length);
        if (length == 0) {
            return 0;
        }
        double total = 0;
        for (int index = 0; index < length; index++) {
            total += closeness(samples[index], means[index]);
        }
        return total / length;
    }

    static double closeness(double sampleMillis, double meanMillis) {
        double mean = Math.max(meanMillis, 1);
        double tolerance = Math.max(40.0, mean * 0.35);
        double delta = Math.abs(sampleMillis - mean);
        if (delta <= tolerance) {
            return 1;
        }
        return Math.max(0, 1 - (delta - tolerance) / tolerance);
    }

    private static String sequence(List<Tick> kept, int end, int order) {
        int start = end - order + 1;
        StringBuilder builder = new StringBuilder(order);
        for (int index = start; index <= end; index++) {
            builder.append(kept.get(index).symbol());
        }
        return builder.toString();
    }

    private static double[] steps(List<Tick> kept, int end, int order) {
        int start = end - order + 1;
        double[] gaps = new double[order - 1];
        for (int index = 0; index < gaps.length; index++) {
            double gap = kept.get(start + index + 1).pressedAtMillis() - kept.get(start + index).pressedAtMillis();
            gaps[index] = Math.max(0, gap);
        }
        return gaps;
    }

    private record Tick(String symbol, long pressedAtMillis) {
    }
}
