# Documentación de la API - CRCoach

Esta documentación resume los endpoints disponibles en la API del proyecto Backend-CRCoach.

Nota rápida:
- Base path: `http://{host}:{port}` (por defecto `http://localhost:8080`).
- Prefijo de rutas: `/api/v1/`.
- Autenticación: el proyecto define un esquema JWT (Bearer) en la configuración OpenAPI. Los endpoints que requieren autenticación usan `Authorization: Bearer <token>`.
- Paginación: los endpoints que devuelven colecciones paginadas aceptan parámetros estándar `page`, `size`, `sort`.

Índice
- [Autenticación](#autenticaci%C3%B3n)
- [Usuarios](#usuarios)
- [Player Profiles](#player-profiles)
- [Decks](#decks)
- [Cards](#cards)
- [Player Cards](#player-cards)
- [Battles](#battles)
- [Snapshots](#snapshots)
- [Clans](#clans)
- [Arenas](#arenas)
- [Player Entities](#player-entities)
- [Sessions](#sessions)
- [Notifications](#notifications)
- [Goals](#goals)
- [Password Reset](#password-reset)

-------------------------

## Autenticación

La aplicación define un esquema `bearerAuth` (JWT). Muchos endpoints (por ejemplo `/api/v1/users/me`) requieren que el usuario esté autenticado.

Cabecera de ejemplo:

```
Authorization: Bearer <token>
```

El proyecto no expone en los controladores vistos un endpoint público de login/registro con token, pero la configuración indica que la API utiliza JWT. Ajusta según cómo tu aplicación gestione login (probablemente un endpoint en otro módulo o externalizado).

-------------------------

## Usuarios

Rutas principales:

- GET `/api/v1/users` — Listar usuarios (paginado).
  - Query params: `page`, `size`, `sort`.
  - Respuesta: página de `UserResponseDTO`.

- GET `/api/v1/users/id/{id}` — Obtener usuario por ID.

- GET `/api/v1/users/username/{username}` — Obtener usuario por nombre de usuario.

- GET `/api/v1/users/email/{email}` — Obtener usuario por correo.

- POST `/api/v1/users` — Crear usuario.
  - Body: `UserRequestDTO` (JSON). Ejemplo mínimo:

```json
{
  "name": "Juan",
  "surnames": "Pérez",
  "username": "juanp",
  "email": "juan@example.com",
  "passwordHash": "hashedPassword",
  "role": "USER"
}
```

- PUT `/api/v1/users/{id}` — Actualizar usuario.

- DELETE `/api/v1/users/{id}` — Eliminar usuario.

- GET `/api/v1/users/email-exists?email=...` — Comprueba existencia del correo.
- GET `/api/v1/users/username-exists?username=...` — Comprueba existencia del nombre de usuario.

- GET `/api/v1/users/me` — Obtener perfil del usuario autenticado (requiere autenticación).

- POST `/api/v1/users/me/player-profile/link/{tag}` — Vincular perfil de Clash Royale al usuario autenticado obteniendo asi los datos de dicho perfil los registros de batalla y generando una snapshot (tag de Supercell). [IMPOTANTE]

- POST `/api/v1/users/me/player-profile/unlink/{id}` — Desvincular perfil.

- POST `/api/v1/users/{id}/avatar` — Subir avatar (multipart/form-data, campo `file`). Ejemplo curl:

```bash
curl -X POST "http://localhost:8080/api/v1/users/123/avatar" \
  -H "Authorization: Bearer <token>" \
  -F "file=@./avatar.png"
```

- GET `/api/v1/users/me/avatar` — Obtener avatar del usuario autenticado (devuelve imagen).
- GET `/api/v1/users/{id}/avatar` — Obtener avatar por id.

Modelos relevantes:

- `UserRequestDTO` (campos principales): name, surnames, username, email, passwordHash, avatarUrl, role, playerTag, enabled.
- `UserResponseDTO` añade `id` y `createdAt`.

-------------------------

## Player Profiles

- GET `/api/v1/player_profiles` — Listar perfiles (paginado).
- GET `/api/v1/player_profiles/id/{id}` — Obtener por ID.
- GET `/api/v1/player_profiles/tag/{playerTag}` — Obtener por tag.
- POST `/api/v1/player_profiles` — Crear perfil (body: `PlayerProfileRequestDTO`).
- GET `/api/v1/player_profiles/player/{tag}` — Endpoint adicional para obtener un perfil por tag asi como su regitro de batalla y generar una snapshot (usa la lógica de importación/sincronización con la API de Clash Royale). [IMPOTANTE]

Descripción: estas rutas permiten obtener y almacenar el perfil extraído de la API de Clash Royale (campos como tag, trophies, arena, clan, playerCards, etc.).

Ejemplo (crear):

```bash
curl -X POST "http://localhost:8080/api/v1/player_profiles" \
  -H "Content-Type: application/json" \
  -d '{"tag":"#PLAYER_TAG","name":"Jugador"}'
```

-------------------------

## Decks

- GET `/api/v1/decks` — Listar decks.
- GET `/api/v1/decks/{id}` — Obtener deck por ID.
- GET `/api/v1/decks/api/{apiId}` — Obtener deck por id externo (apiId).
- POST `/api/v1/decks` — Crear deck (body: `DeckRequestDTO`).
- PUT `/api/v1/decks/{id}` — Actualizar deck.
- DELETE `/api/v1/decks/{id}` — Eliminar deck.

Ejemplo crear:

```bash
curl -X POST "http://localhost:8080/api/v1/decks" \
  -H "Content-Type: application/json" \
  -d '{"apiId":12345,"archetype":"Control","playerCards":[] }'
```

-------------------------

## Cards

- GET `/api/v1/cards` — Listar cartas (locales/importadas).
- GET `/api/v1/cards/{id}` — Obtener carta por ID.
- POST `/api/v1/cards` — Crear carta (body: `CardRequestDTO`).
- DELETE `/api/v1/cards/{id}` — Eliminar carta.
- POST `/api/v1/cards/import` — Importar todas las cartas desde la API externa. [IMPOTANTE]

Ejemplo importar:

```bash
curl -X POST "http://localhost:8080/api/v1/cards/import"
```

-------------------------

## Player Cards

- GET `/api/v1/player_cards` — Listar cartas asociadas a jugadores.
- POST `/api/v1/player_cards` — Crear una PlayerCard (body: `PlayerCardRequestDTO`).
- POST `/api/v1/player_cards/import/{playerTag}` — Importar cartas para un jugador por tag.

-------------------------

## Battles

- POST `/api/v1/battles` — Crear batalla (body: `BattleRequestDTO`).
- GET `/api/v1/battles` — Listar batallas.
- GET `/api/v1/battles/{id}` — Obtener batalla por ID.
- PUT `/api/v1/battles/{id}` — Actualizar batalla.
- DELETE `/api/v1/battles/{id}` — Eliminar batalla.
- POST `/api/v1/battles/import/{playerTag}` — Importar batallas para un jugador desde la API externa.

Ejemplo importar por jugador:

```bash
curl -X POST "http://localhost:8080/api/v1/battles/import/%23PLAYER_TAG"
```

-------------------------

## Snapshots

Endpoints:

- GET `/api/v1/snapshots/player-tag/{playerTag}` — Obtener snapshots por tag de jugador (paginado).
- GET `/api/v1/snapshots/player-profile/{playerProfileId}` — Obtener snapshots por ID de perfil (paginado).
- GET `/api/v1/snapshots/{id}` — Obtener snapshot por ID.
- GET `/api/v1/snapshots/player-tag/{playerTag}/range?from={ISO_DATETIME}&to={ISO_DATETIME}` — Obtener snapshots en un rango de fechas (paginado).
- DELETE `/api/v1/snapshots/cleanup?daysOld=30` — Eliminar snapshots más antiguas que X días (por defecto 30).

Parámetros de fecha: usar formato ISO (ej. `2023-03-01T00:00:00`).

Ejemplo rango:

```bash
curl "http://localhost:8080/api/v1/snapshots/player-tag/%23PLAYER_TAG/range?from=2024-01-01T00:00:00&to=2024-03-01T00:00:00"
```

-------------------------

## Clans

- GET `/api/v1/clans` — Listar clanes.
- GET `/api/v1/clans/{id}` — Obtener clan por ID.
- POST `/api/v1/clans` — Crear clan (body: `ClanRequestDTO`).
- PUT `/api/v1/clans/{id}` — Actualizar clan.
- DELETE `/api/v1/clans/{id}` — Eliminar clan.

-------------------------

## Arenas

- GET `/api/v1/arenas` — Listar arenas.
- GET `/api/v1/arenas/{id}` — Obtener arena por ID.
- POST `/api/v1/arenas` — Crear arena (`ArenaRequestDTO`).
- PUT `/api/v1/arenas/{id}` — Actualizar arena.
- DELETE `/api/v1/arenas/{id}` — Eliminar arena.

-------------------------

## Player Entities

- GET `/api/v1/player_entities` — Listar (paginado).
- GET `/api/v1/player_entities/id/{id}` — Obtener por ID.
- GET `/api/v1/player_entities/tag/{tag}` — Obtener por tag.
- POST `/api/v1/player_entities` — Crear (`PlayerEntityRequestDTO`).
- PUT `/api/v1/player_entities/{id}` — Actualizar.
- DELETE `/api/v1/player_entities/{id}` — Eliminar.

-------------------------

## Sessions

- GET `/api/v1/sessions` — Listar sesiones (paginado).
- GET `/api/v1/sessions/id/{id}` — Obtener por ID.
- GET `/api/v1/sessions/title/{title}` — Obtener por título.
- POST `/api/v1/sessions` — Crear sesión (`SessionRequestDTO`).
- PUT `/api/v1/sessions/{id}` — Actualizar.
- DELETE `/api/v1/sessions/{id}` — Eliminar.

Modelo de sesión (ejemplo JSON):

```json
{
  "title": "Sesión de entrenamiento",
  "notes": "Trabajar control de mazo",
  "mood": "good",
  "startTime": "2024-03-01T18:00:00",
  "endTime": "2024-03-01T19:00:00"
}
```

-------------------------

## Notifications

- GET `/api/v1/notifications` — Listar notificaciones (paginado).
- GET `/api/v1/notifications/myNotifications/{email}` — Listar notificaciones de un usuario por email (paginado).
- POST `/api/v1/notifications` — Crear notificación (`NotificationRequestDTO`).

Ejemplo crear notificación:

```bash
curl -X POST "http://localhost:8080/api/v1/notifications" \
  -H "Content-Type: application/json" \
  -d '{"title":"Recordatorio","message":"No olvides tu sesión","userEmail":"juan@example.com"}'
```

-------------------------

## Goals

- GET `/api/v1/goals` — Listar objetivos (paginado).
- GET `/api/v1/goals/id/{id}` — Obtener por ID.
- GET `/api/v1/goals/title/{title}` — Obtener por título.
- POST `/api/v1/goals` — Crear (`GoalRequestDTO`).
- PUT `/api/v1/goals/{id}` — Actualizar.
- DELETE `/api/v1/goals/{id}` — Eliminar.

Ejemplo crear objetivo:

```bash
curl -X POST "http://localhost:8080/api/v1/goals" \
  -H "Content-Type: application/json" \
  -d '{"title":"Subir trofeos","description":"Llegar a 5000 trofeos","metricType":"trophies","targetValue":5000}'
```

-------------------------

## Password Reset

- POST `/api/v1/auth/password/forgot` — Solicitar reseteo: body `PasswordForgotRequestDTO` con `email`.
- GET `/api/v1/auth/password/verify?token=...` — Verificar token de reseteo.
- POST `/api/v1/auth/password/reset` — Confirmar reseteo: `PasswordResetConfirmDTO` con `token` y `newPassword`.

-------------------------

Apéndice — buenas prácticas y notas

- Para endpoints paginados, revisar la implementación de Spring Data `Pageable` para ajustar `page`, `size` y `sort`.
- Para imágenes (`avatar`) el servidor responde el archivo con un Content-Type apropiado (image/png, image/jpeg, ...).
- Campos complejos (entidades embebidas, listas de `PlayerCard`) suelen representarse como objetos JSON o arrays.
- Si quieres, puedo:
  - Generar ejemplos más detallados por cada DTO.
  - Añadir una tabla de modelos con propiedades y tipos para cada DTO.
  - Incluir ejemplos de respuestas (200, 201, 400, 404) para cada endpoint.

-------------------------

Si quieres que convierta esta documentación Markdown en HTML, la publique en una ruta estática del proyecto o que complete automáticamente los esquemas con todos los campos y ejemplos, dímelo y lo hago.


