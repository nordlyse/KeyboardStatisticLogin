package com.nordlyse.wordfrequency.keystrike;

import java.util.Locale;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PgNgramTransitionStore implements NgramTransitionStore {

    private final JdbcClient jdbc;
    private final KeystrikeSettings settings;

    @Override
    @Transactional
    public Optional<NgramTransition> apply(NgramUpdate update) {
        Optional<Row> loaded = load(update.orderSize(), update.sequence(), true);
        StoredNgram current = loaded
                .map(row -> fromRow(update.orderSize(), update.sequence(), row))
                .orElse(null);
        StoredNgram next = NgramAccumulator.apply(current, update);
        if (next == null) {
            return Optional.empty();
        }
        if (loaded.isEmpty()) {
            insert(next);
        } else {
            updateRow(next);
        }
        log.debug("Stored order {} with seen {} and error {}",
                next.transition().orderSize(),
                next.transition().seenCount(),
                next.transition().errorCount());
        return Optional.of(next.transition());
    }

    @Override
    public Optional<NgramTransition> find(int orderSize, String sequence) {
        return load(orderSize, sequence, false)
                .map(row -> fromRow(orderSize, sequence, row).transition());
    }

    @Override
    public boolean hasSamples() {
        return Boolean.TRUE.equals(jdbc.sql("""
                SELECT EXISTS (
                    SELECT 1
                    FROM keystrike_ngram
                    WHERE profile_name = :profile
                      AND seen_count > 0
                )
                """)
                .param("profile", settings.profile())
                .query(Boolean.class)
                .single());
    }

    private Optional<Row> load(int orderSize, String sequence, boolean lock) {
        String lockClause = lock ? " FOR UPDATE" : "";
        return jdbc.sql("""
                SELECT seen_count, error_count, CAST(features AS text) AS features
                FROM keystrike_ngram
                WHERE profile_name = :profile
                  AND order_size = :orderSize
                  AND sequence = :sequence
                """ + lockClause)
                .param("profile", settings.profile())
                .param("orderSize", orderSize)
                .param("sequence", sequence)
                .query((rs, rowNumber) -> new Row(
                        rs.getLong("seen_count"),
                        rs.getLong("error_count"),
                        rs.getString("features")))
                .optional();
    }

    private void insert(StoredNgram stored) {
        NgramTransition transition = stored.transition();
        jdbc.sql("""
                INSERT INTO keystrike_ngram
                    (profile_name, order_size, sequence, seen_count, error_count, features)
                VALUES
                    (:profile, :orderSize, :sequence, :seenCount, :errorCount, CAST(CAST(:features AS text) AS vector))
                """)
                .param("profile", settings.profile())
                .param("orderSize", transition.orderSize())
                .param("sequence", transition.sequence())
                .param("seenCount", transition.seenCount())
                .param("errorCount", transition.errorCount())
                .param("features", literal(stored.features()))
                .update();
    }

    private void updateRow(StoredNgram stored) {
        NgramTransition transition = stored.transition();
        jdbc.sql("""
                UPDATE keystrike_ngram
                SET seen_count = :seenCount,
                    error_count = :errorCount,
                    features = CAST(CAST(:features AS text) AS vector)
                WHERE profile_name = :profile
                  AND order_size = :orderSize
                  AND sequence = :sequence
                """)
                .param("profile", settings.profile())
                .param("orderSize", transition.orderSize())
                .param("sequence", transition.sequence())
                .param("seenCount", transition.seenCount())
                .param("errorCount", transition.errorCount())
                .param("features", literal(stored.features()))
                .update();
    }

    private static StoredNgram fromRow(int orderSize, String sequence, Row row) {
        float[] features = parse(row.features());
        int steps = Math.max(0, orderSize - 1);
        double[] means = new double[steps];
        for (int index = 0; index < steps && index < NgramAccumulator.STEP_SLOTS; index++) {
            means[index] = features[NgramAccumulator.STEP_ORIGIN + index] * 1000.0;
        }
        double errorFrequency = row.seenCount() == 0 ? 0 : (double) row.errorCount() / row.seenCount();
        NgramTransition transition = new NgramTransition(
                orderSize, sequence, row.seenCount(), row.errorCount(), errorFrequency, means);
        return new StoredNgram(transition, features);
    }

    private static String literal(float[] features) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < features.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.8f", features[index]));
        }
        return builder.append(']').toString();
    }

    private static float[] parse(String text) {
        String raw = text == null ? "" : text.trim();
        if (raw.startsWith("[")) {
            raw = raw.substring(1);
        }
        if (raw.endsWith("]")) {
            raw = raw.substring(0, raw.length() - 1);
        }
        float[] features = new float[NgramAccumulator.DIMENSION];
        if (raw.isBlank()) {
            return features;
        }
        String[] parts = raw.split(",");
        if (parts.length != NgramAccumulator.DIMENSION) {
            throw new IllegalStateException("pgvector row has " + parts.length + " values");
        }
        for (int index = 0; index < parts.length; index++) {
            features[index] = Float.parseFloat(parts[index].trim());
        }
        return features;
    }

    private record Row(long seenCount, long errorCount, String features) {
    }
}
