-- UUID identifiers. Instants are ISO-8601 UTC text; use UTC lexicographic ordering.

CREATE TABLE organization (
  id VARCHAR(36) PRIMARY KEY,
  parent_id VARCHAR(36),
  org_code VARCHAR(255),
  org_name VARCHAR(255),
  org_type VARCHAR(255),
  description TEXT,
  status VARCHAR(255),
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (org_code)
);

CREATE TABLE person (
  id VARCHAR(36) PRIMARY KEY,
  person_code VARCHAR(255),
  display_name VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(255),
  date_of_birth DATE,
  status VARCHAR(255) NOT NULL,
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (person_code)
);

CREATE TABLE identity (
  id VARCHAR(36) PRIMARY KEY,
  person_id VARCHAR(36) NOT NULL,
  subject_id VARCHAR(255) NOT NULL,
  identifier VARCHAR(255),
  identity_type VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (subject_id),
  UNIQUE (identity_type, identifier)
);

CREATE TABLE org_membership (
  id VARCHAR(36) PRIMARY KEY,
  identity_id VARCHAR(36),
  organization_id VARCHAR(36),
  membership_type VARCHAR(255),
  status VARCHAR(255),
  joined_at DATE,
  ended_at DATE,
  created_at VARCHAR(40)
);

CREATE TABLE account (
  id VARCHAR(36) PRIMARY KEY,
  identity_id VARCHAR(36) NOT NULL,
  username VARCHAR(255) NOT NULL,
  account_type VARCHAR(255),
  status VARCHAR(255) NOT NULL,
  is_locked BOOLEAN NOT NULL,
  failed_login_attempts INT,
  locked_until VARCHAR(40),
  lock_reason VARCHAR(255),
  webauthn_user_handle TEXT,
  last_login_at VARCHAR(40),
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (username)
);

CREATE TABLE external_identity (
  id VARCHAR(36) PRIMARY KEY,
  identity_id VARCHAR(36),
  issuer VARCHAR(255),
  external_subject VARCHAR(255),
  provider VARCHAR(255),
  linked_at VARCHAR(40),
  UNIQUE (issuer, external_subject)
);

CREATE TABLE credential (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36) NOT NULL,
  credential_type VARCHAR(255) NOT NULL,
  credential_name VARCHAR(255),
  credential_identifier VARCHAR(255),
  secret_hash TEXT,
  secret_reference TEXT,
  certificate_fingerprint VARCHAR(255),
  status VARCHAR(255) NOT NULL,
  registered_at VARCHAR(40),
  last_used_at VARCHAR(40),
  expires_at VARCHAR(40),
  revoked_at VARCHAR(40)
);

CREATE TABLE webauthn_credential (
  id VARCHAR(36) PRIMARY KEY,
  credential_id VARCHAR(36),
  webauthn_credential_id TEXT,
  user_handle TEXT,
  rp_id VARCHAR(255),
  public_key_cose TEXT,
  cose_algorithm INT,
  sign_count BIGINT,
  aaguid VARCHAR(255),
  transports TEXT,
  backup_eligible BOOLEAN,
  backup_state BOOLEAN,
  attestation_format VARCHAR(255),
  UNIQUE (credential_id)
);

CREATE TABLE webauthn_challenge (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36),
  challenge VARCHAR(255),
  ceremony_type VARCHAR(255),
  rp_id VARCHAR(255),
  user_verification VARCHAR(255),
  expires_at VARCHAR(40),
  consumed_at VARCHAR(40),
  created_at VARCHAR(40),
  UNIQUE (challenge)
);

CREATE TABLE totp_credential (
  id VARCHAR(36) PRIMARY KEY,
  credential_id VARCHAR(36),
  encrypted_secret TEXT,
  encryption_key_id VARCHAR(255),
  algorithm VARCHAR(255),
  digits INT,
  period_seconds INT,
  verified_at VARCHAR(40),
  last_accepted_step BIGINT,
  UNIQUE (credential_id)
);

CREATE TABLE recovery_code (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36),
  code_hash VARCHAR(255),
  created_at VARCHAR(40),
  used_at VARCHAR(40),
  revoked_at VARCHAR(40),
  UNIQUE (code_hash)
);

