# Despliegue (Docker Compose) - Backend CRCoach

Este archivo explica cómo desplegar la aplicación backend localmente usando Docker Compose y cómo verificar la red y los endpoints.

Requisitos previos
- Docker y docker-compose (o Docker Desktop) instalados.
- Copia del fichero de ejemplo de variables de entorno: `.env.example` → `.env` (rellenar valores reales, **no** subir `.env` al repositorio).

Pasos rápidos

1. Crear el fichero `.env` a partir del ejemplo y editar valores sensibles:

```bash
cp .env.example .env
# Edita .env con tus valores (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD, PORT, etc.)
```

2. Levantar los servicios:

```bash
docker compose up -d --build
```

3. Verificar el estado de los servicios:

```bash
docker compose ps
docker compose logs -f app
```

Comprobaciones de red y API

- API docs (OpenAPI):

```bash
curl -sS http://localhost:8080/v3/api-docs | jq .info
```

- Endpoint público de registro (ejemplo):

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","passwordHash":"pass"}'
```

- Si usas el reverse proxy nginx (si lo despliegas), la ruta `/api/` se encaminan al backend.

Notas importantes
- No incluyas secretos en el repositorio. Si has subido claves por error, rótalas inmediatamente.
- `docker-compose.yml` referencia `.env`. Si no existe, el servicio no podrá arrancar correctamente.
- Volumen de la base de datos: `crcoach_db_data` (persistencia local).

Troubleshooting rápido
- Error de conexión a la DB: verifica que `PGHOST`, `PGUSER`, `PGPASSWORD` y `PGDATABASE` en `.env` son correctos y accesibles desde la máquina Docker.
- Permisos/volúmenes: si hay problemas con el volumen de Postgres, inspecciona `docker volume ls` y `docker volume inspect crcoach_db_data`.
- Logs del backend: `docker compose logs -f app`.

