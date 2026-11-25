# Các case mới chờ chạy BE

Trạng thái cập nhật 25/11/2026: bộ regression hiện tại đã PASS 291 kiểm tra (cached compatibility, H2/MockMvc). Bảng bên dưới là danh mục đã từng chờ kiểm chứng; xem TEST_CASE_RESULTS.md và frontend/docs/test-evidence/card-demo-contract-latest.json để biết case thực thi. Các kết quả này không chứng minh thiết bị thật, MySQL, HTTP/trình duyệt hoặc Maven release build.

| Nhóm | Case cần xác minh | Kết quả kỳ vọng |
|---|---|---|
| RBAC | Catalog chỉ liệt kê quyền có enforcement | Không có permission tự khai báo chưa được backend kiểm tra |
| RBAC | Tạo/trùng role, phân trang quá giới hạn, role hệ thống | 201 / 409 / 400 / 409 |
| RBAC | Thay tập permission, trùng permission ID | Thành công / 400 |
| RBAC | Delegate tự cấp idp.manage hoặc SUPER_ADMIN | 403 |
| RBAC | Delegate sửa role có quyền cao hơn mình | 403 |
| RBAC | Assignment bị trùng/chồng thời gian, sai khoảng hiệu lực, hai scope | 409 / 400 / 400 |
| RBAC | Quyền thay đổi khi JWT cũ còn hạn | Enforcement theo database ở request tiếp theo |
| Scope | Quyền organization không thành global hoặc application | Không vượt scope |
| Scope | SUPER_ADMIN future-dated không được tính là quản trị viên đang hoạt động | Vẫn bảo vệ Superuser thực tế cuối cùng |
| Credential | Delegate thu hồi credential của tài khoản đặc quyền | 403 |
| WebAuthn | Counter 0 ở hai ceremony hợp lệ riêng biệt | Được chấp nhận; replay vẫn bị từ chối |
| Outbox | Worker cũ ACK/fail sau khi lease được nhận lại | Không cập nhật claim mới; chỉ attempt hiện tại được ACK |
| FE–BE | Cấp/gỡ key.read với JWT hiện tại; thử nâng quyền key.rotate | Quyền thay đổi tức thì; hành vi vượt quyền bị 403 |

Mã: `src/test/java/vn/kma/idp/RbacFlowChecks.java`, `IntegrationChecks.java`; tích hợp tùy chọn tại repository frontend `integration-tests/ContractFlowChecks.java`.

Frontend standalone đã có báo cáo mới cho 25 kiểm tra API và smoke giao diện. Điều đó không thay thế chạy lại bộ tích hợp FE–BE.