CREATE TABLE account_activation_token (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36),
  token_hash VARCHAR(255),
  purpose VARCHAR(255),
  expires_at VARCHAR(40),
  consumed_at VARCHAR(40),
  created_at VARCHAR(40),
  UNIQUE (token_hash)
);

CREATE TABLE org_role (
  id VARCHAR(36) PRIMARY KEY,
  role_code VARCHAR(255),
  role_name VARCHAR(255),
  description TEXT,
  is_active BOOLEAN,
  UNIQUE (role_code)
);

CREATE TABLE org_role_assignment (
  id VARCHAR(36) PRIMARY KEY,
  membership_id VARCHAR(36),
  identity_id VARCHAR(36),
  organization_id VARCHAR(36),
  org_role_id VARCHAR(36),
  assigned_by VARCHAR(36),
  valid_from VARCHAR(40),
  valid_until VARCHAR(40),
  status VARCHAR(255)
);

CREATE TABLE role (
  id VARCHAR(36) PRIMARY KEY,
  role_code VARCHAR(255) NOT NULL,
  role_name VARCHAR(255),
  description TEXT,
  is_system BOOLEAN,
  is_active BOOLEAN NOT NULL,
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (role_code)
);

CREATE TABLE permission (
  id VARCHAR(36) PRIMARY KEY,
  permission_code VARCHAR(255) NOT NULL,
  resource VARCHAR(255),
  action VARCHAR(255),
  description TEXT,
  is_active BOOLEAN,
  UNIQUE (permission_code)
);

CREATE TABLE role_permission (
  id VARCHAR(36) PRIMARY KEY,
  role_id VARCHAR(36) NOT NULL,
  permission_id VARCHAR(36) NOT NULL,
  created_at VARCHAR(40),
  UNIQUE (role_id, permission_id)
);

CREATE TABLE account_role_assignment (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36) NOT NULL,
  role_id VARCHAR(36) NOT NULL,
  organization_id VARCHAR(36),
  application_id VARCHAR(36),
  assigned_by VARCHAR(36),
  valid_from VARCHAR(40),
  valid_until VARCHAR(40),
  status VARCHAR(255) NOT NULL,
  created_at VARCHAR(40),
  CHECK (organization_id IS NULL OR application_id IS NULL)
);

CREATE TABLE application (
  id VARCHAR(36) PRIMARY KEY,
  organization_id VARCHAR(36),
  client_id VARCHAR(255),
  client_name VARCHAR(255),
  client_type VARCHAR(255),
  protocol VARCHAR(255),
  client_auth_method VARCHAR(255),
  require_pkce BOOLEAN,
  status VARCHAR(255),
  created_at VARCHAR(40),
  updated_at VARCHAR(40),
  UNIQUE (client_id)
);

CREATE TABLE client_redirect_uri (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  redirect_uri TEXT,
  is_active BOOLEAN,
  created_at VARCHAR(40)
);

CREATE TABLE client_logout_uri (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  post_logout_redirect_uri TEXT,
  is_active BOOLEAN
);

CREATE TABLE client_credential (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  credential_type VARCHAR(255),
  credential_name VARCHAR(255),
  secret_hash TEXT,
  secret_reference TEXT,
  public_key TEXT,
  certificate_fingerprint VARCHAR(255),
  expires_at VARCHAR(40),
  revoked_at VARCHAR(40),
  status VARCHAR(255),
  created_at VARCHAR(40)
);

CREATE TABLE oidc_scope (
  id VARCHAR(36) PRIMARY KEY,
  scope_name VARCHAR(255),
  description TEXT,
  is_standard BOOLEAN,
  is_active BOOLEAN,
  UNIQUE (scope_name)
);

CREATE TABLE application_scope (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  scope_id VARCHAR(36),
  is_default BOOLEAN,
  is_required BOOLEAN,
  status VARCHAR(255),
  UNIQUE (application_id, scope_id)
);

CREATE TABLE client_grant_type (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  grant_type VARCHAR(255),
  is_enabled BOOLEAN,
  UNIQUE (application_id, grant_type)
);

CREATE TABLE signing_key (
  id VARCHAR(36) PRIMARY KEY,
  kid VARCHAR(255),
  algorithm VARCHAR(255),
  public_jwk TEXT,
  private_key_ref TEXT,
  status VARCHAR(255),
  not_before VARCHAR(40),
  expires_at VARCHAR(40),
  created_at VARCHAR(40),
  UNIQUE (kid)
);

