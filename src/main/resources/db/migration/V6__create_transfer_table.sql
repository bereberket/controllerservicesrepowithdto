CREATE TABLE transfer_record (
                                 transfer_id VARCHAR(36) PRIMARY KEY,
                                 request_id VARCHAR(36) NOT NULL UNIQUE,

                                 performed_by VARCHAR(255) NOT NULL,

                                 source_account_number VARCHAR(255) NOT NULL,
                                 target_account_number VARCHAR(255) NOT NULL,

                                 amount NUMERIC(19, 2) NOT NULL,
                                 source_balance_after NUMERIC(19, 2) NOT NULL,
                                 target_balance_after NUMERIC(19, 2) NOT NULL,

                                 status VARCHAR(20) NOT NULL,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                 CONSTRAINT chk_transfer_positive_amount
                                     CHECK (amount > 0),

                                 CONSTRAINT chk_transfer_source_balance
                                     CHECK (source_balance_after >= 0),

                                 CONSTRAINT chk_transfer_different_accounts
                                     CHECK (source_account_number <> target_account_number)
);

CREATE INDEX idx_transfer_source_account
    ON transfer_record(source_account_number);

CREATE INDEX idx_transfer_target_account
    ON transfer_record(target_account_number);

CREATE INDEX idx_transfer_created_at
    ON transfer_record(created_at);
