# KMA IdP Backend

Repository độc lập cho API Spring Boot / MySQL. Không cần mã nguồn frontend để build hoặc test.

## Chạy

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
.\build.ps1
.\setup-database.ps1
.\run.ps1
```

Nếu Maven Central chưa truy cập được nhưng máy có cache phù hợp:

```powershell
python .\scripts\check_architecture.py
python .\scripts\offline_verify.py
.\setup-database.ps1
.\run.ps1 -UseCachedRuntime
```

Cached runtime chỉ dành cho lab, không dùng production. API status: `GET /api/v1/status`.

## Tổ chức

- `src/main/java/vn/kma/idp/<module>/controller`: HTTP và DTO validation.
- `dto`: dữ liệu request/response.
- `service`: nghiệp vụ, transaction và invariants.
- `repository`: JPA và JDBC aggregate queries.
- `entity`: ánh xạ schema.
- `src/main/resources/db/migration`: Flyway, không sửa migration đã áp dụng.
- `scripts`: công cụ build/kiểm tra riêng của backend.
- `docs/reference`: bản sao ba tài liệu yêu cầu, để repo tự kiểm tra schema độc lập.

Thông tin môi trường nằm trong `.env.example`; không tự nạp file này. Secrets được cung cấp bằng môi trường hoặc lời nhắc cục bộ. Cache, output build và credential DPAPI không được commit.

Hướng dẫn chi tiết: [HUONG_DAN_KHOI_DONG_BE.md](HUONG_DAN_KHOI_DONG_BE.md). Phạm vi và tiến độ: [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md). Kết quả case: [docs/TEST_CASE_RESULTS.md](docs/TEST_CASE_RESULTS.md).

## MailHog để nhận OTP trong môi trường local

Mở Docker Desktop, dừng BE cũ rồi chạy từ gốc workspace:

```powershell
.\backend\run-mailhog.ps1 -Port 18080
```

Hộp thư: http://127.0.0.1:8025. Script khởi động MailHog và BE với SMTP/outbox đã cấu hình. Xem [docs/MAILHOG.md](docs/MAILHOG.md) cho cách đăng nhập OTP và kích hoạt tài khoản.
