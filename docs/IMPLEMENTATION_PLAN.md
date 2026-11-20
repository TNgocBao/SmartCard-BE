# Kế hoạch hoàn thiện KMA IdP v1.1

Phạm vi được mở rộng theo yêu cầu ngày 19/11/2026: hoàn thiện chức năng trong cả ba tài liệu, không dừng ở schema/demo thẻ.

- [x] Khung BE/FE desktop, 41 entity/repository, schema gốc và APDU applet.
- [x] Xác thực thẻ, JWT, refresh rotation, account lock, session ownership, audit nền.
- [x] Kiểm thử tương thích offline, MockMvc, validate Flyway/Hibernate và smoke FE.
- [x] Crypto service AES-GCM, authentication transaction, TOTP và chống replay.
- [x] Activation, recovery codes, reset MFA và onboarding có phiên giới hạn.
- [x] OTP provider, outbox retry và delivery adapter.
- [x] WebAuthn registration/assertion bằng thư viện chuyên dụng; ES256/none, UV/UP và atomic claim; cần native-client/attestation coverage tiếp.
- [ ] API chuẩn cho identity/organization/account/credential và admin incident response.
- [ ] RBAC granular/scoped, tổ chức, claim mapping và policy precedence.
- [ ] Client/scopes/grants/redirect URI/secret lifecycle.
- [ ] OIDC discovery, code+PKCE, ID Token, refresh/revoke/introspect/userinfo.
- [ ] Consent, SSO session, step-up, idle timeout và logout.
- [x] Signing key lifecycle và overlap public keys; custody PKCS12/local sealed files đã có mã, kiểm chứng vận hành/restart còn riêng.
- [ ] Hoàn thiện FE cho toàn bộ SRS; đã có MFA, recovery, self-credential, key lifecycle và regression API/Swing.
- [ ] Kết nối MySQL, simulator, SMTP/TLS/SIEM và hạ tầng thật: cần kiểm chứng ngoài sandbox chặn network.

Mặc định triển khai: single-tenant KMA; UUID; Person–Identity và Identity–Account 1:N; provisioning thủ công; OTP email qua adapter; MySQL là nguồn trạng thái có atomic consume; token TTL access/ID 5 phút, refresh/absolute session 8 giờ; idle timeout cấu hình; key custody PKCS12 + encryption key từ secret môi trường; lab một node. Những lựa chọn vận hành HA/DR, nguồn SIS/HR và SMTP thực tế không được giả định đã cấu hình.

Không đánh dấu hoàn thành chỉ vì có bảng hoặc controller. Chỉ chốt từng mục sau khi có use case, validation/security và kiểm thử; production/conformance/hardware phải có bằng chứng chạy thực tế.
