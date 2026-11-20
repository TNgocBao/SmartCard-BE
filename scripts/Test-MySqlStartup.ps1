# Deterministic launcher regression. Docker, password prompt and ACL boundary are simulated; no containers/network.
$ErrorActionPreference='Stop'
$entry=Join-Path (Split-Path $PSScriptRoot -Parent) 'start-mysql.ps1'
$results=[System.Collections.Generic.List[object]]::new()
$global:fixturePassword='LocalFixtureOnly!123$#'
function global:Read-Host { ConvertTo-SecureString $global:fixturePassword -AsPlainText -Force }
function global:Get-Acl {
    param([string]$LiteralPath)
    if ($global:scenario -eq 'acl-fails' -or !$global:fixtureAcl) { return [System.Security.AccessControl.FileSecurity]::new() }
    return $global:fixtureAcl
}
function global:Set-Acl { param([string]$LiteralPath,$AclObject) $global:fixtureAcl=$AclObject }
function global:docker {
    $a=@($args);$global:LASTEXITCODE=0
    $global:dockerCalls.Add(($a -join ' '))
    if (($a -join ' ').Contains($global:fixturePassword)) { throw 'Secret appeared in Docker arguments.' }
    switch ($a[0]) {
        info { '8.4-fixture' }
        container { 'atbm-mysql' }
        volume { }
        inspect {
            if ($a[2] -eq '{{.State.Health.Status}}') { 'healthy';return }
            $owner=if ($global:scenario -eq 'unrelated') { 'false' } else { 'true' }
            $envValues=if ($global:scenario -eq 'existing-ok') { @('MYSQL_ROOT_PASSWORD=fixture-existing') } else { @() }
            @{Config=@{Labels=@{'vn.kma.idp.localdb'=$owner};Image='mysql:8.4';Env=$envValues};Mounts=@(@{Type='volume';Name='atbm_mysql_data';Destination='/var/lib/mysql'});HostConfig=@{PortBindings=@{'3306/tcp'=@(@{HostIp='127.0.0.1';HostPort='3306'})}}} | ConvertTo-Json -Depth 8 -Compress
        }
        logs { if ($global:scenario -eq 'wrong-error') { 'another error' } else { 'Database is uninitialized and password option is not specified' } }
        run {
            $index=[Array]::IndexOf($a,'--env-file');if ($index -lt 0) { throw 'Missing explicit env-file.' }
            $global:environmentFile=$a[$index+1]
            $value=[System.IO.File]::ReadAllText($global:environmentFile)
            if (!$value.StartsWith('MYSQL_ROOT_PASSWORD='+$global:fixturePassword+"`n")) { throw 'Password not passed through the environment file.' }
            $acl=Get-Acl -LiteralPath $global:environmentFile
            if (!$acl.AreAccessRulesProtected) { throw 'Secret file ACL inherits permissions.' }
            if ($global:scenario -eq 'create-fails') { $global:LASTEXITCODE=1 } else { 'fixture-container-id' }
        }
        default { 'fixture-operation-ok' }
    }
}
function Check-Launcher {
    param([string]$Name,[string]$Scenario,[switch]$Repair,[switch]$ShouldFail,[string]$ExpectedMessage='')
    $global:scenario=$Scenario;$global:environmentFile=$null;$global:fixtureAcl=$null;$global:dockerCalls=[System.Collections.Generic.List[string]]::new()
    $failed=$false;$failureMessage=''
    try { if ($Repair) { & $entry -RepairMissingPassword | Out-Null } else { & $entry | Out-Null } }
    catch { $failed=$true;$failureMessage=$_.Exception.Message }
    $pass=$failed -eq [bool]$ShouldFail
    if ($ExpectedMessage) { $pass=$pass -and $failureMessage.Contains($ExpectedMessage) }
    if ($global:environmentFile) { $pass=$pass -and !(Test-Path -LiteralPath $global:environmentFile) }
    $pass=$pass -and !(@($global:dockerCalls | Where-Object { $_ -match '(^rm |^volume rm|^system prune)' }).Count)
    if ($Scenario -eq 'repair') { $pass=$pass -and @($global:dockerCalls | Where-Object { $_ -match '^rename atbm-mysql atbm-mysql-failed-' }).Count -eq 1 -and @($global:dockerCalls | Where-Object { $_ -match '^update --restart=no atbm-mysql-failed-' }).Count -eq 1 }
    if ($Scenario -eq 'existing-ok') { $pass=$pass -and !(@($global:dockerCalls | Where-Object { $_ -match '^run |^rename |^stop ' }).Count) }
    if ($Scenario -in @('missing','unrelated','wrong-error','short-password','acl-fails')) { $pass=$pass -and !(@($global:dockerCalls | Where-Object { $_ -match '^run |^rename |^stop |^update ' }).Count) }
    $results.Add(@{case=$Name;passed=$pass})
    if (!$pass) { throw "Launcher check failed: $Name ($failureMessage)" }
}
try {
    Check-Launcher 'Missing password is diagnosed without changing container' 'missing' -ShouldFail -ExpectedMessage 'missing MYSQL_ROOT_PASSWORD'
    Check-Launcher 'Repair preserves failed container and volume while passing password explicitly' 'repair' -Repair
    Check-Launcher 'Healthy configured container starts without recreation' 'existing-ok'
    Check-Launcher 'Unrelated container is never modified' 'unrelated' -Repair -ShouldFail -ExpectedMessage 'ownership/image'
    Check-Launcher 'Repair requires the exact uninitialized-database error' 'wrong-error' -Repair -ShouldFail -ExpectedMessage 'expected uninitialized'
    Check-Launcher 'Docker run failure still removes secret file' 'create-fails' -Repair -ShouldFail -ExpectedMessage 'Container creation failed'
    Check-Launcher 'ACL verification failure blocks secret transmission and container changes' 'acl-fails' -Repair -ShouldFail -ExpectedMessage 'securely prepare'
    $global:fixturePassword='short'
    Check-Launcher 'Short password cannot recreate container' 'short-password' -Repair -ShouldFail -ExpectedMessage 'at least 14'
    Write-Host "MySQL launcher checks PASS: $($results.Count) (simulated Docker only)"
} finally {
    $report=Join-Path (Split-Path $PSScriptRoot -Parent) 'docs\test-evidence\mysql-startup-script-latest.json'
    $hashes=@{};foreach ($p in @($entry,(Join-Path $PSScriptRoot 'MySqlLocal.ps1'),$PSCommandPath)) { $hashes[(Split-Path $p -Leaf)]=(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash }
    @{recordedAt=[DateTime]::UtcNow.ToString('o');scope='Docker, Read-Host and ACL mocked; real temporary-file I/O and cleanup with nonsensitive fixed fixture; no MySQL, Docker runtime or actual Windows ACL enforcement verified';scriptSha256=$hashes;cases=@($results.ToArray())} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $report -Encoding UTF8
    Remove-Item Function:\docker,Function:\Read-Host,Function:\Get-Acl,Function:\Set-Acl -ErrorAction SilentlyContinue
    Remove-Variable fixturePassword,scenario,dockerCalls,environmentFile,fixtureAcl -Scope Global -ErrorAction SilentlyContinue
}
