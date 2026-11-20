param([switch]$UseCachedRuntime,[ValidateRange(1,65535)][int]$Port)
$ErrorActionPreference='Stop'
if ($UseCachedRuntime -and $env:SPRING_PROFILES_ACTIVE -match '\bprod\b') { throw 'Cached runtime is for local lab only; production requires the pinned Maven release build.' }
$savedPort=$env:PORT;$savedIssuer=$env:IDP_ISSUER;$savedOrigin=$env:WEBAUTHN_ORIGIN
if ($PSBoundParameters.ContainsKey('Port')) {
    $env:PORT=[string]$Port
    if ($env:SPRING_PROFILES_ACTIVE -notmatch '\bprod\b') {
        if (!$env:IDP_ISSUER) { $env:IDP_ISSUER="http://127.0.0.1:$Port" }
        if (!$env:WEBAUTHN_ORIGIN) { $env:WEBAUTHN_ORIGIN="http://localhost:$Port" }
    }
}
if (!$env:JAVA_HOME -or !(Test-Path "$env:JAVA_HOME\bin\java.exe")) { $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11' }
if ($UseCachedRuntime) {
    & python "$PSScriptRoot\scripts\prepare_cached_runtime.py"
    if ($LASTEXITCODE -ne 0) { throw 'Cached runtime compilation failed. Backend was not started; fix the compiler/dependency error above.' }
}
$credentialFile="$PSScriptRoot\.local\database.credential.xml"
if (!$env:DB_PASSWORD) {
    if (!(Test-Path $credentialFile)) { & "$PSScriptRoot\setup-database.ps1" }
    $credential=Import-Clixml -LiteralPath $credentialFile
    $env:DB_USERNAME=$credential.UserName
    $env:DB_PASSWORD=$credential.GetNetworkCredential().Password
}
if (!$env:BOOTSTRAP_PASSWORD) {
    $secret=Read-Host 'Bootstrap admin password (14–72 characters, needed only for empty database)' -AsSecureString
    $env:BOOTSTRAP_PASSWORD=[PSCredential]::new('admin',$secret).GetNetworkCredential().Password
}
$jar="$PSScriptRoot\target\idp-backend-0.1.0.jar"
if (!$UseCachedRuntime -and !(Test-Path $jar)) { throw 'Build backend first using build.ps1' }
Push-Location $PSScriptRoot
try {
    if ($UseCachedRuntime) {
        $classpathFile=Join-Path $PSScriptRoot 'target\cached-runtime\classpath.txt'
        if (!(Test-Path $classpathFile)) { throw 'Run python scripts/offline_verify.py first. This mode is for local lab only.' }
        $classpath=(Get-Content $classpathFile -Raw).Trim()
        & "$env:JAVA_HOME\bin\java.exe" '-Dorg.springframework.boot.logging.LoggingSystem=none' -cp $classpath vn.kma.idp.IdpApplication
    } else { & "$env:JAVA_HOME\bin\java.exe" -jar $jar }
    if ($LASTEXITCODE -ne 0) { throw 'Backend stopped with errors' }
}
finally {
    Pop-Location
    Remove-Item Env:\DB_PASSWORD,Env:\BOOTSTRAP_PASSWORD -ErrorAction SilentlyContinue
    $env:PORT=$savedPort;$env:IDP_ISSUER=$savedIssuer;$env:WEBAUTHN_ORIGIN=$savedOrigin
}
