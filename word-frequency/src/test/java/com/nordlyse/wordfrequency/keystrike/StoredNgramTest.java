package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StoredNgramTest {

    @Test
    void copiesFeaturesAndFillsAMissingVector() {
        NgramTransition transition = new NgramTransition(1, "a", 1, 0, 0, new double[0]);
        float[] features = {1};
        StoredNgram stored = new StoredNgram(transition, features);
        features[0] = 4;

        assertThat(stored.features()[0]).isEqualTo(1);
        assertThat(new StoredNgram(transition, null).features()).hasSize(NgramAccumulator.DIMENSION);
    }
}
