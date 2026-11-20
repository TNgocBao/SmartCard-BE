param([switch]$RepairConnection,[PSCredential]$RootCredential)
$ErrorActionPreference='Stop'
if (!$env:JAVA_HOME -or !(Test-Path "$env:JAVA_HOME\bin\java.exe")) { $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11' }
$env:KMA_CREDENTIAL_FILE="$PSScriptRoot\.local\database.credential.xml"
if ((Test-Path $env:KMA_CREDENTIAL_FILE) -and !$RepairConnection) { Write-Host 'KMA IdP Local connection already configured. Use -RepairConnection if MySQL rejects the application login.'; return }
$driver=Get-ChildItem "$PSScriptRoot\target\dependency\mysql-connector-j-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if (!$driver) { $workspaceRepository=Join-Path $PSScriptRoot '.cache\m2\com\mysql\mysql-connector-j'; $driver=Get-ChildItem $workspaceRepository -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1 }
if (!$driver) { $driver=Get-ChildItem "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j" -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1 }
if (!$driver) { throw 'MySQL JDBC driver missing. Build backend first.' }
$credential=$RootCredential
if (!$credential) { $credential=Get-Credential -UserName root -Message 'MySQL localhost:3306 — configure dedicated KMA IdP connection' }
if (!$credential) { throw 'Database setup cancelled' }
try {
    if ($RepairConnection -and (Test-Path $env:KMA_CREDENTIAL_FILE)) {
        $existing=Import-Clixml -LiteralPath $env:KMA_CREDENTIAL_FILE
        $env:KMA_APP_DB_USER=$existing.UserName
        $env:KMA_APP_DB_PASSWORD=$existing.GetNetworkCredential().Password
    }
    $env:KMA_MYSQL_ROOT_PASSWORD=$credential.GetNetworkCredential().Password
    $repairArguments=@();if ($RepairConnection) { $repairArguments=@('--repair') }
    & "$env:JAVA_HOME\bin\java.exe" --class-path $driver.FullName "$PSScriptRoot\tools\InitializeDatabase.java" @repairArguments
    if ($LASTEXITCODE -ne 0) { throw 'MySQL connection setup failed. Check service, root password and MySQL version (8.0.16+ required).' }
} finally {
    Remove-Item Env:\KMA_MYSQL_ROOT_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:\KMA_CREDENTIAL_FILE -ErrorAction SilentlyContinue
    Remove-Item Env:\KMA_APP_DB_USER,Env:\KMA_APP_DB_PASSWORD -ErrorAction SilentlyContinue
    $credential=$null
}
