"""Reproducible baseline: v1.1 UUID model + explicitly retained legacy metadata.
Never run against a migrated database; future changes require V2+ migrations.
"""
from pathlib import Path
import json

# S=bounded string, T=text, I=integer, B=boolean, D=date, L=bigint.
models = {
 'organization': 'parent_id org_code org_name org_type description:T status created_at updated_at',
 'person': 'person_code display_name email phone date_of_birth:D status created_at updated_at',
 'identity': 'person_id subject_id identifier identity_type status created_at updated_at',
 'org_membership': 'identity_id organization_id membership_type status joined_at:D ended_at:D created_at',
 'account': 'identity_id username account_type status is_locked:B failed_login_attempts:I locked_until lock_reason webauthn_user_handle:T last_login_at created_at updated_at',
 'external_identity': 'identity_id issuer external_subject provider linked_at',
 'credential': 'account_id credential_type credential_name credential_identifier secret_hash:T secret_reference:T certificate_fingerprint status registered_at last_used_at expires_at revoked_at',
 'webauthn_credential': 'credential_id webauthn_credential_id:T user_handle:T rp_id public_key_cose:T cose_algorithm:I sign_count:L aaguid transports:T backup_eligible:B backup_state:B attestation_format',
 'webauthn_challenge': 'account_id challenge ceremony_type rp_id user_verification expires_at consumed_at created_at',
 'totp_credential': 'credential_id encrypted_secret:T encryption_key_id algorithm digits:I period_seconds:I verified_at last_accepted_step:L',
 'recovery_code': 'account_id code_hash created_at used_at revoked_at',
 'account_activation_token': 'account_id token_hash purpose expires_at consumed_at created_at',
 'org_role': 'role_code role_name description:T is_active:B',
 'org_role_assignment': 'membership_id identity_id organization_id org_role_id assigned_by valid_from valid_until status',
 'role': 'role_code role_name description:T is_system:B is_active:B created_at updated_at',
 'permission': 'permission_code resource action description:T is_active:B',
 'role_permission': 'role_id permission_id created_at',
 'account_role_assignment': 'account_id role_id organization_id application_id assigned_by valid_from valid_until status created_at',
 'application': 'organization_id client_id client_name client_type protocol client_auth_method require_pkce:B status created_at updated_at',
 'client_redirect_uri': 'application_id redirect_uri:T is_active:B created_at',
 'client_logout_uri': 'application_id post_logout_redirect_uri:T is_active:B',
 'client_credential': 'application_id credential_type credential_name secret_hash:T secret_reference:T public_key:T certificate_fingerprint expires_at revoked_at status created_at',
 'oidc_scope': 'scope_name description:T is_standard:B is_active:B',
 'application_scope': 'application_id scope_id is_default:B is_required:B status',
 'client_grant_type': 'application_id grant_type is_enabled:B',
 'signing_key': 'kid algorithm public_jwk:T private_key_ref:T status not_before expires_at created_at',
 'idp_session': 'account_id session_id_hash status authentication_level amr:T ip_address user_agent:T authenticated_at last_activity_at expires_at revoked_at',
 'session_client': 'session_id application_id joined_at logged_out_at',
 'authentication_event': 'identity_id account_id application_id credential_id authentication_method event_type result failure_reason_code ip_address user_agent:T created_at',
 'authorization_code': 'application_id account_id session_id code_hash redirect_uri:T nonce code_challenge code_challenge_method expires_at consumed_at created_at',
 'authorization_code_scope': 'authorization_code_id scope_id',
 'refresh_token': 'account_id application_id session_id token_hash family_id replaced_by_token_id status issued_at expires_at consumed_at revoked_at',
 'refresh_token_scope': 'refresh_token_id scope_id',
 'user_consent': 'identity_id account_id application_id status granted_at revoked_at',
 'user_consent_scope': 'user_consent_id scope_id granted_at',
 'security_policy': 'organization_id application_id policy_type policy_name policy_config:T priority:I status updated_by updated_at',
 'audit_log': 'actor_account_id event_type action resource_type resource_id result ip_address correlation_id metadata:T created_at',
 'smart_card_credential': 'credential_id card_identifier public_key_spki:T algorithm transport status created_at',
 'smart_card_challenge': 'account_id credential_id session_id challenge_hash challenge_payload:T expires_at consumed_at created_at',
 'authentication_transaction': 'account_id required_acr completed_amr:T oidc_context_json:T expires_at created_at',
 'outbox_event': 'event_type payload:T status attempts:I next_attempt_at created_at'
}
refs = {'person_id':'person','identity_id':'identity','organization_id':'organization','account_id':'account',
 'membership_id':'org_membership','org_role_id':'org_role','role_id':'role','permission_id':'permission',
 'application_id':'application','credential_id':'credential','scope_id':'oidc_scope','session_id':'idp_session',
 'authorization_code_id':'authorization_code','refresh_token_id':'refresh_token','user_consent_id':'user_consent',
 'assigned_by':'account','updated_by':'account','actor_account_id':'account','replaced_by_token_id':'refresh_token'}
