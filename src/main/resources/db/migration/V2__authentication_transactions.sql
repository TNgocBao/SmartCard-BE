ALTER TABLE authentication_transaction ADD COLUMN transaction_token_hash VARCHAR(64);
ALTER TABLE authentication_transaction ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE authentication_transaction ADD COLUMN consumed_at VARCHAR(40);
CREATE UNIQUE INDEX uk_auth_transaction_token ON authentication_transaction(transaction_token_hash);
CREATE INDEX ix_auth_transaction_expiry ON authentication_transaction(expires_at);
