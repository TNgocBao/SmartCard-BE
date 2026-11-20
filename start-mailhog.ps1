param()
$ErrorActionPreference='Stop'
$dockerCommand=Get-Command docker -ErrorAction SilentlyContinue
if ($dockerCommand) { $dockerPath=$dockerCommand.Source }
else { $dockerPath='C:\Program Files\Docker\Docker\resources\bin\docker.exe' }
if (!(Test-Path -LiteralPath $dockerPath)) { throw 'Docker CLI not found. Install and open Docker Desktop first.' }
& $dockerPath info --format '{{.ServerVersion}}' 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Cannot access Docker Engine. Open Docker Desktop and run this script in your own PowerShell session.' }
$names=@(& $dockerPath container ls -a --format '{{.Names}}')
if ($LASTEXITCODE -ne 0) { throw 'Cannot list containers.' }
if ($names -contains 'atbm-mailhog') {
    $raw=& $dockerPath inspect atbm-mailhog
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect atbm-mailhog.' }
    $existing=($raw | ConvertFrom-Json)[0]
    if ($existing.Config.Labels.'vn.kma.idp.mailhog' -ne 'true') { throw 'atbm-mailhog exists but is not managed by this project. Review it before proceeding.' }
    foreach ($mapping in @(@('1025/tcp','1025'),@('8025/tcp','8025'))) {
        $bindings=@($existing.HostConfig.PortBindings.($mapping[0]))
        if ($bindings.Count -ne 1 -or $bindings[0].HostIp -ne '127.0.0.1' -or $bindings[0].HostPort -ne $mapping[1]) { throw 'Existing MailHog has unexpected port bindings. Review its configuration.' }
    }
    & $dockerPath start atbm-mailhog | Out-Null
} else {
    & $dockerPath compose -f "$PSScriptRoot\docker-compose.mailhog.yml" up -d
}
if ($LASTEXITCODE -ne 0) { throw 'MailHog startup failed. Check Docker Desktop, free ports 1025/8025, and Docker Hub connectivity.' }
$ready=$false
for ($attempt=0;$attempt -lt 20;$attempt++) {
    try {
        $null=Invoke-RestMethod -Uri 'http://127.0.0.1:8025/api/v2/messages?limit=1' -TimeoutSec 2
        $ready=$true;break
    } catch { Start-Sleep -Milliseconds 500 }
}
if (!$ready) { throw 'MailHog started but HTTP readiness failed. Inspect atbm-mailhog logs in Docker Desktop.' }
Write-Host 'MailHog ready: SMTP 127.0.0.1:1025 | inbox http://127.0.0.1:8025'
