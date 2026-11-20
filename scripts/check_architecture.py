from pathlib import Path
import re,json
root=Path(__file__).resolve().parents[1]
catalog={x['table']:set(x['columns']) for x in json.loads((root/'src/main/resources/schema-catalog.json').read_text())}
source=(root/'docs/reference/kma_idp_erd.md').read_text(encoding='utf-8')
count=0
for name,body in re.findall(r'^    ([A-Z_]+) \{(.*?)^    \}',source,re.M|re.S):
 fields=set(re.findall(r'^\s+\w+ (\w+)',body,re.M));missing=fields-catalog.get(name.lower(),set())
 assert not missing,(name,missing)
 count+=1
for path in (root/'src/main/java').rglob('*.java'):
 text=path.read_text(encoding='utf-8')
 if path.parent.name in ('controller','service'):
  assert not re.search(r'"(?:SELECT|UPDATE|INSERT|DELETE)\s',text),f'SQL outside repository: {path}'
 if path.parent.name=='controller':assert 'JdbcTemplate' not in text and 'Database db' not in text,path
entities=list((root/'src/main/java').glob('**/entity/*.java'))
assert len(entities)==len(catalog)==41
print(f'PASS: {count} original ERD tables and all their attributes retained; 41 entities; layer boundaries checked.')
