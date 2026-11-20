"""Local running-server smoke checks. Reports retain no password, token or response body."""
import argparse,datetime,getpass,json,urllib.request,urllib.error
from pathlib import Path
from verification_evidence import source_hash,publish

parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=18080);parser.add_argument('--check-admin',action='store_true');args=parser.parse_args()
if not 1<=args.port<=65535:raise SystemExit('Invalid port')
root=Path(__file__).resolve().parents[1];base=f'http://127.0.0.1:{args.port}'
opener=urllib.request.build_opener(urllib.request.ProxyHandler({}));cases=[]
def call(path,method='GET',payload=None,headers=None):
    headers={'Accept':'application/json',**(headers or {})}
    if payload is not None:headers['Content-Type']='application/json'
    request=urllib.request.Request(base+path,data=None if payload is None else json.dumps(payload).encode(),method=method,headers=headers)
    try:response=opener.open(request,timeout=10)
    except urllib.error.HTTPError as failure:response=failure
    with response:return response.status,json.load(response),dict(response.headers)
def check(name,passed,status):
    cases.append({'case':name,'passed':bool(passed),'httpStatus':status})
    print(('PASS' if passed else 'FAIL')+' '+name+' HTTP '+str(status))
    if not passed:raise RuntimeError('Local smoke check failed: '+name)
try:
    status,data,headers=call('/api/v1/status');check('Status identifies KMA service as UP',status==200 and data.get('status')=='UP' and data.get('service')=='KMA Identity',status)
    status,data,headers=call('/api/v1/health');check('Database readiness with actual MySQL',status==200 and data.get('status')=='UP',status)
    status,data,headers=call('/api/v1/me');check('Anonymous profile access is denied',status==401 and data.get('code')=='AUTHENTICATION_REQUIRED',status)
    safe_headers={k.lower():v for k,v in headers.items()};check('Authentication error has request ID and no-store',bool(data.get('requestId')) and bool(safe_headers.get('x-request-id')) and 'no-store' in safe_headers.get('cache-control',''),status)
    status,data,headers=call('/oauth2/jwks');keys=data.get('keys',[]);check('JWKS exposes public keys without RSA private parameters',status==200 and bool(keys) and all(not set(key).intersection({'d','p','q','dp','dq','qi','oth'}) for key in keys),status)
    status,data,headers=call('/api/v1/auth/password','POST',{});check('Missing login fields produce safe validation response',status==400 and bool(data.get('code')) and bool(data.get('requestId')),status)
    status,data,headers=call('/api/v1/me',headers={'Authorization':'Bearer invalid-fixture'});check('Malformed bearer token is rejected',status==401 and bool(data.get('code')),status)
    if args.check_admin:
        password=getpass.getpass('KMA admin password (secure input): ')
        try:status,data,headers=call('/api/v1/auth/password','POST',{'username':'admin','password':password})
        finally:password=None
        mode='ENROLLMENT_REQUIRED' if data.get('requiresEnrollment') else 'MFA_REQUIRED' if data.get('requiresMfa') else 'SESSION_ISSUED' if data.get('accessToken') and data.get('refreshToken') else 'REJECTED'
        check('Admin password reaches the expected authentication step: '+mode,status==200 and mode!='REJECTED',status)
        data=None
finally:
    publish(root,'local-http-smoke-latest.json',{'recordedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceSha256':source_hash(root),'baseUrl':base,'scope':'HTTP against running cached Spring Boot backend and actual local MySQL; public endpoints, auth rejection, validation and optional password first step. No full business regression or card test.','cases':cases})
