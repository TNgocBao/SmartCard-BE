"""Compile the local lab runtime separately from IDE output. Never launch Spring Boot."""
from pathlib import Path
import os,subprocess,shutil,json,datetime,zipfile
from verification_evidence import lock,source_hash,clean_classes,publish

root=Path(__file__).resolve().parents[1]
guard=lock(root/'.cache/offline/verification.lock')
before=source_hash(root)
java=Path(os.environ.get('JAVA_HOME',r'C:\Program Files\Java\jdk-21.0.11'))/'bin'
if not (java/'javac.exe').is_file():java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
if not (java/'javac.exe').is_file():raise SystemExit('JDK 21 javac was not found. Set JAVA_HOME to a JDK, not a JRE.')
cp_file=root/'.cache/offline/classpath.txt'
if not cp_file.exists():raise SystemExit('Cached classpath is missing. Prepare dependencies/build the backend first.')
jars=[Path(p.strip()) for p in cp_file.read_text(encoding='utf-8').strip().split(';') if p.strip()]
if not jars or any(not p.is_file() or p.suffix!='.jar' for p in jars):raise SystemExit('Cached classpath contains missing or invalid JAR entries.')
required=['com/fasterxml/jackson/dataformat/cbor/CBORFactory.class','com/fasterxml/jackson/databind/ObjectMapper.class','com/webauthn4j/WebAuthnManager.class']
found=set()
for jar in jars:
    with zipfile.ZipFile(jar) as archive:found.update(set(required).intersection(archive.namelist()))
if set(required)-found:raise SystemExit('Missing cached runtime types: '+', '.join(sorted(set(required)-found)))
runtime=root/'target/cached-runtime';out=runtime/'classes';clean_classes(root,out)
sources=sorted((root/'src/main/java').rglob('*.java'))
argfile=root/'.cache/offline/cached-runtime.args'
classpath=';'.join(str(p) for p in jars)
argfile.write_text('--release\n21\n-proc:none\n-parameters\n-encoding\nUTF-8\n-classpath\n"'+classpath.replace('\\','/')+'"\n-d\n"'+out.as_posix()+'"\n'+'\n'.join('"'+p.as_posix()+'"' for p in sources),encoding='utf-8')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],cwd=root,check=True)
shutil.copytree(root/'src/main/resources',out,dirs_exist_ok=True)
# ECJ may emit problem bytecode into target/classes while javac rejects the same source.
# This launcher exclusively loads a successful javac output, never IDE-generated classes.
if source_hash(root)!=before:raise SystemExit('Sources changed during runtime preparation. Compile again.')
(runtime/'classpath.txt').write_text(str(out)+';'+classpath,encoding='utf-8')
report={'recordedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'PASS_COMPILE_ONLY','sourceSha256':before,'mainFiles':len(sources),'classesDirectory':'target/cached-runtime/classes','dependencyTypesVerified':required,'springBootExecuted':False}
publish(root,'cached-runtime-compilation-latest.json',report)
print('CACHED RUNTIME COMPILED: '+str(len(sources))+' source files; CBOR/WebAuthn dependency types present; IDE target/classes excluded. Spring Boot was not launched.')
