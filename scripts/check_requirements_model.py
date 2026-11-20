"""Audit attributes from all three supplied documents, with explicit reviewed naming aliases."""
from pathlib import Path
import re,json,hashlib

root=Path(__file__).resolve().parents[1]
catalog={entry['table']:set(entry['columns']) for entry in json.loads((root/'src/main/resources/schema-catalog.json').read_text(encoding='utf-8'))}
aliases=json.loads((root/'docs/MODEL_FIELD_ALIASES.json').read_text(encoding='utf-8'))
def snake(value):return re.sub(r'([a-z0-9])([A-Z])',r'\1_\2',re.sub(r'(.)([A-Z][a-z]+)',r'\1_\2',value)).lower()
checks=[];sources=[]
def record(source,table,field,section):
    column=aliases.get(table,{}).get(field,field)
    checks.append({'source':source,'section':section,'table':table,'attribute':field,'column':column,'present':column in catalog.get(table,set())})
for name in ['kma_idp_erd.md','kma_idp_class_diagram.md','kma_idp_system_analysis_and_design_v1_1.md']:
    file=root/'docs/reference'/name;text=file.read_text(encoding='utf-8')
    sources.append({'file':name,'sha256':hashlib.sha256(file.read_bytes()).hexdigest()})
    for table,body in re.findall(r'^    ([A-Z_]+) \{(.*?)^    \}',text,re.M|re.S):
        for field in re.findall(r'^\s+\w+ (\w+)',body,re.M):record(name,table.lower(),field,'ERD')
    for table,body in re.findall(r'^\| `([A-Z_]+)` \| (.+?) \|$',text,re.M):
        for field in set(re.findall(r'`([a-z][a-z_0-9]*)`',body)):record(name,table.lower(),field,'Data dictionary')
    for cls,body in re.findall(r'^\s*class (\w+) \{(.*?)^\s*\}',text,re.M|re.S):
        table=snake(cls)
        if table not in catalog:continue
        for field in re.findall(r'^\s*\+[\w<>]+\s+(\w+)\s*$',body,re.M):record(name,table,snake(field),'Class attributes')
missing=[check for check in checks if not check['present']]
report={'scope':'Attributes only; does not certify methods, use cases, constraints or runtime behavior','sources':sources,'attributeChecks':len(checks),'missing':missing,'checks':checks}
target=root/'target';target.mkdir(parents=True,exist_ok=True)
(target/'requirements-model-audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# Đối chiếu thuộc tính của ba tài liệu','',f"Đã đối chiếu **{len(checks)}** lần xuất hiện thuộc tính qua ERD, data dictionary và class diagram; thiếu ánh xạ: **{len(missing)}**.",'',
       'Đây là kiểm tra model, không phải chứng nhận các phương thức/use case đã được triển khai. Các tên tương đương được khai báo trong `MODEL_FIELD_ALIASES.json`; quy đổi trạng thái sang boolean chỉ biểu diễn active/inactive. Membership hiện lưu ngày DATE, chưa biểu diễn thời điểm hiệu lực trong ngày.','',
       '| Tài liệu | SHA-256 |','|---|---|']
lines.extend(f"| {item['file']} | `{item['sha256']}` |" for item in sources)
lines.extend(['','Chi tiết từng thuộc tính: `target/requirements-model-audit.json`. Tình trạng nghiệp vụ: `REQUIREMENT_COVERAGE.md`.'])
(root/'docs/MODEL_ATTRIBUTE_COVERAGE.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
if missing:
    for item in missing:print('MISSING',item['source'],item['table'],item['attribute'])
    raise SystemExit(1)
print(f'PASS: {len(checks)} attribute occurrences mapped across all three documents; methods and business cases tracked separately.')
