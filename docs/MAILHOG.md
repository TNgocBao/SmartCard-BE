# MailHog cho OTP và email kích hoạt

MailHog là hộp thư thử nghiệm cục bộ: BE gửi SMTP vào đây, sau đó mở web để đọc mã. Thư được giữ trong MailHog, không gửi đến hộp thư thật của người nhận.

## Khởi động

Mở Docker Desktop, đảm bảo MySQL đang chạy. Dừng BE cũ bằng Ctrl+C trước khi chạy BE trên cùng cổng.

Từ PowerShell của bạn:

```powershell
cd D:\ATBM-SYSTEM
.\backend\run-mailhog.ps1 -Port 18080
```

Script tự khởi động container `atbm-mailhog`, chờ hộp thư sẵn sàng, rồi compile/chạy BE bằng cached runtime với profile `local,mailhog`. BE có thể hỏi mật khẩu database/bootstrap như trước. Cấu hình môi trường MFA đã đặt trong terminal vẫn được giữ nguyên.

Muốn dùng JAR release đã build lại: `run-mailhog.ps1 -Release -Port 18080`. Phải build JAR sau khi cập nhật SMTP adapter/profile; không dùng JAR cũ.

| Thành phần | Địa chỉ |
|---|---|
| Hộp thư MailHog | http://127.0.0.1:8025 |
| SMTP của BE | 127.0.0.1:1025 |
| BE | http://127.0.0.1:18080 |
| FE đang build | http://127.0.0.1:8787 |

Nếu FE chưa chạy, mở terminal khác:

```powershell
cd D:\ATBM-SYSTEM\frontend
.\run.ps1 -ApiBaseUrl http://127.0.0.1:18080
```

FE đang chạy với đúng địa chỉ BE thì không cần restart/build lại.

## Thử OTP

1. Dùng tài khoản ACTIVE, không bị khóa và có email trong hồ sơ. Trong lab, có thể dùng `sv001@example.test`; MailHog sẽ nhận thư cho địa chỉ đó.
2. Tại màn hình đăng nhập FE, chọn **Email OTP**, nhập username, bấm **Gửi mã xác thực**.
3. Mở http://127.0.0.1:8025, tìm thư có người nhận đúng email. Tiêu đề OTP là `KMA IdP — one-time sign-in code`.
4. Lấy mã 6 số và nhập vào FE. OTP hiện hết hạn sau 120 giây, chỉ sử dụng một lần. Outbox kiểm tra thư chờ mỗi giây.
5. Nếu tài khoản có TOTP, web sẽ yêu cầu tiếp mã từ ứng dụng Authenticator. MailHog cung cấp email OTP; không thay thế mã TOTP của Authenticator.

Để kích hoạt sinh viên mới: admin gửi lời mời tại Tài khoản; mở thư `KMA IdP — activation` trong MailHog, lấy token và dùng ở **Kích hoạt tài khoản**. Hoàn tất đăng ký Authenticator trước khi thử đăng nhập OTP cho tài khoản còn PENDING_ACTIVATION.

## Cấu hình đã tích hợp

`application-mailhog.yml` bật outbox, cấu hình SMTP local cổng 1025 và tắt SMTP AUTH/STARTTLS cho MailHog. Cấu hình thông thường vẫn mặc định bật AUTH/TLS; profile prod từ chối cấu hình bỏ các điều kiện này.

Chỉ khởi động MailHog (chưa chạy BE):

```powershell
.\backend\start-mailhog.ps1
```

Hoặc sau khi MailHog đã chạy, bật profile thủ công trong terminal BE:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local,mailhog'
.\backend\run.ps1 -UseCachedRuntime -Port 18080
```

Các cổng Docker chỉ bind 127.0.0.1. Hộp thư dùng bộ nhớ của MailHog; restart container có thể mất email. Ctrl+C tại terminal chỉ dừng BE; MailHog tiếp tục chạy.

Dừng riêng MailHog khi cần:

```powershell
& 'C:\Program Files\Docker\Docker\resources\bin\docker.exe' stop atbm-mailhog
```

## Chẩn đoán

| Hiện tượng | Cách kiểm tra |
|---|---|
| Access is denied / không truy cập Docker Engine | Mở Docker Desktop; chạy script từ PowerShell của bạn, bên ngoài môi trường giới hạn quyền |
| Docker CLI không có trong PATH | Script tự tìm đường dẫn Docker Desktop chuẩn trên Windows |
| Cổng 1025/8025 bị chiếm | Kiểm tra dịch vụ đang dùng cổng, không tự xóa container khác |
| Có OTP transaction nhưng không có email | Xác nhận BE khởi động với profile local,mailhog; tài khoản ACTIVE, không khóa và có email |
| Thư đến nhưng mã hết hạn | Yêu cầu OTP mới rồi nhập ngay; không dùng mã từ thư cũ |
| OTP xong vẫn hỏi mã nữa | Tài khoản đã có MFA/TOTP; nhập mã Authenticator ở bước tiếp theo |

Kiểm tra cấu hình không gửi email: `python backend/scripts/check_smtp_configuration.py` từ gốc workspace. Docker/SMTP thực tế cần kiểm tra riêng sau khi MailHog chạy.

Tài liệu MailHog chính thức: https://github.com/mailhog/MailHog/blob/master/docs/DEPLOY.md
