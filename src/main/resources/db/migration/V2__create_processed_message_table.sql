CREATE TABLE processed_message(
    event_id VARCHAR(36) PRIMARY KEY ,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);