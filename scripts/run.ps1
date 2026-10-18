$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'configure.ps1')
Get-Content -LiteralPath (Join-Path $projectRoot '.env') | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') }
}
$env:PORT = $env:APP_PORT
$env:DB_URL = "jdbc:mysql://127.0.0.1:$($env:MYSQL_PORT)/vetturno"
Push-Location $projectRoot
try {
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'La aplicación no pudo iniciarse.' }
} finally { Pop-Location }
