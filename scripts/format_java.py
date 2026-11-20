"""Small whitespace formatter for this starter's Java subset (no text blocks)."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
def format_source(source):
 lines=[];line='';indent=0;parens=0;state=None;escape=False;i=0
 def flush():
  nonlocal line
  if line.strip():lines.append('    '*max(indent,0)+line.strip())
  line=''
 while i<len(source):
  ch=source[i];nextch=source[i+1] if i+1<len(source) else ''
  if state in ('"',"'"):
   line+=ch
   if escape:escape=False
   elif ch=='\\':escape=True
   elif ch==state:state=None
  elif state=='//':
   if ch=='\n':state=None;flush()
   else:line+=ch
  elif state=='/*':
   line+=ch
   if ch=='*' and nextch=='/':line+='/';i+=1;state=None
   elif ch=='\n':flush()
  elif ch=='/' and nextch in ('/','*'):line+=ch+nextch;state='// ' .strip() if nextch=='/' else '/*';i+=1
  elif ch in ('"',"'"):state=ch;line+=ch
  elif ch=='(':parens+=1;line+=ch
  elif ch==')':parens-=1;line+=ch
  elif ch=='{':line+=ch;flush();indent+=1
  elif ch=='}':flush();indent-=1;line='}';flush()
  elif ch==';':line+=ch;flush() if parens==0 else None
  elif ch=='\n':flush()
  elif ch=='\r':pass
  else:line+=ch
  i+=1
 flush();return '\n'.join(lines)+'\n'
for folder in ('src','tools'):
 for file in (root/folder).rglob('*.java'):
  file.write_text(format_source(file.read_text(encoding='utf-8')),encoding='utf-8')
print('Java whitespace normalized')
