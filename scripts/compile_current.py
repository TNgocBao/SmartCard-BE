"""Compile current BE and test sources with cached dependencies; never execute application or business tests."""
from pathlib import Path
import os,subprocess,json,datetime
from verification_evidence import lock,source_hash,clean_classes,publish

root=Path(__file__).resolve().parents[1]
guard=lock(root/'.cache/offline/verification.lock')
before=source_hash(root)
cp=(root/'.cache/offline/classpath.txt').read_text(encoding='utf-8')
java=Path(r'C:\Program Files\Java\jdk-21.0.11\bin')
def compile_files(files,out,classpath,label):
    clean_classes(root,out)
    args=root/'.cache/offline'/('static-'+label+'.args')
    args.write_text('--release\n21\n-proc:none\n-parameters\n-encoding\nUTF-8\n-classpath\n"'+classpath.replace('\\','/')+'"\n-d\n"'+out.as_posix()+'"\n'+'\n'.join('"'+file.as_posix()+'"' for file in files),encoding='utf-8')
    subprocess.run([str(java/'javac.exe'),'@'+str(args)],cwd=root,check=True)
main=list((root/'src/main/java').rglob('*.java'));tests=[p for p in (root/'src/test/java').rglob('*.java') if not p.name.endswith('Test.java')]
compile_files(main,root/'target/classes',cp,'main')
compile_files(tests,root/'target/test-classes',cp+';'+str(root/'target/classes'),'tests')
# Resources are copied for the ordinary cached launcher, without launching it here.
import shutil
shutil.copytree(root/'src/main/resources',root/'target/classes',dirs_exist_ok=True)
shutil.copytree(root/'src/test/resources',root/'target/test-classes',dirs_exist_ok=True)
if source_hash(root)!=before:raise SystemExit('Sources changed during compilation')
report={'recordedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'PASS_COMPILE_ONLY','sourceSha256':before,'mainFiles':len(main),'standaloneTestFiles':len(tests),'businessTestsExecuted':False}
(root/'target/static-compilation.json').write_text(json.dumps(report,indent=2),encoding='utf-8');publish(root,'backend-compilation-latest.json',report)
print('CURRENT STATIC COMPILATION PASSED: '+str(len(main))+' main files, '+str(len(tests))+' standalone test files. No business tests executed.')
