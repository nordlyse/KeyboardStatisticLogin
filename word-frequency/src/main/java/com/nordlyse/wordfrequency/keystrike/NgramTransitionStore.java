package com.nordlyse.wordfrequency.keystrike;

import java.util.Optional;

public interface NgramTransitionStore {

    Optional<NgramTransition> apply(NgramUpdate update);

    Optional<NgramTransition> find(int orderSize, String sequence);

    boolean hasSamples();
}
