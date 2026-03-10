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

### Reset de BD de test (mgdb_test)

Si la base `mgdb_test` acumula datos duplicados por uso repetido o E2E, puedes resetearla:

**Opción 1 – Flyway clean + migrate** (con backend detenido):

```bash
cd mg-request-backend
mvn flyway:clean flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3307/mgdb_test -Dflyway.user=root -Dflyway.password=admin
```

**Opción 2 – Borrar y recrear la BD**:

```sql
DROP DATABASE mgdb_test;
CREATE DATABASE mgdb_test;
```

Al arrancar el backend con perfil `test`, Flyway aplicará las migraciones.

**Limpieza automática E2E:** Los tests E2E de Playwright llaman a `POST /api/test/cleanup` antes y después de la suite para borrar datos con prefijos `e2e_` y `reqtest_` (evita acumulación de datos de tests JUnit e E2E). Si ves duplicados al navegar tras `mvn test`, ejecuta `npm run e2e` para limpiar, o recrea la BD con las SQL anteriores.

### Datos demo para dashboards (perfil test)

Para explorar los dashboards con datos de ejemplo sin afectar a JUnit ni a los E2E:

1. Levanta el backend con perfil `test`.
2. Ejecuta el script:

```bash
cd mg-request-backend
./scripts/seed-demo.sh
```

Los datos usan el prefijo `demo_` y no son limpiados por los tests. Puedes ejecutar el script varias veces (es idempotente). Para borrarlos: `POST /api/test/cleanup` con `{"prefix":"demo_"}`.

## Despliegue en AWS

### RDS MySQL

Para producción en AWS, usa RDS MySQL (por ejemplo `db.t3.micro` o `db.t3.small`):

- Crea una instancia RDS MySQL 8 en la misma VPC que Elastic Beanstalk o ECS.
- Crea la base de datos `mgdb` (o el nombre que uses).
- Configura el security group para permitir conexiones en el puerto 3306 solo desde el security group del backend.
- Flyway aplica las migraciones automáticamente al arrancar la aplicación.

### Variables de entorno para producción

Configura estas variables en Elastic Beanstalk (Configuration → Software → Environment properties) o en ECS (Task Definition):

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL de RDS | `jdbc:mysql://tu-rds.region.rds.amazonaws.com:3306/mgdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | (segura) |
| `JWT_SECRET` | Clave para firmar JWT (mín. 256 bits) | (clave segura) |
| `JWT_EXPIRATION_MS` | Expiración del token en ms | `86400000` |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `prod` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | (Opcional) Configuración de email | — |

### Docker (ECS / App Runner)

```bash
docker build -t mg-request-backend .
docker run -e SPRING_DATASOURCE_URL=... -e JWT_SECRET=... -p 8080:8080 mg-request-backend
```

### Elastic Beanstalk (JAR)

1. `mvn package -DskipTests`
2. Crear aplicación EB con plataforma Java 17 (Corretto).
3. Subir el JAR o configurar CodeBuild para despliegue automático.
4. El archivo `.ebextensions/01_jvm.config` configura JVM para instancias pequeñas.

Ver `mg-request-frontend/docs/DEPLOY-AWS.md` para la guía completa de despliegue (frontend + backend).

## Seguridad en producción

- **Variables de entorno obligatorias:** No uses los valores por defecto en producción. Configura al menos:
  - `JWT_SECRET` (clave segura, no usar `defaultSecretKeyForDevelopmentOnlyChangeInProduction`)
  - `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_DATASOURCE_URL` y `SPRING_DATASOURCE_USERNAME` si aplica
- **Usuario admin del seed:** La migración V2 crea un usuario `admin` con contraseña `password`. En producción, cambia esa contraseña inmediatamente tras el primer despliegue o desactiva/elimina el usuario si no lo usas.
- **Tests:** Las credenciales en `src/test` (p. ej. admin/password, "secret") solo se usan en tests y no se incluyen en el JAR de producción.
