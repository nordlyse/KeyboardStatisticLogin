package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KeystrikeSettingsTest {

    @Test
    void usesTenAndLocalWhenTheOrderAndProfileAreUnset() {
        KeystrikeSettings settings = new KeystrikeSettings(0, " ");

        assertThat(settings.maxOrder()).isEqualTo(10);
        assertThat(settings.profile()).isEqualTo("local");
    }

    @Test
    void keepsAChosenOrderAndProfile() {
        KeystrikeSettings settings = new KeystrikeSettings(4, "writer");

        assertThat(settings.maxOrder()).isEqualTo(4);
        assertThat(settings.profile()).isEqualTo("writer");
    }

    @Test
    void rejectsAnOrderOutsideOneToTen() {
        assertThatThrownBy(() -> new KeystrikeSettings(11, "local"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KeystrikeSettings(-1, "local"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