CREATE TABLE idp_session (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36) NOT NULL,
  session_id_hash VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  authentication_level VARCHAR(255),
  amr TEXT,
  ip_address VARCHAR(255),
  user_agent TEXT,
  authenticated_at VARCHAR(40),
  last_activity_at VARCHAR(40),
  expires_at VARCHAR(40) NOT NULL,
  revoked_at VARCHAR(40),
  UNIQUE (session_id_hash)
);

CREATE TABLE session_client (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36),
  application_id VARCHAR(36),
  joined_at VARCHAR(40),
  logged_out_at VARCHAR(40),
  UNIQUE (session_id, application_id)
);

CREATE TABLE authentication_event (
  id VARCHAR(36) PRIMARY KEY,
  identity_id VARCHAR(36),
  account_id VARCHAR(36),
  application_id VARCHAR(36),
  credential_id VARCHAR(36),
  authentication_method VARCHAR(255),
  event_type VARCHAR(255),
  result VARCHAR(255),
  failure_reason_code VARCHAR(255),
  ip_address VARCHAR(255),
  user_agent TEXT,
  created_at VARCHAR(40)
);

CREATE TABLE authorization_code (
  id VARCHAR(36) PRIMARY KEY,
  application_id VARCHAR(36),
  account_id VARCHAR(36),
  session_id VARCHAR(36),
  code_hash VARCHAR(255),
  redirect_uri TEXT,
  nonce VARCHAR(255),
  code_challenge VARCHAR(255),
  code_challenge_method VARCHAR(255),
  expires_at VARCHAR(40),
  consumed_at VARCHAR(40),
  created_at VARCHAR(40),
  UNIQUE (code_hash)
);

CREATE TABLE authorization_code_scope (
  id VARCHAR(36) PRIMARY KEY,
  authorization_code_id VARCHAR(36),
  scope_id VARCHAR(36),
  UNIQUE (authorization_code_id, scope_id)
);

CREATE TABLE refresh_token (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36),
  application_id VARCHAR(36),
  session_id VARCHAR(36),
  token_hash VARCHAR(255),
  family_id VARCHAR(255),
  replaced_by_token_id VARCHAR(36),
  status VARCHAR(255),
  issued_at VARCHAR(40),
  expires_at VARCHAR(40),
  consumed_at VARCHAR(40),
  revoked_at VARCHAR(40),
  UNIQUE (token_hash)
);

CREATE TABLE refresh_token_scope (
  id VARCHAR(36) PRIMARY KEY,
  refresh_token_id VARCHAR(36),
  scope_id VARCHAR(36),
  UNIQUE (refresh_token_id, scope_id)
);

CREATE TABLE user_consent (
  id VARCHAR(36) PRIMARY KEY,
  identity_id VARCHAR(36),
  account_id VARCHAR(36),
  application_id VARCHAR(36),
  status VARCHAR(255),
  granted_at VARCHAR(40),
  revoked_at VARCHAR(40),
  UNIQUE (account_id, application_id)
);

CREATE TABLE user_consent_scope (
  id VARCHAR(36) PRIMARY KEY,
  user_consent_id VARCHAR(36),
  scope_id VARCHAR(36),
  granted_at VARCHAR(40),
  UNIQUE (user_consent_id, scope_id)
);

CREATE TABLE security_policy (
  id VARCHAR(36) PRIMARY KEY,
  organization_id VARCHAR(36),
  application_id VARCHAR(36),
  policy_type VARCHAR(255),
  policy_name VARCHAR(255),
  policy_config TEXT,
  priority INT,
  status VARCHAR(255),
  updated_by VARCHAR(36),
  updated_at VARCHAR(40),
  CHECK (organization_id IS NULL OR application_id IS NULL)
);

CREATE TABLE audit_log (
  id VARCHAR(36) PRIMARY KEY,
  actor_account_id VARCHAR(36),
  event_type VARCHAR(255),
  action VARCHAR(255),
  resource_type VARCHAR(255),
  resource_id VARCHAR(255),
  result VARCHAR(255),
  ip_address VARCHAR(255),
  correlation_id VARCHAR(255),
  metadata TEXT,
  created_at VARCHAR(40)
);

