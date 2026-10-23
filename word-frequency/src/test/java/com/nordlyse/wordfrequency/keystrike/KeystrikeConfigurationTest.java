package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class KeystrikeConfigurationTest {

    @Test
    void suppliesTheUtcClock() {
        assertThat(new KeystrikeConfiguration().keystrikeClock().getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
