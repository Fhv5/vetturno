$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'configure.ps1')
Get-Content -LiteralPath (Join-Path $projectRoot '.env') | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') }
}
$env:TEST_DB_URL = "jdbc:mysql://127.0.0.1:$($env:MYSQL_PORT)/vetturno_test"
Push-Location $projectRoot
try {
    & .\mvnw.cmd -B -ntp verify
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas fallaron.' }
} finally { Pop-Location }
