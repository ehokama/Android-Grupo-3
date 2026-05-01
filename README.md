# Android-Grupo-3

Aplicación Android nativa (Java) para gestión de actividades turísticas, integrada con `backend_rutea`.

## Stack principal

- Java + AndroidX
- Navigation Component (single-activity + fragments)
- Hilt (inyección de dependencias)
- Retrofit + OkHttp + Gson
- Room (caché offline)
- EncryptedSharedPreferences (sesión/token)
- Glide (imágenes)

## Requisitos

- Backend `backend_rutea` corriendo en `http://localhost:8080`.
- Emulador Android (usa `http://10.0.2.2:8080/`).
- `usesCleartextTraffic=true` habilitado para entorno local.

## Configuración de red

- URL base: `app/src/main/java/com/rutea/app/activitiesandviews/di/NetworkModule.java`
- Endpoints auth públicos (sin token):  
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/auth/otp/request`
  - `/api/auth/otp/verify`
- Endpoints autenticados: resto de `/api/**` (incluye `change-email` y `change-password`).

## Arquitectura de navegación

- Activity única: `MainActivity`
- Contenedor: `FragmentContainerView` + `NavHostFragment`
- Graph principal: `nav_graph` + `home_nav_graph`
- Bottom navigation en destinos de primer nivel.

## Módulos funcionales

- **Auth**
  - Login, registro, OTP.
  - Persistencia de sesión en `TokenManager`.
- **Home / Search / Favorites**
  - Listados de actividades y favoritos con sincronización backend.
- **Detalle de actividad**
  - Información completa + galería de fotos en carrusel.
- **Reservas**
  - Selección de disponibilidad y creación de reserva.
  - Confirmación y vista de historial/mis actividades.
- **Perfil**
  - Edición de datos personales.
  - Cambio de foto local.
  - Cambio de mail y contraseña con validación de contraseña actual.

## Modo sin conexión (Mis Actividades)

Se cachean reservas activas y su detalle esencial en Room:

- datos de actividad y estado de reserva
- destino, guía, duración, punto de encuentro
- datos de reseña (si existen)
- voucher/datos de confirmación necesarios para consulta

Comportamiento:

- al confirmar una reserva, se guarda en caché automáticamente
- `HistoryFragment` carga primero caché local y luego red
- al recuperar conexión, sincroniza con servidor y refresca estado
- se muestra banner visual cuando la app está offline

## Endpoints backend usados (resumen)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/otp/request`
- `POST /api/auth/otp/verify`
- `POST /api/auth/change-password`
- `POST /api/auth/change-email`
- `GET /api/travellers/me`
- `PUT /api/travellers/me`
- `GET /api/activities/**`
- `GET /api/disponibilities`
- `POST /api/reserves`
- `GET /api/reserves/my-history`
- `POST /api/reserves/{id}/cancel`
- `GET /api/news/**`

## Build

Desde la raíz del proyecto:

- Windows: `.\gradlew.bat :app:assembleDebug`

## Nota

Este README refleja el estado actual del proyecto con Hilt + `NetworkModule` + `TokenManager` y reemplaza referencias legacy antiguas.