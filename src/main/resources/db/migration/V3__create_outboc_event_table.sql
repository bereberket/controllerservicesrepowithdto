CREATE TABLE outbox_event(
    event_id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100)NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    payload CLOB NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(1000)
);

CREATE INDEX idx_outbox_event_status_created_at
ON outbox_event(status,created_at);