CREATE TABLE smart_card_credential (
  id VARCHAR(36) PRIMARY KEY,
  credential_id VARCHAR(36) NOT NULL,
  card_identifier VARCHAR(255) NOT NULL,
  public_key_spki TEXT NOT NULL,
  algorithm VARCHAR(255) NOT NULL,
  transport VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at VARCHAR(40),
  UNIQUE (card_identifier),
  UNIQUE (credential_id)
);

CREATE TABLE smart_card_challenge (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36) NOT NULL,
  credential_id VARCHAR(36) NOT NULL,
  session_id VARCHAR(36),
  challenge_hash VARCHAR(255) NOT NULL,
  challenge_payload TEXT NOT NULL,
  expires_at VARCHAR(40) NOT NULL,
  consumed_at VARCHAR(40),
  created_at VARCHAR(40)
);

CREATE TABLE authentication_transaction (
  id VARCHAR(36) PRIMARY KEY,
  account_id VARCHAR(36),
  required_acr VARCHAR(255),
  completed_amr TEXT,
  oidc_context_json TEXT,
  expires_at VARCHAR(40),
  created_at VARCHAR(40)
);

CREATE TABLE outbox_event (
  id VARCHAR(36) PRIMARY KEY,
  event_type VARCHAR(255),
  payload TEXT,
  status VARCHAR(255),
  attempts INT,
  next_attempt_at VARCHAR(40),
  created_at VARCHAR(40)
);

