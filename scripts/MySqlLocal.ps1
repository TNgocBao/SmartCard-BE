# Password transport for local Docker initialization. Never emit the file content or password.
function New-MySqlEnvironmentFile {
    param([Parameter(Mandatory=$true)][string]$Directory)
    $secret=Read-Host 'MySQL root password (at least 14 characters; keep for database setup)' -AsSecureString
    $plain=[PSCredential]::new('root',$secret).GetNetworkCredential().Password
    if ($plain.Length -lt 14 -or $plain.IndexOfAny([char[]]"`r`n`0") -ge 0) { throw 'Password must have at least 14 characters and no line breaks or NUL.' }
    $null=New-Item -ItemType Directory -Path $Directory -Force
    $path=Join-Path $Directory ('mysql-init-'+[Guid]::NewGuid().ToString('N')+'.env')
    try {
        # Create an empty file, restrict it, then write the secret. No secret exists before the ACL applies.
        $stream=[System.IO.File]::Open($path,[System.IO.FileMode]::CreateNew);$stream.Dispose()
        $acl=Get-Acl -LiteralPath $path
        $acl.SetAccessRuleProtection($true,$false)
        foreach ($rule in @($acl.Access)) { $null=$acl.RemoveAccessRuleSpecific($rule) }
        $sid=[System.Security.Principal.WindowsIdentity]::GetCurrent().User
        $acl.AddAccessRule([System.Security.AccessControl.FileSystemAccessRule]::new($sid,'FullControl','Allow'))
        Set-Acl -LiteralPath $path -AclObject $acl
        $verifiedAcl=Get-Acl -LiteralPath $path
        if (!$verifiedAcl.AreAccessRulesProtected -or @($verifiedAcl.Access | Where-Object { $_.IsInherited -or $_.IdentityReference.Translate([System.Security.Principal.SecurityIdentifier]).Value -ne $sid.Value }).Count -gt 0) { throw 'Temporary secret permissions were not applied.' }
        [System.IO.File]::WriteAllText($path,"MYSQL_ROOT_PASSWORD=$plain`nMYSQL_ROOT_HOST=%`n",[System.Text.UTF8Encoding]::new($false))
        return $path
    } catch {
        if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path -Force }
        throw 'Could not securely prepare the temporary Docker environment file. No password was printed.'
    } finally { $plain=$null;$secret=$null }
}

function Assert-ManagedMySqlContainer {
    param([Parameter(Mandatory=$true)]$Container)
    if ($Container.Config.Labels.'vn.kma.idp.localdb' -ne 'true' -or $Container.Config.Image -ne 'mysql:8.4') { throw 'Container ownership/image differs from KMA configuration. No changes made.' }
    $mounts=@($Container.Mounts | Where-Object { $_.Type -eq 'volume' -and $_.Name -eq 'atbm_mysql_data' -and $_.Destination -eq '/var/lib/mysql' })
    $ports=@($Container.HostConfig.PortBindings.'3306/tcp')
    if ($mounts.Count -ne 1 -or $ports.Count -ne 1 -or $ports[0].HostIp -ne '127.0.0.1' -or $ports[0].HostPort -ne '3306') { throw 'Container volume or port differs from KMA configuration. No changes made.' }
}