unique = {'organization': [('org_code',)], 'person':[('person_code',)], 'identity':[('subject_id',),('identity_type','identifier')],
 'account':[('username',)], 'external_identity':[('issuer','external_subject')], 'org_role':[('role_code',)],
 'role':[('role_code',)], 'permission':[('permission_code',)], 'role_permission':[('role_id','permission_id')],
 'application':[('client_id',)],'oidc_scope':[('scope_name',)],'application_scope':[('application_id','scope_id')],
 'client_grant_type':[('application_id','grant_type')], 'signing_key':[('kid',)], 'idp_session':[('session_id_hash',)],
 'session_client':[('session_id','application_id')], 'authorization_code':[('code_hash',)],
 'authorization_code_scope':[('authorization_code_id','scope_id')], 'refresh_token':[('token_hash',)],
 'refresh_token_scope':[('refresh_token_id','scope_id')], 'user_consent':[('account_id','application_id')],
 'user_consent_scope':[('user_consent_id','scope_id')], 'smart_card_credential':[('card_identifier',),('credential_id',)],
 'totp_credential':[('credential_id',)], 'webauthn_credential':[('credential_id',)],
 'recovery_code':[('code_hash',)],'account_activation_token':[('token_hash',)],'webauthn_challenge':[('challenge',)]}
required = {'person':{'display_name','status'},'identity':{'person_id','subject_id','identity_type','status'},
 'account':{'identity_id','username','status','is_locked'},'credential':{'account_id','credential_type','status'},
 'smart_card_credential':{'credential_id','card_identifier','public_key_spki','algorithm','transport','status'},
 'smart_card_challenge':{'account_id','credential_id','challenge_hash','challenge_payload','expires_at'},
 'idp_session':{'account_id','session_id_hash','status','expires_at'},'role':{'role_code','is_active'},
 'account_role_assignment':{'account_id','role_id','status'},'permission':{'permission_code'},'role_permission':{'role_id','permission_id'}}
types={'S':'VARCHAR(255)','T':'TEXT','I':'INT','B':'BOOLEAN','D':'DATE','L':'BIGINT'}
sql=['-- UUID identifiers. Instants are ISO-8601 UTC text; use UTC lexicographic ordering.\n']
catalog=[]
for table,definition in models.items():
 fields={x.split(':')[0]:types[x.split(':')[1] if ':' in x else 'S'] for x in definition.split()}
 columns=['id VARCHAR(36) PRIMARY KEY']
 for name,typ in fields.items():
  if name in refs or name=='parent_id': typ='VARCHAR(36)'
  if typ!='DATE' and (name.endswith('_at') or name in ('valid_from','valid_until','locked_until','not_before')): typ='VARCHAR(40)'
  columns.append(name+' '+typ+(' NOT NULL' if name in required.get(table,set()) else ''))
 for cols in unique.get(table,[]): columns.append('UNIQUE ('+', '.join(cols)+')')
 if table in ('account_role_assignment','security_policy'): columns.append('CHECK (organization_id IS NULL OR application_id IS NULL)')
 sql.append('CREATE TABLE '+table+' (\n  '+',\n  '.join(columns)+'\n);\n')
 catalog.append({'table':table,'columns':['id']+list(fields),'required':sorted(required.get(table,set()))})
for table,definition in models.items():
 for token in definition.split():
  col=token.split(':')[0]
  target = 'organization' if table=='organization' and col=='parent_id' else refs.get(col)
  if target: sql.append(f'ALTER TABLE {table} ADD CONSTRAINT fk_{table}_{col} FOREIGN KEY ({col}) REFERENCES {target}(id);')
for table,col in [('audit_log','created_at'),('audit_log','actor_account_id'),('refresh_token','family_id'),('refresh_token','session_id'),('smart_card_challenge','expires_at'),('account','identity_id'),('credential','account_id'),('idp_session','account_id')]:
 sql.append(f'CREATE INDEX ix_{table}_{col} ON {table} ({col});')
root=Path(__file__).resolve().parents[1]
(root/'target/schema-draft/db/migration').mkdir(parents=True,exist_ok=True)
(root/'target/schema-draft/db/migration/V1__idp_baseline.sql').write_text('\n'.join(sql),encoding='utf-8')
(root/'target/schema-draft/schema-catalog.json').write_text(json.dumps(catalog,indent=2),encoding='utf-8')
print(f'{len(models)} baseline draft tables generated under target/schema-draft; current migrations/catalog were not changed')
