# Quy ước lỗi và bằng chứng kiểm thử

Đã thực thi 56 kiểm tra BE độc lập; không thay thế regression nghiệp vụ Spring Boot/MySQL.

| Tình huống | HTTP / code |
|---|---|
| Thiếu/sai tham số, header, DTO hoặc JSON | 400 VALIDATION_ERROR |
| Chưa đăng nhập / phiên bị thu hồi | 401 AUTHENTICATION_REQUIRED / INVALID_OR_REVOKED_SESSION |
| Không có quyền | 403 ACCESS_DENIED |
| Không có route/tài nguyên | 404 RESOURCE_NOT_FOUND |
| Sai method / Content-Type / Accept | 405 METHOD_NOT_ALLOWED / 415 UNSUPPORTED_MEDIA_TYPE / 406 NOT_ACCEPTABLE |
| Trùng dữ liệu / tranh chấp cập nhật | 409 RESOURCE_CONFLICT / CONCURRENT_UPDATE |
| Quá nhiều request | 429 RATE_LIMITED + Retry-After |
| Không kết nối được datasource / không mở transaction | 503 SERVICE_UNAVAILABLE |
| Lỗi nội bộ không dự đoán | 500 INTERNAL_ERROR |

Controller và filter dùng cùng dạng ProblemDetail, `code`, `requestId`; response có X-Request-Id và no-store. Validation chỉ thêm tên trường, không thêm rejected value. Log ứng dụng chỉ ghi requestId và loại exception trong các nhánh xử lý mới, không ghi raw exception message/stack trace.

FE chuyển mã lỗi thành câu ngắn tiếng Việt; lỗi không biết trước không hiện nguyên văn. Các case mạng, TLS, lỗi PIN/APDU, xóa PIN khi validation thất bại, JSON lỗi và requestId độc hại có test riêng.

Bằng chứng: `test-evidence/backend-exceptions-latest.json`; mã: `src/test/java/vn/kma/idp/ExceptionHandlerChecks.java`.
