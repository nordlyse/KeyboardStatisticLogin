package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NgramTransitionTest {

    @Test
    void copiesStepTimesAndTreatsAMissingArrayAsEmpty() {
        double[] steps = {10};
        NgramTransition transition = new NgramTransition(2, "ab", 1, 0, 0, steps);
        steps[0] = 99;

        assertThat(transition.stepMillis()).containsExactly(10);
        assertThat(new NgramTransition(1, "a", 1, 0, 0, null).stepMillis()).isEmpty();
    }
}
