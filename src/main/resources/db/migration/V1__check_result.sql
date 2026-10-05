CREATE TABLE check_result (
    slug VARCHAR(64) PRIMARY KEY,
    observed_at TIMESTAMPTZ NOT NULL,
    latency_ms INT NOT NULL,
    status_code INT,
    up BOOLEAN NOT NULL
);