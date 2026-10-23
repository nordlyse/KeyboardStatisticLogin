package com.nordlyse.wordfrequency.keystrike;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeystrikeAnalysis {

    private final NgramTransitionStore store;
    private final KeystrikeSettings settings;
    private final Clock clock;
    private final ArrayDeque<KeyTick> ticks = new ArrayDeque<>();

    @Transactional
    public synchronized List<NgramTransition> addKeystrike(KeystrikeRequest request) {
        ParsedKey key = parse(request);
        ArrayDeque<KeyTick> next = new ArrayDeque<>(ticks);
        List<NgramUpdate> updates = plan(next, key);
        List<NgramTransition> saved = new ArrayList<>();
        for (NgramUpdate update : updates) {
            store.apply(update).ifPresent(saved::add);
        }
        ticks.clear();
        ticks.addAll(next);
        log.info("Recorded a keystrike across {} n-gram orders", saved.size());
        return saved;
    }

    private List<NgramUpdate> plan(ArrayDeque<KeyTick> next, ParsedKey key) {
        if (key.undo()) {
            return undo(next);
        }
        if (key.erroneous()) {
            ArrayDeque<KeyTick> withError = new ArrayDeque<>(next);
            withError.addLast(new KeyTick(key.symbol(), key.pressedAt().toEpochMilli()));
            trim(withError);
            return samples(withError, true);
        }
        next.addLast(new KeyTick(key.symbol(), key.pressedAt().toEpochMilli()));
        trim(next);
        return samples(next, false);
    }

    private void trim(ArrayDeque<KeyTick> ticks) {
        while (ticks.size() > settings.maxOrder()) {
            ticks.removeFirst();
        }
    }

    private List<NgramUpdate> undo(ArrayDeque<KeyTick> next) {
        if (next.isEmpty()) {
            return List.of();
        }
        List<NgramUpdate> updates = new ArrayList<>();
        int limit = Math.min(settings.maxOrder(), next.size());
        for (int order = 1; order <= limit; order++) {
            updates.add(NgramUpdate.error(order, sequence(last(next, order))));
        }
        next.removeLast();
        return updates;
    }

    private List<NgramUpdate> samples(ArrayDeque<KeyTick> next, boolean erroneous) {
        List<NgramUpdate> updates = new ArrayList<>();
        int limit = Math.min(settings.maxOrder(), next.size());
        for (int order = 1; order <= limit; order++) {
            List<KeyTick> window = last(next, order);
            updates.add(NgramUpdate.sample(order, sequence(window), steps(window), erroneous));
        }
        return updates;
    }

    private static List<KeyTick> last(ArrayDeque<KeyTick> ticks, int order) {
        KeyTick[] all = ticks.toArray(KeyTick[]::new);
        return List.of(all).subList(all.length - order, all.length);
    }

    private static String sequence(List<KeyTick> window) {
        StringBuilder builder = new StringBuilder(window.size());
        for (KeyTick tick : window) {
            builder.append(tick.symbol());
        }
        return builder.toString();
    }

    private static double[] steps(List<KeyTick> window) {
        double[] steps = new double[Math.max(0, window.size() - 1)];
        for (int index = 1; index < window.size(); index++) {
            double gap = window.get(index).pressedAtMillis() - window.get(index - 1).pressedAtMillis();
            steps[index - 1] = Math.max(0, gap);
        }
        return steps;
    }

    private ParsedKey parse(KeystrikeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A keystrike is required");
        }
        String raw = request.symbol() == null ? "" : request.symbol().trim();
        if (raw.isEmpty() || isCorrectionKey(raw)) {
            return new ParsedKey("", instant(request), true, false);
        }
        if (raw.codePointCount(0, raw.length()) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A keystrike symbol is a single character");
        }
        int codePoint = raw.codePointAt(0);
        String symbol = new String(Character.toChars(Character.toLowerCase(codePoint)));
        Instant pressedAt = instant(request);
        return new ParsedKey(symbol, pressedAt, false, Boolean.TRUE.equals(request.correction()));
    }

    private Instant instant(KeystrikeRequest request) {
        return request.pressedAt() == null ? clock.instant() : request.pressedAt();
    }

    private static boolean isCorrectionKey(String raw) {
        String name = raw.toLowerCase(Locale.ROOT);
        return name.equals("backspace") || name.equals("delete");
    }

    private record ParsedKey(String symbol, Instant pressedAt, boolean undo, boolean erroneous) {
    }

    private record KeyTick(String symbol, long pressedAtMillis) {
    }
}
