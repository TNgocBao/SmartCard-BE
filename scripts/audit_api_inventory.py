"""List the actual HTTP surface without equating generic CRUD to the SRS protocol contract."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
rows=[]
for path in sorted((root/'src/main/java').rglob('*Controller.java')):
 text=path.read_text(encoding='utf-8')
 prefix=re.search(r'@RequestMapping\("([^"]+)"\)',text)
 prefix=prefix.group(1) if prefix else ''
 for method,suffix in re.findall(r'@(Get|Post|Patch|Delete|Put)Mapping(?:\("([^"]*)"\))?',text):
  rows.append((method.upper(),prefix+suffix,path.stem))
doc=['# API thực tế đang triển khai','','Được trích từ controller; endpoint tồn tại không chứng minh toàn bộ business rule của SRS đã được đáp ứng. Các API `/management/resources/*` là API lab, không thay thế contract OAuth/OIDC hay Management API riêng trong tài liệu v1.1.','','| Method | Path | Controller |','|---|---|---|']
doc += [f'| {m} | `{p}` | `{c}` |' for m,p,c in rows]
doc += ['','Chưa có Authorization Code/PKCE, discovery, userinfo, introspection và consent workflow. Có activation, recovery, MFA và signing-key lifecycle như danh sách trên. `/oauth2/jwks` chỉ xuất public key; không có nghĩa toàn bộ OAuth2/OIDC đã triển khai.']
(root/'docs/API_IMPLEMENTATION_INVENTORY.md').write_text('\n'.join(doc)+'\n',encoding='utf-8')
print(f'API inventory: {len(rows)} implemented routes')
