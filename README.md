# VetTurno

API de agenda para Veterinaria Huellitas: responsables, mascotas, veterinarios y citas. Java 17, Spring Boot, Maven, MySQL 8.4, JPA, JWT y Swagger.

## Ejecutar en Windows 11

Con Docker Desktop abierto, desde la carpeta del proyecto:

```powershell
.\scripts\configure.ps1
docker compose up -d --build
```

Swagger: http://localhost:8088/swagger-ui/index.html

Las credenciales locales se generan en `.env`, que queda fuera de Git. Los datos de MySQL se conservan en un volumen de Docker.

Para ejecutar Java directamente (JDK 17 o superior):

```powershell
docker compose up -d mysql
.\scripts\run.ps1
```

## Acceso

En Swagger, ejecuta `POST /api/auth/register` con tu email y una contraseña de 8 a 72 caracteres (máximo 72 bytes UTF-8). El registro siempre crea USER. Copia `token` en **Authorize**, sin escribir `Bearer`.

Para habilitar una cuenta ADMIN registrada, desde la terminal local:

```powershell
.\scripts\grant-admin.ps1 -Email 'tu-email@example.com'
```

Vuelve a iniciar sesión en `POST /api/auth/login` y usa el nuevo token. Solo ADMIN puede crear veterinarios. USER y ADMIN pueden crear y consultar responsables, mascotas y citas.

Orden para probar: registrar cuenta, habilitar ADMIN, iniciar sesión, crear veterinario, crear propietario, crear mascota con `propietarioId`, crear cita con `mascotaId` y `veterinarioId`.

Las fechas usan hora local de `America/Santiago`, por ejemplo `2027-01-20T10:30:00`. Deben ser futuras. Cada veterinario admite una sola cita por fecha y hora exactas. La agenda se devuelve ordenada y se filtra en `GET /api/citas/veterinario/{id}`.

## Pruebas

```powershell
docker compose up -d mysql
.\scripts\test.ps1
```

O mediante Java 17 en Docker:

```powershell
docker compose --profile test run --rm test
```

Las pruebas usan exclusivamente `vetturno_test` y limpian sus datos antes de cada escenario. El flujo CI también ejecuta las pruebas con MySQL real. Los resultados se generan en `target/surefire-reports`.

La API responde 200 para consultas y autenticación, 201 para creaciones, 400 para validaciones o conflictos, 401 sin credenciales válidas y 403 sin permisos. Los errores mantienen `status`, `mensaje`, `errores` y `timestamp`.

Para detener los servicios conservando los datos:

```powershell
docker compose down
```
