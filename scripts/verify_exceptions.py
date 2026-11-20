"""Standalone error-contract checks: explicit small classpath, no Boot/DB/mail runtime, sockets denied by Java 21 test guard."""
from pathlib import Path
import os,subprocess,json,re
from verification_evidence import lock,source_hash,publish

root=Path(__file__).resolve().parents[1]
guard=lock(root/'.cache/offline/verification.lock')
before=source_hash(root)
cached=Path(os.environ['USERPROFILE'])/'.m2/repository'
java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
artifacts={
 'org/springframework':'spring-aop spring-beans spring-context spring-core spring-expression spring-jcl spring-web spring-webmvc spring-test spring-tx',
 'org/springframework/security':'spring-security-core',
 'com/fasterxml/jackson/core':'jackson-annotations jackson-core jackson-databind',
 'io/micrometer':'micrometer-commons micrometer-observation',
 'jakarta/validation':'jakarta.validation-api',
 'org/hibernate/validator':'hibernate-validator',
 'org/jboss/logging':'jboss-logging',
 'com/fasterxml':'classmate',
 'org/apache/tomcat/embed':'tomcat-embed-core',
 'org/slf4j':'slf4j-api',
}
jars=[]
for group,names in artifacts.items():
 for name in names.split():
  candidates=[p for p in (cached/group/name).rglob('*.jar') if not any(x in p.name for x in ['-sources','-javadoc'])]
  preferred='6.1.6' if group=='org/springframework' else '6.2.4' if group=='org/springframework/security' else None
  if preferred:candidates=[p for p in candidates if p.parent.name==preferred]
  if not candidates:raise SystemExit('Missing isolated-test dependency: '+name)
  jars.append(max(candidates,key=lambda p:tuple(map(int,re.findall(r'\d+',p.parent.name)))))
out=root/'target/exception-check-classes';out.mkdir(parents=True,exist_ok=True)
cp=';'.join(map(str,jars));base=root/'src/main/java/vn/kma/idp'
sources=[base/'shared/ApiException.java',base/'shared/ErrorResponses.java',base/'shared/Errors.java',base/'security/RequestTraceFilter.java',base/'security/RateLimitFilter.java',root/'src/test/java/vn/kma/idp/NetworkIsolation.java',root/'src/test/java/vn/kma/idp/ExceptionHandlerChecks.java']
args=root/'.cache/offline/exception-check.args'
args.write_text('--release\n21\n-proc:none\n-parameters\n-encoding\nUTF-8\n-classpath\n"'+cp.replace('\\','/')+'"\n-d\n"'+out.as_posix()+'"\n'+'\n'.join('"'+p.as_posix()+'"' for p in sources),encoding='utf-8')
subprocess.run([str(java/'javac.exe'),'@'+str(args)],cwd=root,check=True)
env=os.environ.copy();env.update({'OTEL_SDK_DISABLED':'true','DOTNET_CLI_TELEMETRY_OPTOUT':'1','POWERSHELL_TELEMETRY_OPTOUT':'1'})
result=subprocess.run([str(java/'java.exe'),'-Djava.security.manager=allow','-cp',cp+';'+str(out),'vn.kma.idp.ExceptionHandlerChecks'],cwd=root,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace')
(root/'target/exception-handler-verification.log').write_text(result.stdout,encoding='utf-8');print('\n'.join(result.stdout.splitlines()[-16:]))
if result.returncode or 'ISOLATED EXCEPTION CHECKS PASSED:' not in result.stdout:raise SystemExit(result.returncode or 1)
if source_hash(root)!=before:raise SystemExit('Sources changed; rerun isolated checks')
file=root/'target/exception-handler-results.json';report=json.loads(file.read_text(encoding='utf-8'));report['sourceSha256']=before;report['dependencies']=[jar.name for jar in jars]
file.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');publish(root,'backend-exceptions-latest.json',report)
print('Standalone error contract verified. Full BE business regression remains a separate, previously blocked action.')
