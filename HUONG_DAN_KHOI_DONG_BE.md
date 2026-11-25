# Khởi động Backend độc lập

## Khởi động local đã kiểm chứng — 20/11/2026

BE đã khởi động thành công và được kiểm tra qua HTTP với MySQL thật. Cổng 8080 đang bận, vì vậy dùng 18080 cho lần demo này. BE do agent bật đã được dừng sau test theo yêu cầu; MySQL vẫn cần chạy khi bạn tự khởi động.

PowerShell 1, tại root workspace:

```powershell
cd D:\ATBM-SYSTEM
.\backend\run.ps1 -UseCachedRuntime -Port 18080
```

Chờ `Started IdpApplication`. PowerShell 2:

```powershell
cd D:\ATBM-SYSTEM
.\frontend\run.ps1 -ApiBaseUrl http://127.0.0.1:18080
```

Nếu FE đang mở, nhập Backend `http://127.0.0.1:18080` rồi bấm Kết nối. Đã quan sát FE báo `Backend: UP` với BE chạy thật. Khi muốn dừng BE, Ctrl+C ở cửa sổ BE. Không cần dừng MySQL hoặc xóa container.

Kết quả thực thi:

- 173 file main biên dịch PASS trong output riêng tránh class lỗi do IDE.
- MySQL 8.4 kết nối bằng user ứng dụng, 4 migration Flyway validate PASS, JPA khởi tạo thành công.
- 8/8 HTTP smoke PASS: status, readiness DB, chặn anonymous, requestId/no-store, public JWKS, validation 400, bearer sai 401, đăng nhập admin bước đầu 200.
- 4 kiểm tra WebAuthn startup/CBOR và 3 kiểm tra readiness khi DB UP/DOWN/exception PASS.
- Không ghi password/token vào báo cáo. Báo cáo: `backend/docs/test-evidence/local-startup-latest.json`, `local-http-smoke-latest.json`, `webauthn-startup-latest.json`, `readiness-checks-latest.json` (trong repo backend bỏ tiền tố backend).

Chạy lại smoke (chỉ khi BE đang chạy): `python backend/scripts/smoke_local_http.py --port 18080 --check-admin`. Script hỏi mật khẩu bằng secure input và chỉ ghi kết quả an toàn.

Bản cache dùng thư viện tương thích cũ: Flyway cảnh báo MySQL 8.4 ngoài bản đã kiểm thử của thư viện; chưa thay thế build Maven đã pin. Bản cache không có Actuator health, readiness DB dùng `/api/v1/health`. Toàn bộ regression nghiệp vụ, Oracle Simulator và thẻ thật chưa được xác minh ở lượt startup này. Các báo cáo 241 BE/28 tích hợp là lịch sử.


<!-- CURRENT_VERIFICATION_START -->
## Cập nhật rà soát ngày 20/11/2026

- FE có **11 tab, 57 định nghĩa kịch bản**. Mỗi tab hiển thị mã case, thao tác và kết quả kỳ vọng; không tự gắn PASS cho hướng dẫn.
- FE: **25 kiểm tra API**, **37 kiểm tra thông báo lỗi/PIN**, **30 kiểm tra tab/case/phân quyền nút**, **46 kiểm tra luồng Swing/API/APDU giả lập** và smoke giao diện đều PASS.
- BE: startup MySQL thật và **8 HTTP smoke PASS**; **173 file main** biên dịch PASS. 56 kiểm tra exception trước đó là bằng chứng nguồn cũ, chưa chạy lại ở lượt startup này.
- Rà soát UI mới nhất: `../frontend/docs/UI_REGRESSION_REVIEW.md`; đã sửa dữ liệu cũ sau logout/đổi BE, kết quả request muộn, gán nhầm thẻ và tải lại trạng thái sau thu hồi phiên.
- Các lỗi có `code` và `requestId`; FE hiển thị thông báo ngắn, chỉ rõ trường cần sửa khi có thể, không đưa raw exception/SQL/token vào dialog hoặc activity log.
- Java Card CAP/installer đã build thành công (converter 26.0: 0 lỗi, 0 cảnh báo); native simulator/thẻ thật chưa được xác minh.
- Người dùng đã cho phép đích telemetry, BE startup và HTTP smoke hiện tại PASS. Full regression BE nghiệp vụ và FE–BE chưa chạy lại; các kết quả 241 BE / 28 tích hợp trước đây vẫn là lịch sử.

