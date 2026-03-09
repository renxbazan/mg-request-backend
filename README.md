# MG Request - API Backend

Backend Spring Boot 3 para gestión de solicitudes de servicio.

## Requisitos

- **Java 17**
- MySQL (para ejecución y tests)

## Perfiles y base de datos

El proyecto utiliza perfiles de Spring y Flyway para gestionar el esquema y los datos iniciales:

- **Perfil por defecto (`application.properties`)**: orientado a desarrollo.
- **Perfil `test` (`src/main/resources/application-test.properties`)**:
  - MySQL en `localhost:3307`
  - Base de datos `mgdb_test`
  - Usuario `root`, contraseña `admin`
  - Flyway aplica todas las migraciones (`db/migration`), incluyendo el seed de datos y los **usuarios E2E** (`V7__e2e_seed_users.sql`).

## Ejecutar tests

En el terminal donde vayas a lanzar los tests, activa Java 17 antes (por ejemplo con SDKMAN):

```bash
sdk use java 17.0.9-amzn
mvn clean test
```

Para los tests de integración hace falta MySQL en `localhost:3307` con base de datos `mgdb_test` (usuario `root`, contraseña `admin`). El perfil `test` usa `application-test.properties` y Flyway aplica las migraciones y el seed.

## Ejecutar la aplicación

### Modo desarrollo (perfil por defecto)

```bash
sdk use java 17.0.9-amzn   # si usas SDKMAN
mvn spring-boot:run
```

Configura en `application.properties` (o variables de entorno) la URL de MySQL, JWT y correo según tu entorno.

### Modo pruebas E2E (perfil `test`)

Para ejecutar el backend apuntando a `mgdb_test` y con los usuarios de pruebas E2E sembrados por Flyway:

```bash
sdk use java 17.0.9-amzn
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

Esto levanta la API en `http://localhost:8080` usando la base de datos `mgdb_test`, que será consumida por el frontend `mg-request-frontend` y los tests E2E de Playwright.

## Documentación funcional

La documentación funcional completa (perfiles, opciones de menú, flujos de negocio, estados de las solicitudes, etc.) está disponible en:

- `docs/documentacion-funcional.md`

Recomendado para analistas funcionales, soporte y para entender el alcance de los tests E2E.

## Pruebas

### Tests unitarios e integración (JUnit)

- Lanzar todos los tests:

```bash
sdk use java 17.0.9-amzn
mvn clean test
```

Algunos puntos relevantes:

- `src/test/java/com/renx/mg/request/controller/RequestApiIntegrationTest.java` cubre los principales flujos de la API de solicitudes.
- `src/test/java/com/renx/mg/request/security/CurrentUserServiceTest.java` valida la resolución del usuario actual y sus permisos.

### Relación con tests E2E (Playwright)

El backend expone la API REST consumida por el frontend `mg-request-frontend` y por los tests E2E:

- Los tests E2E usan el perfil `test` y la base `mgdb_test`.
- Flyway crea los usuarios de pruebas, por ejemplo:
  - `admin` / `password` (migración V2)
  - `requester_e2e`, `company_admin_e2e`, `worker_e2e` / `password` (migración V7)
- El frontend se conecta a `http://localhost:8080` (proxy de Vite) y valida:
  - Flujos de solicitudes (incluyendo estado `REJECTED`).
  - Filtros del listado.
  - Manejo de errores 403 sin redirigir al login.

## Seguridad en producción

- **Variables de entorno obligatorias:** No uses los valores por defecto en producción. Configura al menos:
  - `JWT_SECRET` (clave segura, no usar `defaultSecretKeyForDevelopmentOnlyChangeInProduction`)
  - `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_DATASOURCE_URL` y `SPRING_DATASOURCE_USERNAME` si aplica
- **Usuario admin del seed:** La migración V2 crea un usuario `admin` con contraseña `password`. En producción, cambia esa contraseña inmediatamente tras el primer despliegue o desactiva/elimina el usuario si no lo usas.
- **Tests:** Las credenciales en `src/test` (p. ej. admin/password, "secret") solo se usan en tests y no se incluyen en el JAR de producción.
