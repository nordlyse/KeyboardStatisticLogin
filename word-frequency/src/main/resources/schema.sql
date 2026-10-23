CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS keystrike_ngram (
    profile_name varchar(64) NOT NULL,
    order_size smallint NOT NULL CHECK (order_size BETWEEN 1 AND 10),
    sequence text NOT NULL,
    seen_count bigint NOT NULL CHECK (seen_count > 0),
    error_count bigint NOT NULL CHECK (error_count >= 0),
    features vector(11) NOT NULL,
    PRIMARY KEY (profile_name, order_size, sequence)
);

CREATE INDEX IF NOT EXISTS keystrike_ngram_features_idx
    ON keystrike_ngram USING hnsw (features vector_cosine_ops);
