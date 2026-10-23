package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.zaxxer.hikari.HikariDataSource;

class PgNgramTransitionStoreTest {

    private static HikariDataSource dataSource;
    private static PgNgramTransitionStore store;
    private static PgNgramTransitionStore otherProfile;

    @BeforeAll
    static void open() {
        dataSource = PgVectorDatabase.dataSource();
        JdbcClient jdbc = JdbcClient.create(dataSource);
        store = new PgNgramTransitionStore(jdbc, new KeystrikeSettings(10, "store-test"));
        otherProfile = new PgNgramTransitionStore(jdbc, new KeystrikeSettings(10, "other-profile"));
    }

    @AfterAll
    static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void storesASampleThenUpdatesTheMeanAndTheError() {
        store.apply(NgramUpdate.sample(2, "ab", new double[] {100}, false));

        NgramTransition updated = store.apply(NgramUpdate.sample(2, "ab", new double[] {300}, false)).orElseThrow();
        assertThat(updated.seenCount()).isEqualTo(2);
        assertThat(updated.stepMillis()[0]).isCloseTo(200, offset(0.1));

        NgramTransition corrected = store.apply(NgramUpdate.error(2, "ab")).orElseThrow();
        assertThat(corrected.errorCount()).isEqualTo(1);
        assertThat(corrected.errorFrequency()).isCloseTo(0.5, offset(0.001));
        assertThat(corrected.stepMillis()[0]).isCloseTo(200, offset(0.1));
    }

    @Test
    void returnsEmptyWhenTheErrorHasNoRowAndKeepsProfilesApart() {
        assertThat(store.apply(NgramUpdate.error(2, "never-seen"))).isEmpty();

        otherProfile.apply(NgramUpdate.sample(2, "zz", new double[] {40}, false));
        NgramTransition again = otherProfile.apply(NgramUpdate.sample(2, "zz", new double[] {40}, false)).orElseThrow();

        assertThat(again.seenCount()).isEqualTo(2);
        assertThat(store.apply(NgramUpdate.error(2, "zz"))).isEmpty();
    }

    @Test
    void keepsNineStepsForAnOrderOfTen() {
        double[] steps = {10, 20, 30, 40, 50, 60, 70, 80, 90};
        store.apply(NgramUpdate.sample(10, "abcdefghij", steps, false));

        NgramTransition loaded = store.apply(NgramUpdate.sample(10, "abcdefghij", steps, false)).orElseThrow();

        assertThat(loaded.stepMillis()).hasSize(9);
        assertThat(loaded.stepMillis()[0]).isCloseTo(10, offset(0.1));
        assertThat(loaded.stepMillis()[8]).isCloseTo(90, offset(0.1));
    }
}
