ALTER TABLE signing_key ADD COLUMN active_slot INT;
CREATE UNIQUE INDEX uq_signing_key_active_slot ON signing_key(active_slot);
ALTER TABLE signing_key ADD CONSTRAINT ck_signing_key_active_slot CHECK (status IS NOT NULL AND ((status='ACTIVE' AND active_slot IS NOT NULL AND active_slot=1) OR (status<>'ACTIVE' AND active_slot IS NULL)));
