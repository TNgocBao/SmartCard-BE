ALTER TABLE webauthn_credential ADD COLUMN attested_credential_data TEXT;
ALTER TABLE webauthn_credential ADD COLUMN credential_id_hash VARCHAR(64);
CREATE UNIQUE INDEX uq_webauthn_credential_hash ON webauthn_credential(credential_id_hash);
CREATE INDEX ix_session_account_status ON idp_session(account_id,status);
CREATE INDEX ix_credential_account_type_status ON credential(account_id,credential_type,status);
CREATE INDEX ix_transaction_account_purpose ON authentication_transaction(account_id,required_acr,created_at);
