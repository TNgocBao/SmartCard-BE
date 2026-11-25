# API thực tế đang triển khai

Được trích từ controller; endpoint tồn tại không chứng minh toàn bộ business rule của SRS đã được đáp ứng. Các API `/management/resources/*` là API lab, không thay thế contract OAuth/OIDC hay Management API riêng trong tài liệu v1.1.

| Method | Path | Controller |
|---|---|---|
| POST | `/api/v1/accounts` | `AccountController` |
| POST | `/api/v1/accounts/{id}/smart-cards` | `AccountController` |
| POST | `/api/v1/accounts/{id}/lock` | `AccountController` |
| POST | `/api/v1/accounts/{id}/unlock` | `AccountController` |
| DELETE | `/api/v1/accounts/{id}/sessions` | `AccountController` |
| DELETE | `/api/v1/accounts/credentials/{id}` | `AccountController` |
| POST | `/api/v1/accounts/{id}/activation-invite` | `RecoveryController` |
| POST | `/api/v1/accounts/{id}/mfa/reset` | `RecoveryController` |
| POST | `/api/v1/auth/activate` | `RecoveryController` |
| POST | `/api/v1/auth/enrollment/totp/setup` | `RecoveryController` |
| POST | `/api/v1/auth/enrollment/totp/verify` | `RecoveryController` |
| POST | `/api/v1/auth/recovery/verify` | `RecoveryController` |
| POST | `/api/v1/me/recovery-codes/regenerate` | `RecoveryController` |
| GET | `/api/v1/local-mailbox/{id}` | `LocalMailboxController` |
| POST | `/api/v1/auth/password` | `AuthController` |
| POST | `/api/v1/auth/smart-card/challenge` | `AuthController` |
| POST | `/api/v1/auth/smart-card/verify` | `AuthController` |
| POST | `/api/v1/auth/refresh` | `AuthController` |
| POST | `/api/v1/auth/mfa/totp/verify` | `MfaController` |
| POST | `/api/v1/auth/otp/request` | `OtpController` |
| POST | `/api/v1/auth/otp/verify` | `OtpController` |
| GET | `/oauth2/jwks` | `PublicKeyController` |
| GET | `/api/v1/me/credentials` | `CredentialSelfController` |
| DELETE | `/api/v1/me/credentials/{id}` | `CredentialSelfController` |
| POST | `/api/v1/me/totp/setup` | `TotpController` |
| POST | `/api/v1/me/totp/verify` | `TotpController` |
| DELETE | `/api/v1/me/totp` | `TotpController` |
| POST | `/api/v1/me/webauthn/registration/options` | `WebAuthnController` |
| POST | `/api/v1/me/webauthn/registration/verify` | `WebAuthnController` |
| POST | `/api/v1/auth/webauthn/options` | `WebAuthnController` |
| POST | `/api/v1/auth/webauthn/verify` | `WebAuthnController` |
| GET | `/api/v1/management/catalog` | `ManagementController` |
| GET | `/api/v1/management/overview` | `ManagementController` |
| GET | `/api/v1/management/resources/{table}` | `ManagementController` |
| POST | `/api/v1/management/resources/{table}` | `ManagementController` |
| PATCH | `/api/v1/management/resources/{table}/{id}` | `ManagementController` |
| GET | `/api/v1/signing-keys` | `SigningKeyController` |
| POST | `/api/v1/signing-keys/prepare` | `SigningKeyController` |
| POST | `/api/v1/signing-keys/rotate` | `SigningKeyController` |
| DELETE | `/api/v1/signing-keys/{id}` | `SigningKeyController` |
| GET | `/api/v1/me` | `SessionController` |
| GET | `/api/v1/me/sessions` | `SessionController` |
| DELETE | `/api/v1/me/sessions/{id}` | `SessionController` |
| POST | `/api/v1/logout` | `SessionController` |
| GET | `/api/v1/status` | `SystemController` |

Chưa có Authorization Code/PKCE, discovery, userinfo, introspection và consent workflow. Có activation, recovery, MFA và signing-key lifecycle như danh sách trên. `/oauth2/jwks` chỉ xuất public key; không có nghĩa toàn bộ OAuth2/OIDC đã triển khai.

## API quản lý thẻ bổ sung

- `GET /api/v1/me/smart-cards?page=0`: danh sách thẻ của mình, không trả public key/private key.
- `GET /api/v1/me/smart-cards/history?page=0`: lịch sử CARD_PROOF và đăng nhập bằng thẻ.
- Có thể truyền `accountId` để xem tài khoản khác khi có quyền credential.manage; bảo vệ tài khoản đặc quyền vẫn áp dụng.
- Transport đăng ký: SOFTWARE / SIMULATOR / PCSC; SOFTWARE và SIMULATOR chỉ được đăng ký/xác thực khi bật allow-simulator.
