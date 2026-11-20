# Trạng thái theo cả ba tài liệu nguồn

Mục tiêu là hoàn thiện toàn bộ SRS v1.1. Bảng này phân biệt chức năng đã có mã và test với phần chưa đủ nghiệp vụ; số case chạy gần nhất nằm trong `TEST_CASE_RESULTS.md`. Có schema không đồng nghĩa đã hoàn thành yêu cầu.

| Yêu cầu | Đã triển khai / có kiểm tra giả lập | Phần chưa hoàn tất |
|---|---|---|
| FR-01 Person / Identity | Onboarding Person–Identity–Account, unique/FK, model đầy đủ | API lifecycle riêng, disable cascade và ownership chi tiết |
| FR-02 Organization | Schema, cấu hình tổ chức/membership | Cây chống chu trình, thời hạn membership, nguồn SIS/HR |
| FR-03 Account | Tạo, kích hoạt bằng thẻ/TOTP, lock/unlock, thử sai, thu hồi phiên | API lifecycle/cập nhật/đình chỉ đầy đủ và chính sách configurable |
| FR-04 Credential | Enrollment thẻ/Passkey/TOTP; GET/DELETE credential bản thân; AAL2, ownership, bảo vệ phương thức cuối; admin revoke | Native Passkey client và workflow provisioning thẻ thật |
| FR-05 MFA | WebAuthn4J ES256 none-attestation + UV/UP; TOTP RFC6238; OTP email; AAL1/AAL2; AES-GCM | Attestation trust nâng cao và thiết bị thực; chưa coi fixture là bằng chứng hardware |
| FR-06 Authentication | Challenge atomic một lần, MFA transaction, step-up guard 5 phút, replay/counter/rate-limit tests | Policy engine, OIDC acr_values/prompt/max_age và telemetry đầy đủ |
| FR-07 OIDC | JWT nội bộ, public JWKS | Authorization Code/PKCE, discovery, ID Token, protocol token/userinfo/revoke/introspect/logout |
| FR-08 Session | Ownership, logout, force logout, rotation/reuse; idle 30 phút, absolute 8 giờ; refresh kiểm tra cả hai | SSO cookie và RP/back-channel logout |
| FR-09 Client | Schema, lab CRUD metadata/URI/scope/grant; PKCE partial-patch và URI validation | Client secret lifecycle, API canonical và protocol validation |
| FR-10 RBAC | Quyền global lấy từ DB từng request, evaluator scope org/app, hiệu lực assignment; chặn scope chéo | Permission catalog/granular enforcement toàn API và workflow chống escalation |
| FR-11 Consent | Schema và lab read | Grant/revoke, UI và token invalidation theo app |
| FR-12 Policy | Schema, cấu hình và check scope | Evaluator, precedence và enforcement |
| FR-13 Audit | Audit thao tác chính, login/failure/reuse/WebAuthn; encrypted outbox worker và bounded retry | IP/trace đầy đủ, SIEM, retention và DB tamper resistance |
| FR-14 Activation | Invite token hash, TTL, single use; restricted enrollment; QR TOTP; email outbox | Passkey-first activation; SMTP thật; xác minh danh tính ngoài kênh |
| FR-15 Recovery | Mười mã hash dùng một lần; regenerate vô hiệu mã cũ; reset credential/session; restricted reenrollment | Kịch bản recovery bằng thiết bị và quy trình xác minh ngoài kênh thực tế |
| FR-16 Signing keys | NEXT/ACTIVE/RETIRED/REVOKED, fingerprint kid, một ACTIVE qua DB, JWKS overlap, emergency revoke; PKCS12 production / sealed files local | Kiểm chứng restart/PKCS12 triển khai thật, HA rotation và vận hành KMS/HSM |
| FR-17 Org roles / claims | Model và cấu hình org roles/assignments | Chuẩn hóa membership ↔ identity/org; OIDC scope/consent claim mapping |

Bằng chứng BE: `docs/test-evidence/backend-latest.json` và log tương ứng. Bằng chứng FE/API/Swing tích hợp nằm trong repository frontend, `docs/test-evidence/`. H2/MockMvc/software APDU khác MySQL/TCP/TLS/Oracle Simulator thực.

Chưa đủ căn cứ gọi bản hiện tại là IdP OIDC hoàn chỉnh hoặc production-ready. Các phần còn lại phải có mã, validation, enforcement và test độc lập trước khi đổi trạng thái.
