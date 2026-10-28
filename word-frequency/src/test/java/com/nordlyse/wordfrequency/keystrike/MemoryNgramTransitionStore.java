package com.nordlyse.wordfrequency.keystrike;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class MemoryNgramTransitionStore implements NgramTransitionStore {

    private final Map<String, StoredNgram> rows = new HashMap<>();

    @Override
    public Optional<NgramTransition> apply(NgramUpdate update) {
        String key = key(update.orderSize(), update.sequence());
        StoredNgram next = NgramAccumulator.apply(rows.get(key), update);
        if (next == null) {
            return Optional.empty();
        }
        rows.put(key, next);
        return Optional.of(next.transition());
    }

    @Override
    public Optional<NgramTransition> find(int orderSize, String sequence) {
        StoredNgram stored = rows.get(key(orderSize, sequence));
        return stored == null ? Optional.empty() : Optional.of(stored.transition());
    }

    @Override
    public List<NgramTransition> list(int orderSize) {
        String prefix = ActiveProfile.orElse("local") + "\n" + orderSize + "\n";
        return rows.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(entry -> entry.getValue().transition())
                .filter(row -> row.seenCount() > 0)
                .toList();
    }

    @Override
    public boolean hasSamples() {
        String prefix = ActiveProfile.orElse("local") + "\n";
        return rows.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .anyMatch(entry -> entry.getValue().transition().seenCount() > 0);
    }

    private static String key(int orderSize, String sequence) {
        return ActiveProfile.orElse("local") + "\n" + orderSize + "\n" + sequence;
    }

    Optional<NgramTransition> current(int orderSize, String sequence) {
        return find(orderSize, sequence);
    }
}
