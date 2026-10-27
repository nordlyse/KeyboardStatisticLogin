package com.nordlyse.wordfrequency.account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void storesAHashThatStillMatchesThePassword() {
        String stored = hasher.hash("correct horse");

        assertThat(stored).startsWith("pbkdf2-sha256$1000$");
        assertThat(stored).doesNotContain("correct horse");
        assertThat(hasher.matches("correct horse", stored)).isTrue();
        assertThat(hasher.hash("correct horse")).isNotEqualTo(stored);
    }

    @Test
    void rejectsADifferentPasswordAndABrokenHash() {
        String stored = hasher.hash("correct horse");

        assertThat(hasher.matches("wrong horse", stored)).isFalse();
        assertThat(hasher.matches(null, stored)).isFalse();
        assertThat(hasher.matches("correct horse", null)).isFalse();
        assertThat(hasher.matches("correct horse", "not-a-hash")).isFalse();
    }
}
