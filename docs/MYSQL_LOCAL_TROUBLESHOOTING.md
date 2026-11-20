# MySQL local cho KMA IdP

## Kết quả chẩn đoán ngày 20/11/2026

- Workbench báo không kết nối được localhost; lỗi JDBC là `Connection refused`, xảy ra trước xác thực mật khẩu.
- PowerShell của người dùng không tìm thấy dịch vụ tên MySQL.
- Docker Desktop hiển thị Engine running, danh sách Containers và Images trống. Chỉ có hai volume cũ `backend_mysql_data` và `exam-app-backend_mysql_data`, đều Not in use.
- Vì vậy, ở thời điểm kiểm tra không có MySQL Server phục vụ các connection local tại cổng 3306. Nhãn connection trong Workbench không khởi tạo server.
- Đã đọc cấu hình MySQL của project tham khảo passwordless để tìm dấu vết hạ tầng cũ. Không chạy/sửa project đó và không gắn volume cũ vào server KMA mới.

## Phương án riêng cho KMA

Script `backend/start-mysql.ps1` chuẩn bị MySQL riêng:

| Thành phần | Giá trị |
|---|---|
| Image | mysql:8.4 |
| Container | atbm-mysql |
| Volume mới | atbm_mysql_data |
| Địa chỉ trên Windows | 127.0.0.1:3306 |
| Schema BE tạo sau setup | kma_idp |

Đây là server mới, dữ liệu ban đầu trống; không phải khôi phục các database cũ. Hai volume cũ được giữ nguyên. Script từ chối ghi đè container không có nhãn sở hữu hoặc dùng lại volume bị tách khỏi container. Nếu có server khác đang nghe cổng 3306, script dừng để kiểm tra.

Mở Docker Desktop. Từ PowerShell của bạn:

```powershell
cd D:\ATBM-SYSTEM
.\backend\start-mysql.ps1
.\backend\run.ps1 -UseCachedRuntime
```

Script đầu hỏi mật khẩu root mới bằng SecureString. Lưu mật khẩu để nhập vào bước setup database của script thứ hai. Mật khẩu không nằm trong tham số dòng lệnh hay file script; Docker vẫn lưu cấu hình biến môi trường container, nên quyền quản trị Docker là quyền nhạy cảm. Cổng publish chỉ bind localhost; không bật Docker API TCP.

`run.ps1` tạo user ứng dụng riêng và schema `kma_idp`; BE dùng user ứng dụng. Workbench có thể dùng nhãn **KMA IdP Local**, host 127.0.0.1, port 3306 và tài khoản root của server mới để quản trị.

## Trạng thái thực thi

Đã kiểm tra cú pháp PowerShell của script: PASS. Chưa chạy tạo container, kiểm tra health hay đăng nhập MySQL: phiên agent bị từ chối truy cập Docker named pipe; hệ thống cũng từ chối yêu cầu chạy CLI với quyền nâng cao. Do đó cần người dùng chạy script bằng phiên PowerShell của mình. Không có kết quả MySQL runtime PASS được ghi nhận ở lượt này.

Không xóa volume, reset Docker, chạy `docker system prune --volumes` hoặc cài chồng server để xử lý lỗi này.

## Container báo thiếu MYSQL_ROOT_PASSWORD sau lần tạo đầu

Log người dùng xác nhận image mysql:8.4 đã tải xong nhưng container báo `Database is uninitialized and password option is not specified`. Phiên bản script ban đầu dùng kế thừa biến môi trường đã không truyền mật khẩu vào container thành công trên máy này.

Đã sửa script dùng `--env-file`: file tạm có ACL riêng cho Windows user hiện tại; kiểm tra ACL có hiệu lực trước khi viết mật khẩu, rồi xóa file trong finally sau khi Docker nhận cấu hình. Không đưa mật khẩu vào argv hoặc log. Docker vẫn lưu biến môi trường trong metadata container, chỉ người có quyền Docker mới nên truy cập.

Chạy riêng lệnh sửa trước:

```powershell
cd D:\ATBM-SYSTEM
.\backend\start-mysql.ps1 -RepairMissingPassword
```

Nhập lại mật khẩu root tối thiểu 14 ký tự (có thể dùng mật khẩu vừa chọn). Chế độ repair chỉ áp dụng cho container KMA có đúng image, volume, port, thiếu biến mật khẩu và có đúng log database chưa khởi tạo. Container lỗi được dừng, đổi tên `atbm-mysql-failed-...` và tắt tự restart; container thay thế gắn lại `atbm_mysql_data`. Không xóa container hay volume. Không dùng chế độ này để đổi mật khẩu của database đã khởi tạo.

Chỉ sau khi có `MySQL is ready at 127.0.0.1:3306` mới chạy:

```powershell
.\backend\run.ps1 -UseCachedRuntime
```

Kiểm thử launcher mới: 8 case giả lập PASS qua `powershell.exe -NoProfile -File backend\scripts\Test-MySqlStartup.ps1`. Bao gồm truyền mật khẩu qua env-file, cleanup khi thành công/thất bại, giữ container lỗi/volume, giữ container đã cấu hình, từ chối container khác, sai log, mật khẩu ngắn và ACL không áp dụng. Docker/password prompt/ACL là fixture; file I/O và cleanup chạy thật với mật khẩu fixture không nhạy cảm. Không phải bằng chứng MySQL runtime PASS. JSON và SHA-256 ba file script nằm ở `docs/test-evidence/mysql-startup-script-latest.json`.

## Access denied từ host Docker sau khi MySQL đã healthy

Lỗi thực tế tiếp theo: tài khoản ứng dụng được tạo cho `localhost`, trong khi MySQL nhận kết nối từ Windows qua bridge Docker là `172.17.0.1` (SQLState 28000, error 1045).

Đã sửa `InitializeDatabase.java` để đọc host thực tế từ `SELECT USER()`, kiểm tra host không chứa wildcard/SQL metacharacter, và tạo account grant đúng host đó. Chỉ cấp quyền migration/DML trên `kma_idp.*` cho user ứng dụng có tiền tố/định dạng riêng; không đổi BE sang root, không cấp host `%`. Setup kiểm tra kết nối ứng dụng bằng `SELECT 1` trước khi lưu credential mới. Repair giữ nguyên credential hiện có, không đổi mật khẩu hoặc xóa schema.

Chạy lại bước sửa nếu gặp lỗi host sau thay đổi hạ tầng:

```powershell
.\backend\setup-database.ps1 -RepairConnection
```

Kết quả thực thi ngày 20/11/2026: đăng nhập JDBC vào MySQL thật bằng user ứng dụng PASS, schema `kma_idp` và host `172.17.0.1`; credential được giữ nguyên. 15 kiểm tra validation thuần JDK PASS. Bằng chứng: `docs/test-evidence/mysql-db-setup-latest.json`. Đây là bằng chứng kết nối DB, không phải BE/Flyway/JPA/API đã khởi động thành công. Lần chạy BE bị duyệt tự động chặn do telemetry tới `dc.services.visualstudio.com`; chưa được người dùng cho phép đích đó.

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
