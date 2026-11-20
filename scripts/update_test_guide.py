"""Publish the executed finite test inventory, without converting blocked external tests into passes."""
from pathlib import Path
import json,re
from verification_evidence import source_hash
root=Path(__file__).resolve().parents[1]
report=json.loads((root/'target/test-results.json').read_text(encoding='utf-8'))
summary=['## Kết quả kiểm thử tự động gần nhất','',f"Thời điểm UTC: `{report['recordedAt']}`. Chế độ: `{report['mode']}`. Tổng kiểm tra: **{report['totalChecks']}**; số request được ghi riêng: **{len(report['cases'])}**.",'',f"Môi trường: {report['transport']}; {report['database']}. Đây là kết quả của danh mục đã viết, không có nghĩa mọi case có thể tồn tại đều đã kiểm tra.",'','| Case | Request | Expected | Actual | Kết quả |','|---|---|---|---|---|']
if report.get('sourceSha256')!=source_hash(root):
 summary[2:2]=['**KẾT QUẢ LỊCH SỬ:** mã hiện tại đã khác dấu vân tay của lần test này. Không dùng các PASS bên dưới để kết luận phiên bản hiện tại đã qua test. Xem `docs/test-evidence/backend-current-status.json`.','']
for case in report['cases']:
 summary.append(f"| {case['id']} | `{case['method']} {case['path']}` | {case['expected']} | {case['actual']} | {'PASS' if case['passed'] else 'FAIL'} |")
summary+=['','Chưa được đánh dấu PASS bằng kết quả này: MySQL thực, SMTP/TLS/SIEM thực, Oracle Simulator chạy native, thẻ/đầu đọc thật, OIDC conformance và tải/HA/DR. Những phần này cần kết quả chạy độc lập.']
summary.extend(['',f"Dấu vân tay nguồn: `{report.get('sourceSha256','chưa ghi nhận')}`. Báo cáo máy đọc: `docs/test-evidence/backend-latest.json`; log: `docs/test-evidence/backend-latest.log`."])
block='\n'.join(summary)
(root/'docs/TEST_CASE_RESULTS.md').write_text('# Danh mục test đã thực thi\n\n'+block+'\n',encoding='utf-8')
start='<!-- TEST_RESULTS_START -->';end='<!-- TEST_RESULTS_END -->'
section=start+'\n'+block+'\n'+end
guides=[root/'HUONG_DAN_KHOI_DONG_BE.md',root.parent/'HUONG_DAN_KHOI_DONG_BE_FE.md']
for guide in guides:
 if not guide.exists():continue
 text=guide.read_text(encoding='utf-8')
 rendered=section.replace('`docs/','`backend/docs/') if guide.parent==root.parent else section
 if start in text:text=re.sub(re.escape(start)+r'.*?'+re.escape(end),lambda _:rendered,text,flags=re.S)
 else:text+='\n'+rendered+'\n'
 guide.write_text(text,encoding='utf-8')
print('Updated backend test report and available startup guides')
