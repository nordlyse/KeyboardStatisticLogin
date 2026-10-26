package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class KeystrikeMatcherTest {

    private final MemoryNgramTransitionStore store = new MemoryNgramTransitionStore();
    private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    private final KeystrikeAnalysis analysis = new KeystrikeAnalysis(
            store,
            new KeystrikeSettings(10, "local"),
            clock);
    private final KeystrikeMatcher matcher = new KeystrikeMatcher(store, clock);

    @Test
    void reportsThatNoProfileIsStored() {
        assertThat(matcher.profile().ready()).isFalse();
        assertThat(matcher.score(attempt("ab", 100)).ready()).isFalse();
        assertThat(matcher.score(attempt("ab", 100)).scored()).isFalse();
    }

    @Test
    void acceptsASentenceTypedAtTheStoredPace() {
        train("a cat", 100);

        MatchReport report = matcher.score(attempt("a cat", 100));

        assertThat(report.ready()).isTrue();
        assertThat(report.scored()).isTrue();
        assertThat(report.matchPercent()).isEqualTo(100.0);
        assertThat(report.accepted()).isTrue();
    }

    @Test
    void refusesASentenceTypedFarFromTheStoredPace() {
        train("cat", 100);

        MatchReport report = matcher.score(attempt("cat", 400));

        assertThat(report.scored()).isTrue();
        assertThat(report.accepted()).isFalse();
        assertThat(report.matchPercent()).isLessThan(KeystrikeMatcher.ACCEPT_ABOVE);
    }

    @Test
    void refusesASentenceWhoseStepsWereNeverSeen() {
        train("cat", 100);

        MatchReport report = matcher.score(attempt("zoo", 100));

        assertThat(report.ready()).isTrue();
        assertThat(report.accepted()).isFalse();
        assertThat(report.matchPercent()).isZero();
    }

    @Test
    void asksForASentenceWhenFewerThanTwoKeysRemain() {
        train("cat", 100);

        MatchReport report = matcher.score(new MatchAttempt(List.of(key("a", 0))));

        assertThat(report.ready()).isTrue();
        assertThat(report.scored()).isFalse();
    }

    private void train(String text, long gap) {
        List<KeystrikeRequest> keys = attempt(text, gap).keys();
        for (KeystrikeRequest key : keys) {
            analysis.addKeystrike(key);
        }
    }

    private static MatchAttempt attempt(String text, long gap) {
        List<KeystrikeRequest> keys = new ArrayList<>();
        int index = 0;
        for (int offset = 0; offset < text.length();) {
            int next = text.offsetByCodePoints(offset, 1);
            keys.add(key(text.substring(offset, next), index * gap));
            offset = next;
            index++;
        }
        return new MatchAttempt(keys);
    }

    private static KeystrikeRequest key(String symbol, long millis) {
        return new KeystrikeRequest(symbol, Instant.ofEpochMilli(millis), false);
    }
}
