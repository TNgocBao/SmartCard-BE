# Đối chiếu ba tài liệu nguồn

1. Giữ toàn bộ 32 bảng và tên cột trong `kma_idp_erd.md` để không mất thuộc tính gốc.
2. Áp dụng UUID của đặc tả v1.1, lưu `VARCHAR(36)`; các kiểu Long/bigint ở hai sơ đồ cũ được chuẩn hóa thành UUID. Các bộ đếm vẫn dùng INT/BIGINT.
3. Bổ sung từ v1.1: `totp_credential`, `recovery_code`, `account_activation_token`, `signing_key`, `refresh_token_scope`.
4. Bổ sung luồng thẻ/hạ tầng: `smart_card_credential`, `smart_card_challenge`, `authentication_transaction`, `outbox_event`. Tổng 41 bảng nghiệp vụ.
5. `org_role_assignment` giữ `membership_id` cũ, bổ sung `identity_id` và `organization_id` của v1.1. Cần chốt một nguồn chuẩn trước khi triển khai claim mapping; generic CRUD chưa chứng minh toàn bộ tính nhất quán giữa ba trường.
6. `user_consent` giữ `identity_id` cũ, bổ sung `account_id` theo v1.1. Use case consent chưa được triển khai nên không tự suy diễn ownership từ dữ liệu chưa chốt.
7. Tên cột tương đương dùng ánh xạ thay vì nhân đôi dữ liệu: `full_name` → `display_name`; organization `code/name/organization_type` → `org_code/org_name/org_type`; `valid_to` → `ended_at` ở membership và `valid_until` ở role assignment; credential `display_name/created_at` → `credential_name/registered_at`; session `session_token_hash/created_at/last_accessed_at` → `session_id_hash/authenticated_at/last_activity_at`; audit `details/occurred_at` → `metadata/created_at`.
8. JWK/JSON/COSE/SPKI được lưu TEXT; binary credential material dùng Base64. Không lưu private key thẻ. Không lưu JWT/refresh token plaintext trong database.
9. Instant được chuẩn hóa ISO-8601 UTC với đúng 9 chữ số nano trong VARCHAR(40), đảm bảo thứ tự so sánh chuỗi. Ngày sinh/ngày membership dùng DATE. Có thể chuyển sang DATETIME(6) trong migration sau khi chốt phạm vi độ chính xác.
10. Bảng liên kết có surrogate UUID `id` để đơn giản hóa quản trị, kèm unique constraint bảo toàn cặp khóa của v1.1. Credential detail có `credential_id` unique FK, thêm `id` nội bộ.
11. FK, unique, check scope và index nằm trong Flyway V1. JPA `ddl-auto=validate`, không tự tạo/cập nhật schema khi chạy.
12. Challenge và transaction lab dùng database; bản phân tán cần Redis/TTL cleanup hoặc store tương đương. Outbox có worker encrypted payload, lease, backoff và DEAD; SMTP mặc định tắt.

Kiểm tra tự động: `python scripts/check_architecture.py` xác nhận toàn bộ cột của ERD cũ được giữ. Đây không phải khẳng định mọi use case trong tài liệu đã được triển khai; xem bảng coverage.

13. V2 thêm hash token/attempts/consumed cho authentication transaction; V3 thêm attested credential data và hash unique credential ID cho WebAuthn; V4 thêm active_slot unique cho signing key. Entity và catalog được cập nhật tương ứng; không sửa lại V1.
14. Signing private key không lưu DB: production dùng alias PKCS12; local lưu PKCS8 được AES-GCM seal trong `.local/signing-keys/`; test chỉ giữ RAM. File encryption key local cũng cần bảo vệ và backup, không dùng cơ chế lab làm secret custody production.
