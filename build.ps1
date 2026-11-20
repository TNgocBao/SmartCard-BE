param([switch]$Offline)
$ErrorActionPreference='Stop'
if (!$env:JAVA_HOME -or !(Test-Path "$env:JAVA_HOME\bin\java.exe")) { $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11' }
$arguments=@('-B',"-Dmaven.repo.local=$PSScriptRoot\.cache\m2")
if ($Offline) { $arguments+='-o' }
Push-Location $PSScriptRoot
try {
    & mvn.cmd @arguments -f "$PSScriptRoot\pom.xml" verify
    if ($LASTEXITCODE -ne 0) { throw 'Backend build/test failed' }
    python "$PSScriptRoot\scripts\check_architecture.py"
    if ($LASTEXITCODE -ne 0) { throw 'Backend architecture check failed' }
    python "$PSScriptRoot\scripts\check_requirements_model.py"
    if ($LASTEXITCODE -ne 0) { throw 'Three-document model audit failed' }
    python "$PSScriptRoot\scripts\update_test_guide.py"
    if ($LASTEXITCODE -ne 0) { throw 'Test guide update failed' }
} finally { Pop-Location }
