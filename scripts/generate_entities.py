"""Generate explicit JPA entity/repository pairs from the reviewed schema definition."""
from pathlib import Path
import re
from generate_schema import models, required, refs
root=Path(__file__).resolve().parents[1]/'target/schema-draft/java/vn/kma/idp'
modules={
 'identity':['organization','person','identity','org_membership','external_identity','org_role','org_role_assignment'],
 'account':['account','account_activation_token','recovery_code'],
 'credential':['credential','webauthn_credential','webauthn_challenge','totp_credential','smart_card_credential','smart_card_challenge'],
 'rbac':['role','permission','role_permission','account_role_assignment'],
 'client':['application','client_redirect_uri','client_logout_uri','client_credential','oidc_scope','application_scope','client_grant_type','signing_key'],
 'session':['idp_session','session_client','refresh_token','refresh_token_scope','user_consent','user_consent_scope'],
 'auth':['authentication_event','authorization_code','authorization_code_scope','authentication_transaction'],
 'policy':['security_policy'], 'audit':['audit_log','outbox_event']}
def pascal(x):return ''.join(v.capitalize() for v in x.split('_'))
def camel(x):p=pascal(x);return p[0].lower()+p[1:]
for module,tables in modules.items():
 for table in tables:
  name=pascal(table);entity_dir=root/module/'entity';repo_dir=root/module/'repository'
  entity_dir.mkdir(parents=True,exist_ok=True);repo_dir.mkdir(parents=True,exist_ok=True)
  lines=[f'package vn.kma.idp.{module}.entity;','', 'import jakarta.persistence.*;','',f'@Entity',f'@Table(name = "{table}")',f'public class {name} {{','    @Id','    @Column(length = 36, nullable = false)','    private String id;']
  fields=[('id','String')]
  for token in models[table].split():
   col=token.split(':')[0];kind=token.split(':')[1] if ':' in token else 'S';typ={'I':'Integer','L':'Long','B':'Boolean','D':'java.time.LocalDate'}.get(kind,'String');field=camel(col)
   attributes=[f'name = "{col}"']
   if col in required.get(table,set()):attributes+=['nullable = false']
   if kind=='T':attributes+=['columnDefinition = "TEXT"']
   elif col in refs or col=='parent_id':attributes+=['length = 36']
   elif kind!='D' and (col.endswith('_at') or col in ('valid_from','valid_until','locked_until','not_before')):attributes+=['length = 40']
   lines += ['    @Column('+', '.join(attributes)+')',f'    private {typ} {field};']
   fields.append((field,typ))
  lines += ['',f'    public {name}() {{}}']
  for field,typ in fields:
   cap=field[0].upper()+field[1:];lines += [f'    public {typ} get{cap}() {{ return {field}; }}',f'    public void set{cap}({typ} value) {{ this.{field} = value; }}']
  lines+=['}'];(entity_dir/(name+'.java')).write_text('\n'.join(lines)+'\n',encoding='utf-8')
  repo_path=repo_dir/(name+'Repository.java')
  if not repo_path.exists(): repo_path.write_text(f'''package vn.kma.idp.{module}.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.{module}.entity.{name};

public interface {name}Repository extends JpaRepository<{name}, String> {{}}
''',encoding='utf-8')
print('Generated 41 JPA entities and 41 repositories')
