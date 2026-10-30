package com.nordlyse.wordfrequency.keystrike;

import java.util.List;
import java.util.Optional;

public interface NgramTransitionStore {

    Optional<NgramTransition> apply(NgramUpdate update);

    Optional<NgramTransition> find(int orderSize, String sequence);

    List<NgramTransition> list(int orderSize);

    List<NgramTransition> list(String profile, int orderSize);

    boolean hasSamples();
}