Chi tiết theo tab: `frontend/docs/DEMO_CASES.md`. Danh mục chờ chạy: `docs/PENDING_VERIFICATION_CASES.md`. Bằng chứng JSON: `frontend/docs/test-evidence/` và `docs/test-evidence/`.
<!-- CURRENT_VERIFICATION_END -->


## 1. Môi trường

JDK 21, Maven, Python để chạy các công cụ kiểm tra phụ trợ và MySQL 8.0.16+ tại `127.0.0.1:3306`.

Mở PowerShell tại root repository backend:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
& "$env:JAVA_HOME\bin\java.exe" -version
mvn.cmd -version
```

## 2. Build/test

```powershell
.\build.ps1
```

Maven verify biên dịch, test và đóng gói JAR. Script kiểm tra kiến trúc và cập nhật phần kết quả trong hướng dẫn này. Không cần clone frontend.

Nếu Maven Central bị chặn, sử dụng cache trên máy cho lab:

```powershell
python .\scripts\check_architecture.py
python .\scripts\offline_verify.py
```

Chỉ tiếp tục khi có `INTEGRATION CHECKS PASSED`. Đây là kiểm tra tương thích với thư viện cache, không thay thế build pin của production.

## 3. Tạo connection/schema MySQL

```powershell
.\setup-database.ps1
```

Nhập mật khẩu root trên laptop. Script tạo schema `kma_idp` và user ứng dụng ngẫu nhiên riêng, lưu credential bằng Windows DPAPI. Không xóa schema hay thay đổi user của project khác.

Nếu tự cấu hình, cung cấp `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Mẫu URL nằm trong `.env.example`; không commit giá trị thật.

## 4. Chạy

Sau build Maven:

```powershell
.\run.ps1
```

Sau build tương thích bằng cache:

```powershell
.\run.ps1 -UseCachedRuntime
```

Script hỏi mật khẩu bootstrap admin (14–72 ký tự). Chỉ tạo `admin` khi database trống, không reset mật khẩu đã có. Giữ cửa sổ đang chạy mở.

