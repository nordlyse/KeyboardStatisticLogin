package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class KeystrikeRequestTest {

    @Test
    void holdsTheSymbolTimeAndOptionalCorrection() {
        KeystrikeRequest request = new KeystrikeRequest("a", Instant.EPOCH, null);

        assertThat(request.symbol()).isEqualTo("a");
        assertThat(request.pressedAt()).isEqualTo(Instant.EPOCH);
        assertThat(request.correction()).isNull();
    }
}
