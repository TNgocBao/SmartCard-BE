# Rà soát bảo mật và mức đáp ứng — 19/11/2026

## Kết luận

**Chưa đáp ứng đầy đủ cả ba tài liệu và chưa đủ bằng chứng để gọi là production-ready.** Bản hiện tại đã có xác thực Smart Card/MFA, activation/recovery, self-credential và vòng đời khóa ký; còn các nhóm OIDC, consent, policy và API lifecycle chưa hoàn tất. Có các cơ chế bảo mật nền, nhưng không phải toàn bộ cơ chế bảo mật của SRS v1.1.

## Phạm vi bằng chứng

| Hạng mục | Kết quả / giới hạn |
|---|---|
| Biên dịch backend và frontend | Đã qua biên dịch bằng dependency cache tại máy |
| MockMvc + Spring Security + service/repository | Đã qua bộ kiểm thử tích hợp; số kiểm tra của lần cuối nằm trong `backend/target/offline-verification.log` |
| Flyway + Hibernate | Migration H2 và validate mapping 41 entity đã qua |
| Frontend | Smoke check khởi tạo 6 màn hình và quyền nút ban đầu đã qua; không thay thế kiểm thử thao tác mọi màn hình với server/thẻ thật |
| Java Card | Biên dịch applet, converter CAP và installer đã qua; chưa chạy xuyên Oracle Simulator trong môi trường hiện tại |
| MySQL thực tế | Chưa xác minh migration/kết nối vì socket localhost bị môi trường tự động chặn; cần root credential ở setup cục bộ |
| Maven release | Chưa tạo được JAR chuẩn với toàn bộ dependency pin do Maven Central không truy cập được trong môi trường tự động |
| Workbench | Đã tạo profile KMA IdP Local; không đồng nghĩa schema/user ứng dụng đã được tạo |
| Dependency security | Đã đối chiếu và sửa một dependency JWT có advisory; chưa chạy SCA/SBOM scan toàn bộ dependency graph |

Kiểm thử dùng H2 trong RAM, MockMvc không mở socket và các thư viện cache khác phiên bản POM. Kết quả này không chứng minh MySQL, TLS, Java Card Simulator hoặc phần cứng thật đã chạy thành công.

## Cơ chế nền hiện có

| Cơ chế | Hiện trạng |
|---|---|
| Private key thẻ | Tạo và giữ trong applet, không truyền tới FE/BE |
| PIN | Kiểm tra trên thẻ, giới hạn 5 lần thử, reset quyền sau ký/deselect |
| Challenge | Nonce ngẫu nhiên 256 bit, TTL 60 giây, gắn account/credential, conditional consume trong transaction |
| Chữ ký | RSA-2048 trên applet, SHA256withRSA, BE kiểm tra public key được admin gán |
| JWT | RS256 allowlist, kid, signature, issuer, audience, exp, nbf; session ID gắn server state |
| Giới hạn JWT parser | Kích thước tối đa 8192, giới hạn nesting, từ chối duplicate JSON key trước parser JWT |
| Mật khẩu fallback admin | BCrypt cost 12; không có mật khẩu bootstrap mặc định trong source |
| Session | Access token 5 phút, refresh có hash, rotate/reuse detection, thu hồi khi logout/lock/revoke |
| Khóa tài khoản | Khóa sau 5 lần sai; thu hồi phiên để mở khóa không làm token cũ hồi sinh |
| Phân quyền | Authority quản trị kiểm tra tại BE; self-service session có ownership |
| Input / SQL | DTO validation, SQL bind parameters, catalog allowlist, redaction trường nhạy cảm |
| Transaction | Onboarding, enrollment, consume challenge, refresh và thao tác quản trị có transaction; có row lock ở luồng nhạy cảm |
| Local secret | Script cấu hình DB dùng Windows DPAPI; token FE giữ trong RAM |
| HTTPS / key custody | Production profile yêu cầu TLS và signing keystore lâu dài; cần kiểm thử cấu hình thực tế |
| Rate limit | Có fixed window theo IP cho auth, dành cho một node |

## Lỗi đã sửa trong quá trình rà soát

- Mapping DATE cho joined_at/ended_at của membership; không suy diễn mọi tên `_id` đều là FK UUID.
- Tách SQL khỏi controller/service, thêm DTO/entity/repository rõ ràng.
- Bảo vệ superuser và permission quản trị hệ thống khỏi sửa đổi làm mất quyền quản trị cuối.
- Không đọc field trực tiếp qua CGLIB service proxy; dùng method cho catalog.
- Đảm bảo password login tạo phiên trong transaction, không phụ thuộc self-invocation của `@Transactional`.
- Credential hết hạn bị từ chối ở cả bước xin challenge và verify.
- Thu hồi phiên sau khóa do thử sai; token cũ không hoạt động lại sau unlock.
- Refresh reuse chạy đồng thời cũng thu hồi family/phiên khi conditional update thất bại.
- Kiểm tra trạng thái account dưới row lock khi cấp phiên; kiểm tra locked_until tại API filter.
- Kiểm tra cấu hình PUBLIC client trên dữ liệu sau patch, tránh bỏ qua PKCE bằng partial update.
- Không tự khởi tạo HTTP client trong constructor giao diện; không ghi token/PIN vào activity log.
- Nạp SocketCardTerminalProvider qua JCA ServiceLoader, không phụ thuộc cấu hình java.security của máy.
- kid của production keystore dựa trên fingerprint public key, không dùng alias làm định danh khóa.
- Pin Nimbus từ 9.37.3 lên 9.37.4 và bổ sung parser guard. Cache offline vẫn có thể dùng bản cũ, nên không dùng cached runtime cho production.

