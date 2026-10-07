CREATE TABLE check_result_history (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(64) NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,
    latency_ms INT NOT NULL,
    status_code INT,
    up BOOLEAN NOT NULL
);

CREATE INDEX check_result_history_slug_observed_at_idx ON check_result_history (slug, observed_at DESC);