Kiểm tra từ PowerShell khác:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/v1/status
```

Cần trả `UP`. Chưa đăng nhập mà gọi `/api/v1/me` phải bị từ chối.

## 5. MFA và email

Luồng TOTP, activation, OTP và recovery có token một lần. Mã OTP/email nằm trong outbox mã hóa; mặc định không gửi email ra ngoài. Cấu hình `SMTP_*` và `OUTBOX_ENABLED=true` khi bạn có SMTP thực.

Local/test có hộp thư mô phỏng yêu cầu quyền quản trị, không được bật ở production. Activation token có thể trả một lần ở local để thao tác demo; production dùng delivery adapter.

Production cần TLS keystore, signing keystore, encryption key 256 bit và MySQL cấu hình qua môi trường. Cached runtime bị từ chối nếu bật profile prod.

## 6. Xử lý lỗi

- Không tải được parent/dependency: kiểm tra DNS/proxy của laptop; dùng đường cache chỉ cho lab.
- MySQL không kết nối: kiểm tra dịch vụ, cổng 3306 và credential; môi trường tự động có thể bị chặn socket.
- Flyway checksum mismatch: không sửa migration đã chạy; dùng migration V2+ tiếp theo.
- Token cũ bị từ chối sau thay đổi credential/khóa tài khoản: đăng nhập lại, không vô hiệu hóa guard.
- TOTP code vừa dùng bị từ chối: chờ mã kế tiếp; đây là chống replay.
- `STEP_UP_REQUIRED`: thực hiện lại đăng nhập đủ các yếu tố rồi thử thao tác nhạy cảm.

Nhấn Ctrl+C để dừng BE. Không cần xóa database.

<!-- TEST_RESULTS_START -->
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
<!-- TEST_RESULTS_END -->

<!-- EXCEPTION_RESULTS_START -->
## Exception tests độc lập

Thời điểm UTC: `2026-11-20T13:29:45.287806400Z`. 56 kiểm tra PASS. Scope: Standalone MockMvc/servlet only; no Spring Boot, database, SMTP, or sockets.

| Case | Kết quả |
|---|---|
| POST /probe/body returns 400 | PASS |
| POST /probe/body does not expose exception details | PASS |
| POST /probe/body returns stable code and request ID | PASS |
| Validation identifies field names without rejected values | PASS |
| POST /probe/body returns 400 | PASS |
| POST /probe/body does not expose exception details | PASS |
| POST /probe/body returns stable code and request ID | PASS |
| POST /probe/body returns 400 | PASS |
| POST /probe/body does not expose exception details | PASS |
| POST /probe/body returns stable code and request ID | PASS |
| POST /probe/body returns 200 | PASS |
| POST /probe/body does not expose exception details | PASS |
| GET /probe/integer returns 400 | PASS |
| GET /probe/integer does not expose exception details | PASS |
| GET /probe/integer returns stable code and request ID | PASS |
| GET /probe/integer?value=not-a-number returns 400 | PASS |
| GET /probe/integer?value=not-a-number does not expose exception details | PASS |
| GET /probe/integer?value=not-a-number returns stable code and request ID | PASS |
| GET /probe/header returns 400 | PASS |
| GET /probe/header does not expose exception details | PASS |
| GET /probe/header returns stable code and request ID | PASS |
| POST /probe/body returns 415 | PASS |
| POST /probe/body does not expose exception details | PASS |
| POST /probe/body returns stable code and request ID | PASS |
| DELETE /probe/body returns 405 | PASS |
| DELETE /probe/body does not expose exception details | PASS |
| DELETE /probe/body returns stable code and request ID | PASS |
| 405 retains the allowed HTTP method | PASS |
| GET /probe/known returns 423 | PASS |
| GET /probe/known does not expose exception details | PASS |
| GET /probe/known returns stable code and request ID | PASS |
| GET /probe/conflict returns 409 | PASS |
| GET /probe/conflict does not expose exception details | PASS |
| GET /probe/conflict returns stable code and request ID | PASS |
| GET /probe/concurrent returns 409 | PASS |
| GET /probe/concurrent does not expose exception details | PASS |
| GET /probe/concurrent returns stable code and request ID | PASS |
| GET /probe/unavailable returns 503 | PASS |
| GET /probe/unavailable does not expose exception details | PASS |
| GET /probe/unavailable returns stable code and request ID | PASS |
| GET /probe/server returns 500 | PASS |
| GET /probe/server does not expose exception details | PASS |
| GET /probe/server returns stable code and request ID | PASS |
| GET /probe/denied returns 403 | PASS |
| GET /probe/denied does not expose exception details | PASS |
| GET /probe/denied returns stable code and request ID | PASS |
| GET /probe/missing returns 404 | PASS |
| GET /probe/missing does not expose exception details | PASS |
| GET /probe/missing returns stable code and request ID | PASS |
| Unsupported response format returns structured 406 | PASS |
| Unexpected filter failure is sanitized JSON 500 | PASS |
| Invalid error status and code cannot expose arbitrary text | PASS |
| Filter error 401 has the same code/requestId schema | PASS |
| Filter error 403 has the same code/requestId schema | PASS |
| Filter error 429 has the same code/requestId schema | PASS |
| Rate limit returns structured 429 with Retry-After | PASS |

Dấu vân tay nguồn: `20f5a1f8516cee495fa5c3a878a1dfb630a8426678216ccfe6dc09c009be572a`. JSON: `docs/test-evidence/backend-exceptions-latest.json`. Regression nghiệp vụ tổng vẫn cần kết quả riêng.
<!-- EXCEPTION_RESULTS_END -->

## MySQL chưa chạy trên laptop

Chẩn đoán mới nhất: Docker Engine đang chạy nhưng không có container MySQL; các connection Workbench chỉ lưu địa chỉ. Script riêng KMA: `backend/start-mysql.ps1` (chạy từ root workspace). Container `atbm-mysql`, volume `atbm_mysql_data`; giữ nguyên volume project cũ. Script đã qua kiểm tra cú pháp, chưa được thực thi do giới hạn quyền Docker của agent. Xem `docs/MYSQL_LOCAL_TROUBLESHOOTING.md` để chạy từ PowerShell của bạn và đọc giới hạn kiểm chứng.

### Sửa quyền MySQL qua Docker

Đã sửa grant host từ `localhost` sang host client thực tế (quan sát `172.17.0.1`), và xác minh đăng nhập MySQL thật bằng tài khoản ứng dụng PASS. Chạy `.\setup-database.ps1 -RepairConnection` nếu cần sửa lại. Giữ nguyên schema/credential, không chạy BE bằng root. 15 kiểm tra validation setup PASS. Kết quả DB không thay thế kiểm tra khởi động BE; lần chạy BE hiện bị duyệt tự động chặn do external telemetry. Xem `docs/MYSQL_LOCAL_TROUBLESHOOTING.md`.

## Lỗi WebAuthnVerifier / Unresolved compilation problems khi chạy cache

Ngày 20/11/2026 đã đọc được lỗi trong terminal VS Code: `com.fasterxml.jackson.dataformat.cbor cannot be resolved` và `ObjectMapper(Object) is undefined`, phát sinh khi khởi tạo WebAuthnVerifier. Log cho thấy class được nạp từ output biên dịch của IDE có lỗi `Unresolved compilation problems`; launcher cũ dùng chung `target/classes` với IDE.

Đã khai báo trực tiếp `jackson-dataformat-cbor` trong pom vì mã BE import CBORFactory, và tách runtime cache khỏi output của IDE:

- `run.ps1 -UseCachedRuntime` tự gọi `scripts/prepare_cached_runtime.py` trước khi hỏi mật khẩu/chạy Java.
- Script kiểm tra dependency CBOR/WebAuthn và biên dịch bằng javac vào `target/cached-runtime/classes`, không nạp `target/classes` của IDE.
- Biên dịch lỗi thì dừng trước khi khởi động; không chạy lại class cũ.
- Biên dịch 171 file main PASS; 4 kiểm tra constructor WebAuthn/CBOR/vị trí class/origin PASS bằng classpath nhỏ, không khởi động Spring Boot, DB hoặc HTTP server.

Chạy lại từ root workspace:

```powershell
cd D:\ATBM-SYSTEM
.\backend\run.ps1 -UseCachedRuntime
```

Bằng chứng: `backend/docs/test-evidence/cached-runtime-compilation-latest.json` và `backend/docs/test-evidence/webauthn-startup-latest.json` (trong repo backend bỏ tiền tố backend). Kiểm tra riêng: `python backend/scripts/prepare_cached_runtime.py`, rồi `python backend/scripts/verify_webauthn_startup.py`.

Đây chưa phải PASS khởi động BE/Flyway/JPA/API đầy đủ. Agent chưa được phép chạy lại toàn ứng dụng vì duyệt tự động đã chặn external telemetry tới dc.services.visualstudio.com. Không dùng kiểm tra constructor riêng này để thay thế bộ test đã bị chặn.