ALTER TABLE organization ADD CONSTRAINT fk_organization_parent_id FOREIGN KEY (parent_id) REFERENCES organization(id);
ALTER TABLE identity ADD CONSTRAINT fk_identity_person_id FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE org_membership ADD CONSTRAINT fk_org_membership_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE org_membership ADD CONSTRAINT fk_org_membership_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);
ALTER TABLE account ADD CONSTRAINT fk_account_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE external_identity ADD CONSTRAINT fk_external_identity_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE credential ADD CONSTRAINT fk_credential_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE webauthn_credential ADD CONSTRAINT fk_webauthn_credential_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);
ALTER TABLE webauthn_challenge ADD CONSTRAINT fk_webauthn_challenge_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE totp_credential ADD CONSTRAINT fk_totp_credential_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);
ALTER TABLE recovery_code ADD CONSTRAINT fk_recovery_code_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE account_activation_token ADD CONSTRAINT fk_account_activation_token_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE org_role_assignment ADD CONSTRAINT fk_org_role_assignment_membership_id FOREIGN KEY (membership_id) REFERENCES org_membership(id);
ALTER TABLE org_role_assignment ADD CONSTRAINT fk_org_role_assignment_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE org_role_assignment ADD CONSTRAINT fk_org_role_assignment_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);
ALTER TABLE org_role_assignment ADD CONSTRAINT fk_org_role_assignment_org_role_id FOREIGN KEY (org_role_id) REFERENCES org_role(id);
ALTER TABLE org_role_assignment ADD CONSTRAINT fk_org_role_assignment_assigned_by FOREIGN KEY (assigned_by) REFERENCES account(id);
ALTER TABLE role_permission ADD CONSTRAINT fk_role_permission_role_id FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE role_permission ADD CONSTRAINT fk_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES permission(id);
ALTER TABLE account_role_assignment ADD CONSTRAINT fk_account_role_assignment_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE account_role_assignment ADD CONSTRAINT fk_account_role_assignment_role_id FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE account_role_assignment ADD CONSTRAINT fk_account_role_assignment_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);
ALTER TABLE account_role_assignment ADD CONSTRAINT fk_account_role_assignment_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE account_role_assignment ADD CONSTRAINT fk_account_role_assignment_assigned_by FOREIGN KEY (assigned_by) REFERENCES account(id);
ALTER TABLE application ADD CONSTRAINT fk_application_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);
ALTER TABLE client_redirect_uri ADD CONSTRAINT fk_client_redirect_uri_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE client_logout_uri ADD CONSTRAINT fk_client_logout_uri_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE client_credential ADD CONSTRAINT fk_client_credential_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE application_scope ADD CONSTRAINT fk_application_scope_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE application_scope ADD CONSTRAINT fk_application_scope_scope_id FOREIGN KEY (scope_id) REFERENCES oidc_scope(id);
ALTER TABLE client_grant_type ADD CONSTRAINT fk_client_grant_type_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE idp_session ADD CONSTRAINT fk_idp_session_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE session_client ADD CONSTRAINT fk_session_client_session_id FOREIGN KEY (session_id) REFERENCES idp_session(id);
ALTER TABLE session_client ADD CONSTRAINT fk_session_client_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE authentication_event ADD CONSTRAINT fk_authentication_event_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE authentication_event ADD CONSTRAINT fk_authentication_event_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE authentication_event ADD CONSTRAINT fk_authentication_event_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE authentication_event ADD CONSTRAINT fk_authentication_event_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);
ALTER TABLE authorization_code ADD CONSTRAINT fk_authorization_code_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE authorization_code ADD CONSTRAINT fk_authorization_code_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE authorization_code ADD CONSTRAINT fk_authorization_code_session_id FOREIGN KEY (session_id) REFERENCES idp_session(id);
ALTER TABLE authorization_code_scope ADD CONSTRAINT fk_authorization_code_scope_authorization_code_id FOREIGN KEY (authorization_code_id) REFERENCES authorization_code(id);
ALTER TABLE authorization_code_scope ADD CONSTRAINT fk_authorization_code_scope_scope_id FOREIGN KEY (scope_id) REFERENCES oidc_scope(id);
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_session_id FOREIGN KEY (session_id) REFERENCES idp_session(id);
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_replaced_by_token_id FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_token(id);
ALTER TABLE refresh_token_scope ADD CONSTRAINT fk_refresh_token_scope_refresh_token_id FOREIGN KEY (refresh_token_id) REFERENCES refresh_token(id);
ALTER TABLE refresh_token_scope ADD CONSTRAINT fk_refresh_token_scope_scope_id FOREIGN KEY (scope_id) REFERENCES oidc_scope(id);
ALTER TABLE user_consent ADD CONSTRAINT fk_user_consent_identity_id FOREIGN KEY (identity_id) REFERENCES identity(id);
ALTER TABLE user_consent ADD CONSTRAINT fk_user_consent_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE user_consent ADD CONSTRAINT fk_user_consent_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE user_consent_scope ADD CONSTRAINT fk_user_consent_scope_user_consent_id FOREIGN KEY (user_consent_id) REFERENCES user_consent(id);
ALTER TABLE user_consent_scope ADD CONSTRAINT fk_user_consent_scope_scope_id FOREIGN KEY (scope_id) REFERENCES oidc_scope(id);
ALTER TABLE security_policy ADD CONSTRAINT fk_security_policy_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);
ALTER TABLE security_policy ADD CONSTRAINT fk_security_policy_application_id FOREIGN KEY (application_id) REFERENCES application(id);
ALTER TABLE security_policy ADD CONSTRAINT fk_security_policy_updated_by FOREIGN KEY (updated_by) REFERENCES account(id);
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_log_actor_account_id FOREIGN KEY (actor_account_id) REFERENCES account(id);
ALTER TABLE smart_card_credential ADD CONSTRAINT fk_smart_card_credential_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);
ALTER TABLE smart_card_challenge ADD CONSTRAINT fk_smart_card_challenge_account_id FOREIGN KEY (account_id) REFERENCES account(id);
ALTER TABLE smart_card_challenge ADD CONSTRAINT fk_smart_card_challenge_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);
ALTER TABLE smart_card_challenge ADD CONSTRAINT fk_smart_card_challenge_session_id FOREIGN KEY (session_id) REFERENCES idp_session(id);
ALTER TABLE authentication_transaction ADD CONSTRAINT fk_authentication_transaction_account_id FOREIGN KEY (account_id) REFERENCES account(id);
CREATE INDEX ix_audit_log_created_at ON audit_log (created_at);
CREATE INDEX ix_audit_log_actor_account_id ON audit_log (actor_account_id);
CREATE INDEX ix_refresh_token_family_id ON refresh_token (family_id);
CREATE INDEX ix_refresh_token_session_id ON refresh_token (session_id);
CREATE INDEX ix_smart_card_challenge_expires_at ON smart_card_challenge (expires_at);
CREATE INDEX ix_account_identity_id ON account (identity_id);
CREATE INDEX ix_credential_account_id ON credential (account_id);
CREATE INDEX ix_idp_session_account_id ON idp_session (account_id);