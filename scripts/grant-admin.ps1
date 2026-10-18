param([Parameter(Mandatory = $true)][string]$Email)
$ErrorActionPreference = 'Stop'
if ($Email -notmatch '^[a-zA-Z0-9.!#$%&*+/=?^_`{|}~-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$') { throw 'Email inválido.' }
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $sql = "UPDATE usuarios SET rol='ADMIN' WHERE email=LOWER('$Email'); SELECT email,rol FROM usuarios WHERE email=LOWER('$Email');"
    $sql | docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u "$MYSQL_USER" "$MYSQL_DATABASE"'
    if ($LASTEXITCODE -ne 0) { throw 'No fue posible asignar ADMIN.' }
    Write-Host 'Inicia sesión nuevamente para obtener un token con el rol actualizado.'
} finally { Pop-Location }
