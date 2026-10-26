package com.nordlyse.wordfrequency.keystrike;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class MemoryNgramTransitionStore implements NgramTransitionStore {

    private final Map<String, StoredNgram> rows = new HashMap<>();

    @Override
    public Optional<NgramTransition> apply(NgramUpdate update) {
        String key = update.orderSize() + "\n" + update.sequence();
        StoredNgram next = NgramAccumulator.apply(rows.get(key), update);
        if (next == null) {
            return Optional.empty();
        }
        rows.put(key, next);
        return Optional.of(next.transition());
    }

    @Override
    public Optional<NgramTransition> find(int orderSize, String sequence) {
        StoredNgram stored = rows.get(orderSize + "\n" + sequence);
        return stored == null ? Optional.empty() : Optional.of(stored.transition());
    }

    @Override
    public boolean hasSamples() {
        return rows.values().stream().anyMatch(row -> row.transition().seenCount() > 0);
    }

    Optional<NgramTransition> current(int orderSize, String sequence) {
        return find(orderSize, sequence);
    }
}
