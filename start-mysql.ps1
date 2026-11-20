param([switch]$RepairMissingPassword)
# Local development only. Existing project volumes are never attached, removed or reset.
$ErrorActionPreference='Stop'
. "$PSScriptRoot\scripts\MySqlLocal.ps1"
$containerName='atbm-mysql'
$volumeName='atbm_mysql_data'
$image='mysql:8.4'
$ownershipLabel='vn.kma.idp.localdb'
$create=$false;$backupName=$null;$environmentFile=$null
if (!(Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Docker CLI not found. Install/open Docker Desktop first.' }
& docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Cannot access Docker Engine. Open Docker Desktop and run this script in your own PowerShell session.' }
$names=@(& docker container ls -a --format '{{.Names}}')
if ($LASTEXITCODE -ne 0) { throw 'Cannot list containers. No changes made.' }
if ($names -contains $containerName) {
    $raw=& docker inspect --format '{{json .}}' $containerName
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect the KMA MySQL container.' }
    $existing=$raw | ConvertFrom-Json;$raw=$null
    Assert-ManagedMySqlContainer $existing
    $hasPassword=@($existing.Config.Env | Where-Object { $_ -match '^MYSQL_ROOT_PASSWORD=.+' }).Count -gt 0
    $existing=$null
    if (!$hasPassword) {
        if (!$RepairMissingPassword) { throw 'Container is missing MYSQL_ROOT_PASSWORD. Run .\backend\start-mysql.ps1 -RepairMissingPassword to preserve its volume and replace the failed container.' }
        $savedPreference=$ErrorActionPreference
        try { $ErrorActionPreference='Continue';$recentLogs=(& docker logs --tail 80 $containerName 2>&1 | Out-String) }
        finally { $ErrorActionPreference=$savedPreference }
        if ($LASTEXITCODE -ne 0 -or $recentLogs -notmatch 'Database is uninitialized and password option is not specified') { throw 'The expected uninitialized-database error was not found. Manual review required; no container was changed.' }
        $recentLogs=$null
        $environmentFile=New-MySqlEnvironmentFile -Directory "$PSScriptRoot\.local"
        $create=$true
        $backupName='atbm-mysql-failed-'+[Guid]::NewGuid().ToString('N').Substring(0,12)
    } else {
        & docker start $containerName | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'MySQL container could not start. Check Docker Desktop for its error.' }
    }
} else {
    $volumes=@(& docker volume ls --format '{{.Name}}')
    if ($LASTEXITCODE -ne 0) { throw 'Cannot list volumes. No changes made.' }
    if ($volumes -contains $volumeName) { throw 'atbm_mysql_data exists without its container. Preserve it and review recovery before creating a new server.' }
    $listener=[System.Net.Sockets.TcpClient]::new()
    try {
        $connect=$listener.ConnectAsync('127.0.0.1',3306)
        try { $null=$connect.Wait(1000) } catch { }
        if ($listener.Connected) { throw 'Port 3306 already has a server. Use that connection or review it first; no new container was created.' }
    } finally { $listener.Dispose() }
    Write-Host 'Creating a NEW empty ATBM MySQL server. Old backend_mysql_data and exam-app-backend_mysql_data volumes remain untouched.'
    $environmentFile=New-MySqlEnvironmentFile -Directory "$PSScriptRoot\.local"
    $create=$true
}
if ($create) {
    try {
        if ($backupName) {
            & docker stop $containerName | Out-Null
            if ($LASTEXITCODE -ne 0) { throw 'Could not stop failed container. Its volume was preserved.' }
            & docker rename $containerName $backupName
            if ($LASTEXITCODE -ne 0) { throw 'Could not preserve failed container under a backup name.' }
            & docker update --restart=no $backupName | Out-Null
            if ($LASTEXITCODE -ne 0) { throw 'Could not disable restart for backup container. Its volume was preserved.' }
            Write-Host "Failed container preserved as $backupName. Reusing ONLY atbm_mysql_data; no volume was deleted."
        }
        # Explicit env-file avoids dependence on environment inheritance through the Docker launcher.
        & docker run -d --name $containerName --label "$ownershipLabel=true" --restart unless-stopped `
            -p '127.0.0.1:3306:3306' --mount "type=volume,source=$volumeName,target=/var/lib/mysql" `
            --env-file $environmentFile `
            --health-cmd 'mysqladmin ping -h 127.0.0.1 --silent' --health-interval 3s --health-timeout 3s --health-retries 30 $image | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'Container creation failed. Inspect Docker Desktop before retrying; no data was deleted.' }
    } finally {
        if ($environmentFile -and (Test-Path -LiteralPath $environmentFile)) { Remove-Item -LiteralPath $environmentFile -Force }
    }
}
Write-Host 'Waiting for MySQL to accept connections (up to 90 seconds)...'
for ($attempt=0;$attempt -lt 45;$attempt++) {
    $health=& docker inspect --format '{{.State.Health.Status}}' $containerName
    if ($LASTEXITCODE -ne 0) { throw 'Cannot read MySQL health state.' }
    if ($health.Trim() -eq 'healthy') {
        Write-Host 'MySQL is ready at 127.0.0.1:3306. TCP health check passed; database authentication is verified by setup-database.ps1.'
        Write-Host 'Next: .\backend\run.ps1 -UseCachedRuntime (from D:\ATBM-SYSTEM). Enter the root password chosen above when prompted.'
        return
    }
    if ($health.Trim() -eq 'unhealthy') { throw 'MySQL health check failed. Inspect atbm-mysql logs in Docker Desktop. Do not delete its volume.' }
    if (($attempt % 10) -eq 0) { Write-Host 'MySQL is still initializing...' }
    Start-Sleep -Seconds 2
}
throw 'MySQL is not ready yet. Check atbm-mysql in Docker Desktop; its volume was preserved.'
