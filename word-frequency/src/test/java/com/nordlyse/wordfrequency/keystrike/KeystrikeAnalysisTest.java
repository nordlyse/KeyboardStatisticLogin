package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class KeystrikeAnalysisTest {

    private final MemoryNgramTransitionStore store = new MemoryNgramTransitionStore();
    private final KeystrikeAnalysis analysis = new KeystrikeAnalysis(
            store,
            new KeystrikeSettings(10, "local"),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    @Test
    void keepsEveryOrderFromOneCharacterThroughTen() {
        List<NgramTransition> atTenth = List.of();
        for (int index = 0; index < 10; index++) {
            atTenth = press(letter(index), index * 20L);
        }

        assertThat(atTenth).extracting(NgramTransition::orderSize).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(find(atTenth, 1).sequence()).isEqualTo("j");
        assertThat(find(atTenth, 2).sequence()).isEqualTo("ij");
        assertThat(find(atTenth, 10).sequence()).isEqualTo("abcdefghij");
        assertThat(find(atTenth, 10).stepMillis()).hasSize(9);
        assertThat(find(atTenth, 10).stepMillis()[0]).isEqualTo(20);

        List<NgramTransition> atEleventh = press("k", 200);
        assertThat(atEleventh).hasSize(10);
        assertThat(atEleventh).noneMatch(row -> row.orderSize() > 10);
        assertThat(find(atEleventh, 10).sequence()).isEqualTo("bcdefghijk");
        assertThat(find(atEleventh, 2).sequence()).isEqualTo("jk");
    }

    @Test
    void updatesLetterToLetterTimingAndErrorFrequency() {
        press("a", 0);
        press("b", 40);
        List<NgramTransition> typed = press("c", 100);

        assertThat(find(typed, 2).sequence()).isEqualTo("bc");
        assertThat(find(typed, 2).stepMillis()[0]).isEqualTo(60);
        assertThat(find(typed, 3).sequence()).isEqualTo("abc");
        assertThat(find(typed, 3).stepMillis()).containsExactly(40, 60);

        List<NgramTransition> corrected = press("Backspace", 130);
        assertThat(find(corrected, 1).sequence()).isEqualTo("c");
        assertThat(find(corrected, 1).errorFrequency()).isEqualTo(1);
        assertThat(find(corrected, 2).sequence()).isEqualTo("bc");
        assertThat(find(corrected, 2).errorCount()).isEqualTo(1);
        assertThat(find(corrected, 3).errorFrequency()).isEqualTo(1);
        assertThat(store.current(1, "a")).hasValueSatisfying(row -> assertThat(row.errorCount()).isZero());

        List<NgramTransition> again = press("c", 200);
        NgramTransition step = find(again, 2);
        assertThat(step.sequence()).isEqualTo("bc");
        assertThat(step.seenCount()).isEqualTo(2);
        assertThat(step.errorFrequency()).isEqualTo(0.5);
        assertThat(step.stepMillis()[0]).isCloseTo(110, offset(0.001));
    }

    @Test
    void leavesAnErroneousCharacterOutOfTheFollowingChain() {
        press("j", 0);
        press("x", 30, true);
        List<NgramTransition> next = press("k", 80);

        assertThat(find(next, 2).sequence()).isEqualTo("jk");
        assertThat(store.current(2, "jx")).hasValueSatisfying(row -> assertThat(row.errorFrequency()).isEqualTo(1));
        assertThat(store.current(2, "xk")).isEmpty();
    }

    @Test
    void foldsLetterCaseAndRejectsAMultiCharacterSymbol() {
        press("A", 0);
        List<NgramTransition> typed = press("B", 25);
        assertThat(find(typed, 2).sequence()).isEqualTo("ab");

        assertThatThrownBy(() -> press("ab", 50))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void recordsErrorFrequencyAndSeenScaleInTheVector() {
        StoredNgram first = NgramAccumulator.apply(null, NgramUpdate.sample(2, "ab", new double[] {100}, false));
        StoredNgram second = NgramAccumulator.apply(first, NgramUpdate.sample(2, "ab", new double[] {300}, false));
        StoredNgram corrected = NgramAccumulator.apply(second, NgramUpdate.error(2, "ab"));

        assertThat(corrected.transition().stepMillis()[0]).isCloseTo(200, offset(0.001));
        assertThat(corrected.transition().errorFrequency()).isEqualTo(0.5);
        assertThat(corrected.features()).hasSize(NgramAccumulator.DIMENSION);
        assertThat(corrected.features()[NgramAccumulator.ERROR_SLOT]).isEqualTo(0.5f);
        assertThat(corrected.features()[NgramAccumulator.SEEN_SLOT])
                .isCloseTo((float) (Math.log1p(2) / 10.0), offset(0.000001f));
        assertThat(corrected.features()[NgramAccumulator.STEP_ORIGIN]).isCloseTo(0.2f, offset(0.0001f));
    }

    private List<NgramTransition> press(String symbol, long millis) {
        return press(symbol, millis, false);
    }

    private List<NgramTransition> press(String symbol, long millis, boolean correction) {
        return analysis.addKeystrike(new KeystrikeRequest(symbol, Instant.ofEpochMilli(millis), correction));
    }

    private static String letter(int index) {
        return String.valueOf((char) ('a' + index));
    }

    private static NgramTransition find(List<NgramTransition> rows, int orderSize) {
        return rows.stream()
                .filter(row -> row.orderSize() == orderSize)
                .findFirst()
                .orElseThrow();
    }

    private static final class MemoryNgramTransitionStore implements NgramTransitionStore {

        private final Map<String, StoredNgram> rows = new HashMap<>();

        @Override
        public Optional<NgramTransition> apply(NgramUpdate update) {
            String key = update.orderSize() + "\n" + update.sequence();
            StoredNgram next = NgramAccumulator.apply(rows.get(key), update);
            if (next == null) {
                return Optional.empty();
            }
            rows.put(key, next);
            return Optional.of(next.transition());
        }

        private Optional<NgramTransition> current(int orderSize, String sequence) {
            StoredNgram stored = rows.get(orderSize + "\n" + sequence);
            return stored == null ? Optional.empty() : Optional.of(stored.transition());
        }
    }
}
