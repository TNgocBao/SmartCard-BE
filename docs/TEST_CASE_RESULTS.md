# Danh mục test đã thực thi

## Kết quả kiểm thử tự động gần nhất

Thời điểm UTC: `2026-11-25T14:57:40.268364100Z`. Chế độ: `cached-compatibility`. Tổng kiểm tra: **291**; số request được ghi riêng: **287**.

Môi trường: MockMvc + real Spring Security filters; H2 in memory. Đây là kết quả của danh mục đã viết, không có nghĩa mọi case có thể tồn tại đều đã kiểm tra.

| Case | Request | Expected | Actual | Kết quả |
|---|---|---|---|---|
| TC001 | `GET /api/v1/status` | 200 | 200 | PASS |
| TC002 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC003 | `POST /api/v1/auth/password` | 200 | 200 | PASS |
| TC004 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC005 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC006 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC007 | `GET /api/v1/management/catalog` | 200 | 200 | PASS |
| TC008 | `GET /api/v1/management/resources/organization` | 200 | 200 | PASS |
| TC009 | `GET /api/v1/management/resources/person` | 200 | 200 | PASS |
| TC010 | `GET /api/v1/management/resources/identity` | 200 | 200 | PASS |
| TC011 | `GET /api/v1/management/resources/org_membership` | 200 | 200 | PASS |
| TC012 | `GET /api/v1/management/resources/account` | 200 | 200 | PASS |
| TC013 | `GET /api/v1/management/resources/external_identity` | 200 | 200 | PASS |
| TC014 | `GET /api/v1/management/resources/credential` | 200 | 200 | PASS |
| TC015 | `GET /api/v1/management/resources/webauthn_credential` | 200 | 200 | PASS |
| TC016 | `GET /api/v1/management/resources/webauthn_challenge` | 200 | 200 | PASS |
| TC017 | `GET /api/v1/management/resources/totp_credential` | 200 | 200 | PASS |
| TC018 | `GET /api/v1/management/resources/recovery_code` | 200 | 200 | PASS |
| TC019 | `GET /api/v1/management/resources/account_activation_token` | 200 | 200 | PASS |
| TC020 | `GET /api/v1/management/resources/org_role` | 200 | 200 | PASS |
| TC021 | `GET /api/v1/management/resources/org_role_assignment` | 200 | 200 | PASS |
| TC022 | `GET /api/v1/management/resources/role` | 200 | 200 | PASS |
| TC023 | `GET /api/v1/management/resources/permission` | 200 | 200 | PASS |
| TC024 | `GET /api/v1/management/resources/role_permission` | 200 | 200 | PASS |
| TC025 | `GET /api/v1/management/resources/account_role_assignment` | 200 | 200 | PASS |
| TC026 | `GET /api/v1/management/resources/application` | 200 | 200 | PASS |
| TC027 | `GET /api/v1/management/resources/client_redirect_uri` | 200 | 200 | PASS |
| TC028 | `GET /api/v1/management/resources/client_logout_uri` | 200 | 200 | PASS |
| TC029 | `GET /api/v1/management/resources/client_credential` | 200 | 200 | PASS |
| TC030 | `GET /api/v1/management/resources/oidc_scope` | 200 | 200 | PASS |
| TC031 | `GET /api/v1/management/resources/application_scope` | 200 | 200 | PASS |
| TC032 | `GET /api/v1/management/resources/client_grant_type` | 200 | 200 | PASS |
| TC033 | `GET /api/v1/management/resources/signing_key` | 200 | 200 | PASS |
| TC034 | `GET /api/v1/management/resources/idp_session` | 200 | 200 | PASS |
| TC035 | `GET /api/v1/management/resources/session_client` | 200 | 200 | PASS |
| TC036 | `GET /api/v1/management/resources/authentication_event` | 200 | 200 | PASS |
| TC037 | `GET /api/v1/management/resources/authorization_code` | 200 | 200 | PASS |
| TC038 | `GET /api/v1/management/resources/authorization_code_scope` | 200 | 200 | PASS |
| TC039 | `GET /api/v1/management/resources/refresh_token` | 200 | 200 | PASS |
| TC040 | `GET /api/v1/management/resources/refresh_token_scope` | 200 | 200 | PASS |
| TC041 | `GET /api/v1/management/resources/user_consent` | 200 | 200 | PASS |
| TC042 | `GET /api/v1/management/resources/user_consent_scope` | 200 | 200 | PASS |
| TC043 | `GET /api/v1/management/resources/security_policy` | 200 | 200 | PASS |
| TC044 | `GET /api/v1/management/resources/audit_log` | 200 | 200 | PASS |
| TC045 | `GET /api/v1/management/resources/smart_card_credential` | 200 | 200 | PASS |
| TC046 | `GET /api/v1/management/resources/smart_card_challenge` | 200 | 200 | PASS |
| TC047 | `GET /api/v1/management/resources/authentication_transaction` | 200 | 200 | PASS |
| TC048 | `GET /api/v1/management/resources/outbox_event` | 200 | 200 | PASS |
| TC049 | `GET /api/v1/management/resources/credential` | 200 | 200 | PASS |
| TC050 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC051 | `POST /api/v1/accounts/{id}/lock` | 409 | 409 | PASS |
| TC052 | `POST /api/v1/accounts` | 200 | 200 | PASS |
| TC053 | `POST /api/v1/accounts/{id}/smart-cards` | 200 | 200 | PASS |
| TC054 | `POST /api/v1/auth/smart-card/challenge` | 200 | 200 | PASS |
| TC055 | `POST /api/v1/auth/smart-card/verify` | 401 | 401 | PASS |
| TC056 | `POST /api/v1/auth/smart-card/verify` | 200 | 200 | PASS |
| TC057 | `POST /api/v1/auth/smart-card/verify` | 410 | 410 | PASS |
| TC058 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC059 | `GET /api/v1/management/overview` | 403 | 403 | PASS |
| TC060 | `DELETE /api/v1/me/sessions/{id}` | 404 | 404 | PASS |
| TC061 | `POST /api/v1/auth/refresh` | 200 | 200 | PASS |
| TC062 | `POST /api/v1/auth/refresh` | 401 | 401 | PASS |
| TC063 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC064 | `POST /api/v1/auth/smart-card/challenge` | 200 | 200 | PASS |
| TC065 | `POST /api/v1/auth/smart-card/verify` | 410 | 410 | PASS |
| TC066 | `POST /api/v1/accounts/{id}/lock` | 200 | 200 | PASS |
| TC067 | `POST /api/v1/auth/smart-card/challenge` | 423 | 423 | PASS |
| TC068 | `POST /api/v1/accounts/{id}/unlock` | 200 | 200 | PASS |
| TC069 | `POST /api/v1/auth/smart-card/challenge` | 200 | 200 | PASS |
| TC070 | `POST /api/v1/auth/smart-card/verify` | 401 | 401 | PASS |
| TC071 | `POST /api/v1/auth/smart-card/challenge` | 401 | 401 | PASS |
| TC072 | `DELETE /api/v1/accounts/credentials/{id}` | 200 | 200 | PASS |
| TC073 | `POST /api/v1/auth/smart-card/challenge` | 401 | 401 | PASS |
| TC074 | `POST /api/v1/auth/password` | 200 | 200 | PASS |
| TC075 | `POST /api/v1/auth/password` | 401 | 401 | PASS |
| TC076 | `POST /api/v1/auth/password` | 401 | 401 | PASS |
| TC077 | `POST /api/v1/auth/password` | 401 | 401 | PASS |
| TC078 | `POST /api/v1/auth/password` | 401 | 401 | PASS |
| TC079 | `POST /api/v1/auth/password` | 401 | 401 | PASS |
| TC080 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC081 | `POST /api/v1/accounts/{id}/unlock` | 200 | 200 | PASS |
| TC082 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC083 | `PATCH /api/v1/management/resources/permission/{id}` | 409 | 409 | PASS |
| TC084 | `GET /api/v1/management/resources/unknown_table` | 404 | 404 | PASS |
| TC085 | `POST /api/v1/management/resources/credential` | 405 | 405 | PASS |
| TC086 | `POST /api/v1/management/resources/application` | 200 | 200 | PASS |
| TC087 | `PATCH /api/v1/management/resources/application/{id}` | 400 | 400 | PASS |
| TC088 | `POST /api/v1/management/resources/client_redirect_uri` | 400 | 400 | PASS |
| TC089 | `POST /api/v1/management/resources/client_redirect_uri` | 400 | 400 | PASS |
| TC090 | `POST /api/v1/management/resources/security_policy` | 409 | 409 | PASS |
| TC091 | `POST /api/v1/auth/password` | 200 | 200 | PASS |
| TC092 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC093 | `ASSERT Live grant takes effect with existing access token` | True | True | PASS |
| TC094 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC095 | `ASSERT Revoked grant disappears without waiting for JWT expiry` | True | True | PASS |
| TC096 | `ASSERT Organization grant cannot become a global grant` | True | True | PASS |
| TC097 | `ASSERT Organization grant matches its own scope` | True | True | PASS |
| TC098 | `ASSERT Organization grant cannot cross organization` | True | True | PASS |
| TC099 | `ASSERT Expired scoped assignment is denied` | True | True | PASS |
| TC100 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC101 | `POST /api/v1/auth/refresh` | 401 | 401 | PASS |
| TC102 | `POST /api/v1/auth/password` | 200 | 200 | PASS |
| TC103 | `POST /api/v1/auth/refresh` | 401 | 401 | PASS |
| TC104 | `ASSERT Last authenticator cannot be removed` | True | True | PASS |
| TC105 | `ASSERT RFC6238 SHA1 vector 59` | True | True | PASS |
| TC106 | `ASSERT RFC6238 SHA1 vector 1111111109` | True | True | PASS |
| TC107 | `ASSERT RFC6238 SHA1 vector 1111111111` | True | True | PASS |
| TC108 | `ASSERT RFC6238 SHA1 vector 1234567890` | True | True | PASS |
| TC109 | `ASSERT RFC6238 SHA1 vector 2000000000` | True | True | PASS |
| TC110 | `ASSERT RFC6238 SHA1 vector 20000000000` | True | True | PASS |
| TC111 | `ASSERT AES-GCM round trip` | True | True | PASS |
| TC112 | `ASSERT AES-GCM AAD swap rejected` | True | True | PASS |
| TC113 | `POST /api/v1/me/totp/setup` | 403 | 403 | PASS |
| TC114 | `POST /api/v1/accounts/{id}/activation-invite` | 202 | 202 | PASS |
| TC115 | `ASSERT Activation hash only` | True | True | PASS |
| TC116 | `POST /api/v1/auth/activate` | 200 | 200 | PASS |
| TC117 | `POST /api/v1/auth/activate` | 410 | 410 | PASS |
| TC118 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC119 | `POST /api/v1/auth/enrollment/totp/setup` | 200 | 200 | PASS |
| TC120 | `POST /api/v1/auth/enrollment/totp/setup` | 409 | 409 | PASS |
| TC121 | `POST /api/v1/auth/enrollment/totp/verify` | 401 | 401 | PASS |
| TC122 | `POST /api/v1/auth/enrollment/totp/verify` | 200 | 200 | PASS |
| TC123 | `ASSERT Recovery codes shown once at enrollment` | True | True | PASS |
| TC124 | `DELETE /api/v1/me/totp` | 409 | 409 | PASS |
| TC125 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC126 | `POST /api/v1/auth/password` | 200 | 200 | PASS |
| TC127 | `ASSERT Primary factor alone grants no access token` | True | True | PASS |
| TC128 | `POST /api/v1/auth/mfa/totp/verify` | 401 | 401 | PASS |
| TC129 | `POST /api/v1/auth/mfa/totp/verify` | 200 | 200 | PASS |
| TC130 | `POST /api/v1/auth/mfa/totp/verify` | 410 | 410 | PASS |
| TC131 | `ASSERT MFA session AAL2` | True | True | PASS |
| TC132 | `ASSERT TOTP secret encrypted` | True | True | PASS |
| TC133 | `POST /api/v1/auth/otp/request` | 200 | 200 | PASS |
| TC134 | `GET /api/v1/local-mailbox/{id}` | 200 | 200 | PASS |
| TC135 | `ASSERT Outbox payload encrypted` | True | True | PASS |
| TC136 | `POST /api/v1/auth/otp/verify` | 401 | 401 | PASS |
| TC137 | `POST /api/v1/auth/otp/verify` | 200 | 200 | PASS |
| TC138 | `POST /api/v1/auth/otp/verify` | 410 | 410 | PASS |
| TC139 | `POST /api/v1/auth/otp/request` | 429 | 429 | PASS |
| TC140 | `POST /api/v1/auth/otp/request` | 200 | 200 | PASS |
| TC141 | `POST /api/v1/me/recovery-codes/regenerate` | 200 | 200 | PASS |
| TC142 | `POST /api/v1/auth/recovery/verify` | 401 | 401 | PASS |
| TC143 | `POST /api/v1/auth/recovery/verify` | 200 | 200 | PASS |
| TC144 | `POST /api/v1/auth/recovery/verify` | 401 | 401 | PASS |
| TC145 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC146 | `POST /api/v1/auth/enrollment/totp/setup` | 200 | 200 | PASS |
| TC147 | `POST /api/v1/auth/enrollment/totp/verify` | 200 | 200 | PASS |
| TC148 | `POST /api/v1/auth/enrollment/totp/verify` | 410 | 410 | PASS |
| TC149 | `DELETE /api/v1/me/totp` | 409 | 409 | PASS |
| TC150 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC151 | `GET /api/v1/permissions` | 200 | 200 | PASS |
| TC152 | `ASSERT Permission catalog offers actual enforced codes only` | True | True | PASS |
| TC153 | `GET /api/v1/roles?size=101` | 400 | 400 | PASS |
| TC154 | `POST /api/v1/roles` | 201 | 201 | PASS |
| TC155 | `POST /api/v1/roles` | 409 | 409 | PASS |
| TC156 | `PATCH /api/v1/roles/{id}` | 409 | 409 | PASS |
| TC157 | `PUT /api/v1/roles/{id}/permissions` | 409 | 409 | PASS |
| TC158 | `PUT /api/v1/roles/{id}/permissions` | 200 | 200 | PASS |
| TC159 | `PUT /api/v1/roles/{id}/permissions` | 400 | 400 | PASS |
| TC160 | `POST /api/v1/accounts/{id}/role-assignments` | 201 | 201 | PASS |
| TC161 | `POST /api/v1/accounts/{id}/role-assignments` | 409 | 409 | PASS |
| TC162 | `POST /api/v1/accounts/{id}/role-assignments` | 400 | 400 | PASS |
| TC163 | `POST /api/v1/accounts/{id}/role-assignments` | 400 | 400 | PASS |
| TC164 | `GET /api/v1/roles` | 200 | 200 | PASS |
| TC165 | `POST /api/v1/roles` | 403 | 403 | PASS |
| TC166 | `PUT /api/v1/roles/{id}/permissions` | 403 | 403 | PASS |
| TC167 | `POST /api/v1/accounts/{id}/role-assignments` | 403 | 403 | PASS |
| TC168 | `POST /api/v1/accounts/{id}/lock` | 403 | 403 | PASS |
| TC169 | `DELETE /api/v1/accounts/credentials/{id}` | 403 | 403 | PASS |
| TC170 | `POST /api/v1/accounts` | 200 | 200 | PASS |
| TC171 | `POST /api/v1/accounts/{id}/lock` | 200 | 200 | PASS |
| TC172 | `POST /api/v1/accounts/{id}/unlock` | 200 | 200 | PASS |
| TC173 | `POST /api/v1/roles` | 201 | 201 | PASS |
| TC174 | `PUT /api/v1/roles/{id}/permissions` | 200 | 200 | PASS |
| TC175 | `PATCH /api/v1/roles/{id}` | 403 | 403 | PASS |
| TC176 | `POST /api/v1/accounts/{id}/role-assignments` | 403 | 403 | PASS |
| TC177 | `DELETE /api/v1/role-assignments/{id}` | 200 | 200 | PASS |
| TC178 | `POST /api/v1/accounts/{id}/lock` | 403 | 403 | PASS |
| TC179 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC180 | `ASSERT Role revocation affects an existing token immediately` | True | True | PASS |
| TC181 | `DELETE /api/v1/role-assignments/{id}` | 409 | 409 | PASS |
| TC182 | `POST /api/v1/accounts/{id}/role-assignments` | 201 | 201 | PASS |
| TC183 | `ASSERT Canonical scoped assignment cannot grant global rights` | True | True | PASS |
| TC184 | `ASSERT Canonical scoped assignment matches its own organization` | True | True | PASS |
| TC185 | `ASSERT Organization grant cannot cross into application scope` | True | True | PASS |
| TC186 | `POST /api/v1/accounts/{id}/role-assignments` | 403 | 403 | PASS |
| TC187 | `POST /api/v1/accounts/{id}/role-assignments` | 403 | 403 | PASS |
| TC188 | `DELETE /api/v1/role-assignments/{id}` | 200 | 200 | PASS |
| TC189 | `POST /api/v1/accounts/{id}/role-assignments` | 201 | 201 | PASS |
| TC190 | `POST /api/v1/accounts/{id}/lock` | 409 | 409 | PASS |
| TC191 | `DELETE /api/v1/role-assignments/{id}` | 200 | 200 | PASS |
| TC192 | `POST /api/v1/me/webauthn/registration/options` | 200 | 200 | PASS |
| TC193 | `POST /api/v1/me/webauthn/registration/verify` | 401 | 401 | PASS |
| TC194 | `POST /api/v1/me/webauthn/registration/verify` | 410 | 410 | PASS |
| TC195 | `POST /api/v1/me/webauthn/registration/options` | 200 | 200 | PASS |
| TC196 | `POST /api/v1/me/webauthn/registration/verify` | 401 | 401 | PASS |
| TC197 | `POST /api/v1/me/webauthn/registration/options` | 200 | 200 | PASS |
| TC198 | `POST /api/v1/me/webauthn/registration/verify` | 200 | 200 | PASS |
| TC199 | `POST /api/v1/me/webauthn/registration/verify` | 410 | 410 | PASS |
| TC200 | `ASSERT WebAuthn COSE public key and attested data persisted` | True | True | PASS |
| TC201 | `POST /api/v1/me/webauthn/registration/options` | 200 | 200 | PASS |
| TC202 | `POST /api/v1/me/webauthn/registration/verify` | 409 | 409 | PASS |
| TC203 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC204 | `POST /api/v1/auth/webauthn/verify` | 200 | 200 | PASS |
| TC205 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC206 | `POST /api/v1/auth/webauthn/verify` | 200 | 200 | PASS |
| TC207 | `ASSERT WebAuthn permits zero counters across distinct valid ceremonies` | True | True | PASS |
| TC208 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC209 | `ASSERT WebAuthn requests required UV` | True | True | PASS |
| TC210 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC211 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC212 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC213 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC214 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC215 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC216 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC217 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC218 | `POST /api/v1/auth/webauthn/verify` | 200 | 200 | PASS |
| TC219 | `ASSERT WebAuthn UV session has AAL2` | True | True | PASS |
| TC220 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC221 | `POST /api/v1/auth/webauthn/verify` | 410 | 410 | PASS |
| TC222 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC223 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC224 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC225 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC226 | `POST /api/v1/auth/webauthn/options` | 401 | 401 | PASS |
| TC227 | `POST /api/v1/auth/webauthn/options` | 401 | 401 | PASS |
| TC228 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC229 | `POST /api/v1/auth/webauthn/verify` | 410 | 410 | PASS |
| TC230 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC231 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC232 | `POST /api/v1/auth/webauthn/verify` | 410 | 410 | PASS |
| TC233 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC234 | `ASSERT Concurrent WebAuthn proof yields exactly one session and one consumed challenge` | True | True | PASS |
| TC235 | `POST /api/v1/auth/webauthn/options` | 200 | 200 | PASS |
| TC236 | `POST /api/v1/auth/webauthn/verify` | 401 | 401 | PASS |
| TC237 | `POST /api/v1/auth/webauthn/options` | 401 | 401 | PASS |
| TC238 | `POST /api/v1/auth/webauthn/options` | 429 | 429 | PASS |
| TC239 | `ASSERT Outbox successful delivery is not dispatched twice` | True | True | PASS |
| TC240 | `ASSERT Outbox failure schedules bounded retry` | True | True | PASS |
| TC241 | `ASSERT Outbox cannot claim before backoff elapsed` | True | True | PASS |
| TC242 | `ASSERT Outbox becomes DEAD after five delivery failures` | True | True | PASS |
| TC243 | `ASSERT DEAD outbox item cannot be claimed` | True | True | PASS |
| TC244 | `ASSERT Only one worker claims an outbox item` | True | True | PASS |
| TC245 | `ASSERT Expired worker lease can be recovered` | True | True | PASS |
| TC246 | `ASSERT Expired final lease does not cause unlimited retries` | True | True | PASS |
| TC247 | `ASSERT Recovered outbox item requires current claim version` | True | True | PASS |
| TC248 | `ASSERT Stale worker cannot acknowledge or fail another claim` | True | True | PASS |
| TC249 | `ASSERT Current outbox worker can acknowledge its own claim` | True | True | PASS |
| TC250 | `ASSERT Outbox stores encrypted notification bodies only` | True | True | PASS |
| TC251 | `GET /api/v1/me/credentials` | 200 | 200 | PASS |
| TC252 | `ASSERT Own credential API excludes all secrets and public-key payloads` | True | True | PASS |
| TC253 | `DELETE /api/v1/me/credentials/{id}` | 404 | 404 | PASS |
| TC254 | `DELETE /api/v1/me/credentials/{id}` | 403 | 403 | PASS |
| TC255 | `DELETE /api/v1/me/credentials/{id}` | 200 | 200 | PASS |
| TC256 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC257 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC258 | `ASSERT Self revoke updates credential state` | True | True | PASS |
| TC259 | `DELETE /api/v1/me/credentials/{id}` | 409 | 409 | PASS |
| TC260 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC261 | `POST /api/v1/logout` | 200 | 200 | PASS |
| TC262 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC263 | `GET /api/v1/signing-keys` | 200 | 200 | PASS |
| TC264 | `ASSERT Signing key metadata excludes private references` | True | True | PASS |
| TC265 | `GET /api/v1/signing-keys` | 403 | 403 | PASS |
| TC266 | `POST /api/v1/signing-keys/prepare` | 403 | 403 | PASS |
| TC267 | `POST /api/v1/signing-keys/prepare` | 400 | 400 | PASS |
| TC268 | `POST /api/v1/signing-keys/prepare` | 201 | 201 | PASS |
| TC269 | `GET /oauth2/jwks` | 200 | 200 | PASS |
| TC270 | `ASSERT NEXT key pre-published alongside active key` | True | True | PASS |
| TC271 | `ASSERT JWKS contains public parameters only` | True | True | PASS |
| TC272 | `POST /api/v1/signing-keys/rotate` | 400 | 400 | PASS |
| TC273 | `POST /api/v1/signing-keys/rotate` | 201 | 201 | PASS |
| TC274 | `ASSERT Rotation changes active kid` | True | True | PASS |
| TC275 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC276 | `ASSERT New tokens use rotated signing key` | True | True | PASS |
| TC277 | `POST /api/v1/signing-keys/rotate` | 409 | 409 | PASS |
| TC278 | `DELETE /api/v1/signing-keys/{id}` | 409 | 409 | PASS |
| TC279 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC280 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC281 | `GET /oauth2/jwks` | 200 | 200 | PASS |
| TC282 | `ASSERT Retired key disappears after overlap expiry` | True | True | PASS |
| TC283 | `DELETE /api/v1/signing-keys/{id}` | 200 | 200 | PASS |
| TC284 | `GET /api/v1/me` | 401 | 401 | PASS |
| TC285 | `ASSERT Emergency retired-key revocation revokes every active session` | True | True | PASS |
| TC286 | `GET /api/v1/me` | 200 | 200 | PASS |
| TC287 | `POST /api/v1/logout` | 200 | 200 | PASS |

Chưa được đánh dấu PASS bằng kết quả này: MySQL thực, SMTP/TLS/SIEM thực, Oracle Simulator chạy native, thẻ/đầu đọc thật, OIDC conformance và tải/HA/DR. Những phần này cần kết quả chạy độc lập.

Dấu vân tay nguồn: `b5577c9ab7e1c25ba08a5c89454f3ae5869cdf60e798916f1a70020dd32a48be`. Báo cáo máy đọc: `docs/test-evidence/backend-latest.json`; log: `docs/test-evidence/backend-latest.log`.
