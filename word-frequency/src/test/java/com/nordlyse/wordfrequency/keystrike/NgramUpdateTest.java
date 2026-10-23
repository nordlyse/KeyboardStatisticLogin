package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NgramUpdateTest {

    @Test
    void copiesSampleStepsAndBuildsAnErrorWithoutSteps() {
        double[] steps = {5};
        NgramUpdate sample = NgramUpdate.sample(2, "ab", steps, true);
        steps[0] = 9;

        assertThat(sample.sample()).isTrue();
        assertThat(sample.erroneous()).isTrue();
        assertThat(sample.stepMillis()).containsExactly(5);

        NgramUpdate error = NgramUpdate.error(3, "abc");
        assertThat(error.sample()).isFalse();
        assertThat(error.erroneous()).isTrue();
        assertThat(error.stepMillis()).isEmpty();
        assertThat(new NgramUpdate(1, "a", null, true, false).stepMillis()).isEmpty();
    }
}
