$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    Write-Host 'La configuración local ya existe.'
    exit 0
}
function New-LocalSecret {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return [Convert]::ToBase64String($bytes)
}
$content = @(
    "DB_PASSWORD=$(New-LocalSecret)"
    "MYSQL_ROOT_PASSWORD=$(New-LocalSecret)"
    "JWT_SECRET=$(New-LocalSecret)"
    'MYSQL_PORT=13306'
    'APP_PORT=8088'
)
[IO.File]::WriteAllLines($envFile, $content, (New-Object Text.UTF8Encoding $false))
Write-Host 'Configuración local generada en .env.'
