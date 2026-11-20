# Đối chiếu thuộc tính của ba tài liệu

Đã đối chiếu **897** lần xuất hiện thuộc tính qua ERD, data dictionary và class diagram; thiếu ánh xạ: **0**.

Đây là kiểm tra model, không phải chứng nhận các phương thức/use case đã được triển khai. Các tên tương đương được khai báo trong `MODEL_FIELD_ALIASES.json`; quy đổi trạng thái sang boolean chỉ biểu diễn active/inactive. Membership hiện lưu ngày DATE, chưa biểu diễn thời điểm hiệu lực trong ngày.

| Tài liệu | SHA-256 |
|---|---|
| kma_idp_erd.md | `a9358760a41d37e86e3031c65093b092ab342e3cb6c540face729c379af44450` |
| kma_idp_class_diagram.md | `58dfe7da4ec2847758ab1508cb85bdd037ef2a41da9a4eeb45b86eadfd49d8ef` |
| kma_idp_system_analysis_and_design_v1_1.md | `7cf6d0919a06a4b967a6d97d667185d4dbf603faf9d37befc5125da3e1e89a07` |

Chi tiết từng thuộc tính: `target/requirements-model-audit.json`. Tình trạng nghiệp vụ: `REQUIREMENT_COVERAGE.md`.
