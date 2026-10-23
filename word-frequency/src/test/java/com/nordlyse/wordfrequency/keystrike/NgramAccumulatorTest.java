package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NgramAccumulatorTest {

    @Test
    void returnsEmptyWhenTheSequenceWasNeverSeen() {
        assertThat(NgramAccumulator.apply(null, NgramUpdate.error(2, "ab"))).isNull();
    }

    @Test
    void packsASingleCharacterWithoutSteps() {
        StoredNgram stored = NgramAccumulator.apply(null, NgramUpdate.sample(1, "a", new double[0], false));

        assertThat(stored.transition().stepMillis()).isEmpty();
        assertThat(stored.transition().seenCount()).isEqualTo(1);
        assertThat(stored.features()).hasSize(NgramAccumulator.DIMENSION);
        assertThat(stored.features()[NgramAccumulator.ERROR_SLOT]).isEqualTo(0f);
    }
}
