"""Use only existing local JARs for additional compilation/testing when Maven Central is unavailable.
This is a compatibility check, NOT a replacement for the pinned Maven release build.
"""
from pathlib import Path
import os,re,subprocess,shutil,json,hashlib
from verification_evidence import lock,source_hash,clean_classes,publish
root=Path(__file__).resolve().parents[1]
repo=Path(os.environ['USERPROFILE'])/'.m2/repository'
cache=root/'.cache/offline';cache.mkdir(parents=True,exist_ok=True)
verification_lock=lock(cache/'verification.lock');source_before=source_hash(root)
java=Path(os.environ.get('JAVA_HOME',r'C:\Program Files\Java\jdk-21.0.11'))/'bin'
if not java.exists():java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
def version(path):return tuple(int(x) for x in re.findall(r'\d+',path.parent.name))
artifacts={}
for jar in repo.rglob('*.jar'):
 if any(x in jar.name for x in ['-sources','-javadoc']):continue
 artifact=jar.parent.parent
 preferred=None
 parts=jar.parts
 if 'spring-boot' in '/'.join(parts) and jar.name.startswith('spring-boot'):preferred='3.2.5'
 elif '/org/springframework/' in jar.as_posix() and jar.name.startswith('spring-'):
  preferred='6.1.6' if 'spring-security' not in jar.name and 'spring-data' not in jar.name else '6.2.4' if 'spring-security' in jar.name else '3.2.5'
 if preferred and (artifact/preferred).exists():
  if jar.parent.name!=preferred:continue
 previous=artifacts.get(artifact)
 if not previous or version(jar)>version(previous):artifacts[artifact]=jar
# Avoid duplicate legacy logging bindings and JUnit vintage; run the main harness directly.
jars=[p for p in artifacts.values() if '/org/hibernate/hibernate-core/' not in p.as_posix() and not p.name.startswith(('liquibase','spring-data-redis','spring-data-keyvalue','spring-boot-starter-data-redis','spring-boot-devtools','log4j-slf4j','slf4j-simple','slf4j-log4j','jcl-over-slf4j','commons-logging','junit-vintage'))]
classpath=';'.join(map(str,jars))
def call(args):
 result=subprocess.run([str(a) for a in args],cwd=root)
 if result.returncode:raise SystemExit(result.returncode)
def compile_sources(sources,out,cp):
 clean_classes(root,out)
 args=cache/(out.parent.name+'-'+out.name+'.args')
 args.write_text('--release\n21\n-proc:none\n-encoding\nUTF-8\n-parameters\n-classpath\n"'+cp.replace('\\','/')+'"\n-d\n"'+str(out).replace('\\','/')+'"\n'+'\n'.join('"'+str(p).replace('\\','/')+'"' for p in sources),encoding='utf-8')
 call([java/'javac.exe','@'+str(args)])
out=root/'target/classes'
compile_sources(list((root/'src/main/java').rglob('*.java')),out,classpath)
shutil.copytree(root/'src/main/resources',out,dirs_exist_ok=True)
backend_libs=root/'target/dependency';backend_libs.mkdir(parents=True,exist_ok=True)
for jar in jars:
 if jar.name.startswith('mysql-connector-j-'):shutil.copy2(jar,backend_libs/jar.name)
testout=root/'target/test-classes'
compile_sources([path for path in (root/'src/test/java').rglob('*.java') if not path.name.endswith('Test.java')],testout,classpath+';'+str(root/'target/classes'))
shutil.copytree(root/'src/test/resources',testout,dirs_exist_ok=True)
cp=classpath+';'+str(root/'target/classes')+';'+str(testout)
(cache/'classpath.txt').write_text(classpath,encoding='utf-8')
verification_env=os.environ.copy()
verification_env.update({'OTEL_SDK_DISABLED':'true','DOTNET_CLI_TELEMETRY_OPTOUT':'1','POWERSHELL_TELEMETRY_OPTOUT':'1'})
result=subprocess.run([str(java/'java.exe'),'-Djava.security.manager=allow','-Dorg.springframework.boot.logging.LoggingSystem=none','-Didp.verification-mode=cached-compatibility','-cp',cp,'vn.kma.idp.IntegrationChecks'],cwd=root,env=verification_env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace')
log_path=root/'target/offline-verification.log'
log_path.write_text(result.stdout,encoding='utf-8')
print('\n'.join(result.stdout.splitlines()[-45:]))
if result.returncode or 'INTEGRATION CHECKS PASSED:' not in result.stdout:raise SystemExit(result.returncode or 1)
if source_hash(root)!=source_before:raise SystemExit('Sources changed during verification; rebuild to obtain valid evidence.')
report_path=root/'target/test-results.json';report=json.loads(report_path.read_text(encoding='utf-8'))
report['sourceSha256']=source_before
report['dependencies']=[{'jar':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()} for jar in jars]
report_path.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
publish(root,'backend-latest.json',report)
shutil.copy2(log_path,root/'docs/test-evidence/backend-latest.log')
subprocess.run([os.sys.executable,str(root/'scripts/update_test_guide.py')],cwd=root,check=True)
print('OFFLINE COMPATIBILITY VERIFICATION PASSED (cached framework versions; see README)')
