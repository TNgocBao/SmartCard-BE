"""Compile and test SMTP configuration using the existing cached runtime. No mail is sent."""
from pathlib import Path
import os, subprocess
root=Path(__file__).resolve().parents[1]
java=Path(os.environ.get('JAVA_HOME',''))/'bin'
if not (java/'javac.exe').is_file():java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
subprocess.run([os.sys.executable,str(root/'scripts/prepare_cached_runtime.py')],cwd=root,check=True)
classpath=(root/'target/cached-runtime/classpath.txt').read_text(encoding='utf-8').strip()
out=root/'target/smtp-configuration-checks';out.mkdir(parents=True,exist_ok=True)
source=root/'src/test/java/vn/kma/idp/SmtpConfigurationChecks.java'
args=root/'.cache/offline/smtp-configuration.args'
lines=['--release','21','-proc:none','-encoding','UTF-8','-classpath','"'+classpath.replace(chr(92),'/')+'"','-d','"'+out.as_posix()+'"','"'+source.as_posix()+'"']
args.write_text(chr(10).join(lines),encoding='utf-8')
subprocess.run([str(java/'javac.exe'),'@'+str(args)],cwd=root,check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(out)+';'+classpath,'vn.kma.idp.SmtpConfigurationChecks'],cwd=root,check=True)
