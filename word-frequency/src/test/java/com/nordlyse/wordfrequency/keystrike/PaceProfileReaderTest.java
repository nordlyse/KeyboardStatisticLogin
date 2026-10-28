package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PaceProfileReaderTest {

    private final MemoryNgramTransitionStore store = new MemoryNgramTransitionStore();
    private final KeystrikeAnalysis analysis = new KeystrikeAnalysis(
            store,
            new KeystrikeSettings(10, "local"),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    private final PaceProfileReader profiles = new PaceProfileReader(store);

    @Test
    void separatesLetterStepsFromWordGaps() {
        train("cat nap", 100);

        PaceProfile profile = profiles.read();

        assertThat(profile.letters()).extracting(PaceMark::from, PaceMark::to)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("c", "a"),
                        org.assertj.core.groups.Tuple.tuple("a", "t"),
                        org.assertj.core.groups.Tuple.tuple("n", "a"),
                        org.assertj.core.groups.Tuple.tuple("a", "p"));
        assertThat(profile.letters()).allSatisfy(mark -> assertThat(mark.meanMillis()).isCloseTo(100, offset(0.001)));
        assertThat(profile.words()).containsExactly(new PaceMark("t", "n", 1, 200, 0, List.of()));
    }

    @Test
    void namesTheWordsOnEitherSideOfTheLetterCrossing() {
        train("cat nap", 100);

        PaceMark gap = profiles.read(List.of("cat nap")).words().get(0);

        assertThat(gap.from()).isEqualTo("t");
        assertThat(gap.to()).isEqualTo("n");
        assertThat(gap.words()).containsExactly(new WordCrossing("cat", "nap"));
    }

    @Test
    void keepsTheTypedCapitalsOnTheWords() {
        train("Cat Nap.", 80);

        PaceMark gap = profiles.read(List.of("Cat Nap.")).words().get(0);

        assertThat(gap.from()).isEqualTo("t");
        assertThat(gap.to()).isEqualTo("n");
        assertThat(gap.words()).containsExactly(new WordCrossing("Cat", "Nap"));
    }

    @Test
    void listsEveryWordPairThatUsesTheSameLetterCrossing() {
        train("cat nap", 100);
        train("hot noon", 100);

        PaceMark gap = profiles.read(List.of("hot noon", "cat nap")).words().get(0);

        assertThat(gap.from()).isEqualTo("t");
        assertThat(gap.to()).isEqualTo("n");
        assertThat(gap.words()).containsExactly(
                new WordCrossing("cat", "nap"),
                new WordCrossing("hot", "noon"));
    }

    @Test
    void skipsAWordPairSplitByPunctuation() {
        train("cat nap", 100);

        assertThat(profiles.read(List.of("cat, nap")).words().get(0).words()).isEmpty();
    }

    @Test
    void averagesALetterStepTypedTwice() {
        train("ab", 100);
        train("ab", 300);

        PaceMark step = profiles.read().letters().get(0);

        assertThat(step.from()).isEqualTo("a");
        assertThat(step.to()).isEqualTo("b");
        assertThat(step.seenCount()).isEqualTo(2);
        assertThat(step.meanMillis()).isCloseTo(200, offset(0.001));
    }

    @Test
    void keepsAnotherAccountOutOfTheChart() {
        ActiveProfile.use("ada");
        try {
            train("ab", 100);
            ActiveProfile.use("bee");
            assertThat(profiles.read().letters()).isEmpty();
            assertThat(profiles.read().words()).isEmpty();
        } finally {
            ActiveProfile.clear();
        }
    }

    private void train(String text, long gap) {
        List<KeystrikeRequest> keys = new ArrayList<>();
        int index = 0;
        for (int offset = 0; offset < text.length();) {
            int next = text.offsetByCodePoints(offset, 1);
            keys.add(new KeystrikeRequest(text.substring(offset, next), Instant.ofEpochMilli(index * gap), false));
            offset = next;
            index++;
        }
        for (KeystrikeRequest key : keys) {
            analysis.addKeystrike(key);
        }
    }
}
