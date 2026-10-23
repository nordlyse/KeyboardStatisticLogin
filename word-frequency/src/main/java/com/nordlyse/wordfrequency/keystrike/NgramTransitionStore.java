package com.nordlyse.wordfrequency.keystrike;

import java.util.Optional;

public interface NgramTransitionStore {

    Optional<NgramTransition> apply(NgramUpdate update);
}