Advisory liên quan Nimbus: [CVE-2025-53864, NVD](https://nvd.nist.gov/vuln/detail/CVE-2025-53864), [GitHub Reviewed Advisory](https://github.com/advisories/GHSA-xwmg-2g98-w7v9). Việc đổi POM chưa chứng minh bản vá đã được tải vào release build; phải build lại với dependency pin và quét toàn bộ graph khi có mạng.

## Phần bảo mật còn thiếu so với SRS

1. **OIDC/SSO/Consent**: Authorization Code + PKCE, discovery, ID Token, protocol userinfo/introspect/revoke và RP/back-channel logout chưa có.
2. **Policy/RBAC**: quyền global đã lấy từ DB mỗi request và scope evaluator đã có; chưa đủ permission catalog/API canonical, precedence và chống escalation toàn diện.
3. **Native WebAuthn / onboarding**: verifier ES256/none, UV/UP đã kiểm tra bằng fixture có chữ ký thật; chưa chứng minh biometrics/hardware/native client; activation hiện đi qua TOTP trước.
4. **Custody**: đã có AES-GCM và signing key lifecycle với overlap; cần kiểm chứng restart, PKCS12, vận hành xoay encryption key và backup/restore thực tế. Local encryption key file là cơ chế lab.
5. **Audit/observability**: đã có encrypted outbox/SMTP adapter/retry/lease/DEAD; chưa SIEM, source IP/trace đầy đủ, retention và chống sửa audit tại DB.
6. **Triển khai**: chưa chứng minh MySQL thật, Oracle Simulator native, TLS, SMTP, HA/DR và tải. Các test H2/MockMvc không thay thế các phần này.
7. **Dependency**: cache dùng phiên bản khác POM, chưa có release build/SCA/SBOM thành công; WebAuthn4J được pin 0.24.0 theo API/cached reference, cần rà soát/nâng cấp dependency có kiểm chứng trước production.
8. **Dữ liệu nghiệp vụ**: cây tổ chức, person/identity lifecycle, org-role consistency và claim minimization chưa hoàn tất.

## NFR đối chiếu

| NFR | Đánh giá |
|---|---|
| 01 Security | Một phần: TLS cấu hình, validation/rate limit; chưa hardening và test TLS thực tế |
| 02 Authorization | Một phần: admin authority và ownership; thiếu RBAC scoped đầy đủ |
| 03 Cryptography | Một phần: RSA/JWT/keystore; có lifecycle rotation/overlap; còn kiểm chứng custody/restart/HA thực |
| 04 Secret Protection | Một phần: BCrypt/hash/DPAPI; TOTP/OTP/outbox đã mã hóa; client secret lifecycle còn thiếu |
| 05 Availability | Chưa chứng minh backup/restore/HA/DR |
| 06 Performance | Chưa load test/sizing |
| 07 Scalability | Một node lab; chưa Redis/shared-store deployment |
| 08 Observability | Audit cơ bản; chưa tracing/SIEM/alerting đầy đủ |
| 09 Maintainability | Có phân lớp và module; cần CI/checkstyle/review khi mở rộng |
| 10 Interoperability | APDU riêng có tài liệu; chưa chứng nhận OIDC conformance |
| 11 Auditability | Một phần, chưa đủ các trường/sự kiện bắt buộc |
| 12 Privacy | Redaction và hạn chế token ở FE; chưa consent/claim-minimization engine |

## Điều kiện để kết luận sẵn sàng demo trên laptop

- Build pin Maven thành công hoặc dùng chế độ lab cache có ghi rõ giới hạn.
- Setup MySQL, chạy Flyway và validate entity trên MySQL thực.
- Khởi động simulator, cài CAP, đọc public key và thử PIN/signature xuyên FE–BE.
- Thử tạo tài khoản, gán thẻ, login, lock/unlock, revoke, refresh và audit bằng hai cửa sổ FE.
- Ghi lại kết quả và lỗi thực tế; chỉ sau đó mới kết luận luồng demo end-to-end hoạt động trên laptop.

Để đáp ứng đầy đủ ba file, cần triển khai thêm các nhóm thiếu ở trên và đối chiếu API contract, không chỉ bổ sung bảng.

## Bổ sung được kiểm tra trong vòng rà soát mới

- Refresh không vượt absolute/idle timeout; thu hồi quyền trong DB có hiệu lực ở request kế tiếp dù JWT còn hạn.
- WebAuthn challenge claim trong transaction riêng trước verify/write; proof sai, replay và hai request đồng thời không cấp hai phiên.
- Parser WebAuthn JSON/CBOR có giới hạn; RP ID/origin/UV/UP/signature/counter được verifier kiểm tra.
- Không tự tắt TOTP quản trị hoặc gỡ phương thức cuối; self revoke kiểm tra ownership và AAL2, thu hồi các phiên.
- Outbox claim kiểm tra due/backoff, không claim hai lần, tối đa 5 attempts và không lặp vô hạn sau final lease crash.
- Signing key có slot ACTIVE unique; key NEXT được publish trước rotation, key RETIRED chỉ xác minh trong overlap; private key không trả qua API/JWKS.
- FE không gửi expired bearer vào refresh; single-flight refresh, bounded response, không retry mutation khi timeout; không phục hồi phiên cũ sau logout/chuyển tài khoản.

Danh mục kết quả và dấu vân tay nguồn: `TEST_CASE_RESULTS.md`, `test-evidence/backend-latest.json`. Kết quả FE riêng ở `frontend/docs/test-evidence` trong workspace tổng hợp.
