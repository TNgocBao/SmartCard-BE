"""Run only the failing WebAuthn constructor with a minimal dependency allowlist."""
from pathlib import Path
import subprocess,json,datetime
from verification_evidence import source_hash,publish

root=Path(__file__).resolve().parents[1]
source=source_hash(root)
report=json.loads((root/'docs/test-evidence/cached-runtime-compilation-latest.json').read_text(encoding='utf-8'))
if report['sourceSha256']!=source:raise SystemExit('Prepare the current cached runtime first.')
all_jars=[Path(p) for p in (root/'.cache/offline/classpath.txt').read_text(encoding='utf-8').strip().split(';')]
allowed=['/com/fasterxml/jackson/','/com/webauthn4j/webauthn4j-core/','/com/webauthn4j/webauthn4j-util/','/org/springframework/spring-core/','/org/springframework/spring-jcl/','/org/slf4j/slf4j-api/','/org/springframework/spring-jdbc/','/org/springframework/spring-tx/','/org/springframework/spring-web/','/org/springframework/spring-context/','/org/springframework/spring-beans/']
jars=[p for p in all_jars if any(part in p.as_posix() for part in allowed)]
classes=root/'target/cached-runtime/classes';tests=root/'target/cached-startup-checks';tests.mkdir(parents=True,exist_ok=True)
cp=';'.join(str(p) for p in [classes,*jars]);java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
subprocess.run([str(java/'javac.exe'),'--release','21','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(tests),str(root/'tools/WebAuthnStartupChecks.java')],cwd=root,check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(tests)+';'+cp,'WebAuthnStartupChecks'],cwd=root,check=True)
subprocess.run([str(java/'javac.exe'),'--release','21','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(tests),str(root/'tools/ReadinessChecks.java')],cwd=root,check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(tests)+';'+cp,'ReadinessChecks'],cwd=root,check=True)
if source_hash(root)!=source:raise SystemExit('Source changed during constructor verification.')
publish(root,'webauthn-startup-latest.json',{'recordedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceSha256':source,'passed':4,'scope':'Isolated WebAuthnVerifier constructor, javac-output origin, CBOR roundtrip and production HTTP rejection. No Spring Boot, database, network server or full business suite.','classpathJars':[p.name for p in jars]})
publish(root,'readiness-checks-latest.json',{'recordedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceSha256':source,'passed':3,'scope':'Readiness controller/service with injected repository states: UP, DOWN, database exception; no Spring Boot or DB connection.'})
