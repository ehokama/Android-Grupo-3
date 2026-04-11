# Android-Grupo-3

## Conexion con backend_rutea (Retrofit)

### Requisitos
- Backend `backend_rutea` ejecutando en `http://localhost:8080`.
- Emulador Android para usar `http://10.0.2.2:8080/`.
- Permiso de internet y `usesCleartextTraffic` habilitado para entorno local.

### URL base
La URL base del cliente Retrofit se define en:
- `app/src/main/java/com/rutea/app/activitiesandviews/ui/data/network/RetrofitClient.java`

Configuraciones recomendadas:
- Emulador: `http://10.0.2.2:8080/`
- Dispositivo fisico: `http://<ip-local-pc>:8080/`

### Estructura de red
- `AuthApiService`: login/register/otp.
- `ActivityApiService`: listado, featured, detalle por id.
- `SessionManager`: persiste token JWT y datos de usuario.
- `AuthInterceptor`: adjunta `Authorization: Bearer <token>` a requests.

### Flujo integrado
1. `LoginFragment` consume `/api/auth/login` y guarda token.
2. `RegisterFragment` consume `/api/auth/register`.
3. `HomeFragment` consume actividades reales desde `/api/activities`.
4. `ActivityDetailFragment` consulta `/api/activities/{id}`.

### Nota
El paquete legacy `com.rutea.app.api` quedo removido para evitar endpoints desactualizados.