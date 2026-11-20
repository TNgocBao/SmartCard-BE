param([ValidateRange(1,65535)][int]$Port=18080,[switch]$Release)
$ErrorActionPreference='Stop'
if ($env:SPRING_PROFILES_ACTIVE -match '(^|[,;\s])prod($|[,;\s])') { throw 'MailHog is for local testing. Run it in a local PowerShell session without the prod profile.' }
& "$PSScriptRoot\start-mailhog.ps1"
if ($LASTEXITCODE -ne 0) { throw 'MailHog did not start.' }
$savedProfiles=$env:SPRING_PROFILES_ACTIVE
try {
    $env:SPRING_PROFILES_ACTIVE='local,mailhog'
    Write-Host 'BE -> MailHog SMTP 127.0.0.1:1025. Open http://127.0.0.1:8025 to read OTP/activation emails.'
    if ($Release) { & "$PSScriptRoot\run.ps1" -Port $Port }
    else { & "$PSScriptRoot\run.ps1" -UseCachedRuntime -Port $Port }
} finally { $env:SPRING_PROFILES_ACTIVE=$savedProfiles }
