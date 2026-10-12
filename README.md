# Banca Microservicios — Prueba Técnica Backend Java (Semi Senior)

![CI](https://github.com/RansilvaV29/banca-ms-tata/actions/workflows/blank.yml/badge.svg)

Sistema bancario con **dos microservicios** que se comunican de forma **asíncrona mediante RabbitMQ**, cada uno con su propia base de datos PostgreSQL, desplegados con **Docker Compose**.

| | |
|---|---|
| **Lenguaje / framework** | Java 21 · Spring Boot 4.1 (Spring Framework 7, Hibernate 7, Jackson 3, Spring AMQP 4) |
| **Persistencia** | JPA / Hibernate · PostgreSQL 16 (una base por microservicio) |
| **Mensajería** | RabbitMQ 3.13 (topic exchange + Dead Letter Queue) |
| **Pruebas** | JUnit · Mockito · MockMvc · H2 en memoria · JaCoCo (cobertura) |
| **Despliegue** | Docker multi-stage · Docker Compose · GitHub Actions (CI) |
| **Documentación de API** | OpenAPI / Swagger UI · colección Postman |

---

## Revisión rápida

**Requisito:** Docker Desktop en ejecución, con los puertos 5432, 5672, 15672, 8081 y 8082 libres.

```bash
https://github.com/RansilvaV29/banca-ms-tata.git
cd banca-ms-tata
docker compose up --build
```

La primera vez tarda unos minutos porque compila los dos servicios dentro de Docker. Está listo cuando `docker compose ps` muestra los 4 contenedores como **healthy**.

Después:

1. Importar en Postman `postman/BancaMicroservicios.postman_collection.json`.
2. Ejecutar la colección completa con el **Collection Runner**, en orden. Recorre todos los casos de uso del enunciado y los escenarios de error, y cada request trae sus aserciones.

| Recurso | URL |
|---|---|
| API Clientes | http://localhost:8081/api/clientes |
| API Cuentas / Movimientos / Reportes | http://localhost:8082/api/cuentas · `/api/movimientos` · `/api/reportes` |
| Swagger Clientes | http://localhost:8081/swagger-ui.html |
| Swagger Cuentas | http://localhost:8082/swagger-ui.html |
| Consola RabbitMQ | http://localhost:15672 (usuario `banca` / clave `banca123`) |
| PostgreSQL | `localhost:5432` (usuario `banca` / clave `banca123`) · bases `clientes_db` y `cuentas_db` |

Para reiniciar con la base de datos vacía: `docker compose down -v && docker compose up --build`.

---

## Cumplimiento del enunciado

| Requisito | Implementación |
|---|---|
| Separación en 2 microservicios: (Cliente, Persona) y (Cuenta, Movimientos) | `clientes-service` y `cuentas-service` |
| Comunicación asincrónica entre microservicios | Eventos de cliente por RabbitMQ (`ClienteEventPublisher` → `ClienteEventListener`) |
| JPA para el manejo de entidades | Entidades JPA con Hibernate; `Cliente` hereda de `Persona` (`InheritanceType.JOINED`) |
| Patrón Repository y buenas prácticas | Spring Data JPA, capa de servicios con interfaces, DTOs y mappers, modelo de dominio rico |
| Manejo de mensajes de excepción | `@RestControllerAdvice` con un formato de error uniforme y códigos HTTP estándar |
| **F1** – CRUD `/clientes`, `/cuentas`, `/movimientos` | GET, POST, PUT, PATCH y DELETE en los tres recursos |
| **F2** – Movimientos positivos/negativos, actualización de saldo, registro de transacciones | `Cuenta.aplicarMovimiento()` y `Movimiento.registrar()`; cada movimiento guarda el saldo resultante |
| **F3** – Alerta "Saldo no disponible" | `SaldoNoDisponibleException` → **HTTP 422** con mensaje `"Saldo no disponible"` |
| **F4** – Reporte "Estado de cuenta" por rango de fechas y cliente, en JSON | `GET /api/reportes?fecha=AAAA-MM-DD,AAAA-MM-DD&cliente={clienteId}` |
| **F5** – Prueba unitaria de la entidad Cliente | `clientes-service/src/test/.../domain/model/ClienteTest.java` |
| **F6** – Prueba de integración (deseable) | `cuentas-service/src/test/.../integration/EstadoCuentaIntegrationTest.java` |
| Mínimo dos pruebas unitarias de endpoints | `ClienteControllerTest` (3) y `MovimientoControllerTest` (3) |
| Despliegue en Docker | `Dockerfile` por servicio + `docker-compose.yml` |
| Script de base de datos `BaseDatos.sql` | En la raíz del repositorio |
| Colección Postman | `postman/BancaMicroservicios.postman_collection.json` |

---

## Arquitectura

```
                      ┌─────────────────────────┐
  Cliente HTTP ─────▶ │  clientes-service :8081 │──JPA──▶ PostgreSQL · clientes_db
                      │  Persona, Cliente       │         (persona, cliente)
                      └───────────┬─────────────┘
                                  │ publica ClienteEvent (después del commit)
                                  ▼
             RabbitMQ · banca.clientes.exchange (topic, routing key cliente.*)
                                  │
                                  ▼  cola cuentas.clientes.eventos  (+ DLQ)
                      ┌─────────────────────────┐
  Cliente HTTP ─────▶ │  cuentas-service :8082  │──JPA──▶ PostgreSQL · cuentas_db
                      │  Cuenta, Movimiento,    │         (cuenta, movimiento,
                      │  Reportes               │          cliente_snapshot)
                      └─────────────────────────┘
```

### Decisiones principales

- **Una base de datos por microservicio (Database per Service).** Ningún servicio lee las tablas del otro. `cuenta.cliente_id` es una referencia lógica al cliente, no una FK, porque no puede haber FK entre bases distintas.
- **Comunicación asíncrona con réplica local.** Al crear, actualizar o eliminar un cliente, `clientes-service` publica un evento. `cuentas-service` lo consume y mantiene una copia mínima del cliente (`cliente_snapshot`: id, nombre y estado). Así, crear cuentas y generar reportes **no depende de que clientes-service esté disponible**: no hay llamadas HTTP entre servicios. El costo es una **consistencia eventual** de milisegundos.
- **Eliminar un cliente** desactiva automáticamente sus cuentas en el otro microservicio, también mediante el evento.
- **Contrato por JSON, no por clases compartidas.** Cada servicio tiene su propia clase `ClienteEvent`. El convertidor `JacksonJsonMessageConverter` infiere el tipo desde el parámetro del `@RabbitListener` en lugar de la cabecera `__TypeId__`, así los servicios se despliegan de forma independiente.

### Capas de cada microservicio

```
controller   → recibe HTTP, valida con @Valid, devuelve el código HTTP correcto
service      → interfaz + implementación; límites transaccionales (@Transactional)
domain       → entidades con reglas de negocio y excepciones de negocio
repository   → Spring Data JPA, consultas JPQL, @EntityGraph y bloqueos
dto / mapper → la API nunca expone entidades (la contraseña jamás sale)
exception    → @RestControllerAdvice con un formato de error uniforme
messaging    → publicación y consumo de eventos RabbitMQ
```

---

## API

Base: `http://localhost:{puerto}/api`. Todos los cuerpos son JSON.

### Clientes — puerto 8081

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/clientes` | 200 · lista |
| GET | `/api/clientes/{clienteId}` | 200 · 404 |
| POST | `/api/clientes` | 201 + header `Location` · 400 · 409 |
| PUT | `/api/clientes/{clienteId}` | 200 (reemplazo completo) |
| PATCH | `/api/clientes/{clienteId}` | 200 (solo los campos enviados) |
| DELETE | `/api/clientes/{clienteId}` | 204 |

```json
{
  "clienteId": "jlema",
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 35,
  "identificacion": "1712345678",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "1234",
  "estado": true
}
```

- `clienteId` es la clave única de negocio que se expone y que usa el otro microservicio. Si no se envía, se genera. La PK técnica (`persona.id`) no sale a la API.
- La contraseña se almacena con hash **BCrypt** y nunca se devuelve.
- La identificación es única e inmutable.

### Cuentas — puerto 8082

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/cuentas` (filtro opcional `?clienteId=`) | 200 |
| GET | `/api/cuentas/{numeroCuenta}` | 200 · 404 |
| POST | `/api/cuentas` | 201 · 404 si el cliente no existe · 409 si la cuenta ya existe |
| PUT | `/api/cuentas/{numeroCuenta}` | 200 |
| PATCH | `/api/cuentas/{numeroCuenta}` | 200 |
| DELETE | `/api/cuentas/{numeroCuenta}` | 204 · 409 si tiene movimientos |

```json
{ "numeroCuenta": "478758", "tipoCuenta": "Ahorros", "saldoInicial": 2000, "estado": true, "clienteId": "jlema" }
```

- `tipoCuenta` acepta `Ahorro`, `Ahorros` o `Corriente`, porque el enunciado usa ambas formas.
- El saldo inicial solo puede modificarse mientras la cuenta no tenga movimientos.
- Una cuenta con movimientos **no se elimina físicamente**, para no perder el historial de transacciones. Se desactiva con `PATCH {"estado": false}`.

### Movimientos — puerto 8082

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/movimientos` (paginado: `?numeroCuenta=&page=&size=`) | 200 |
| GET | `/api/movimientos/{id}` | 200 · 404 |
| POST | `/api/movimientos` | 201 · 422 |
| PUT | `/api/movimientos/{id}` | 200 · 422 |
| DELETE | `/api/movimientos/{id}` | 204 · 422 |

```json
{ "numeroCuenta": "478758", "valor": -575 }
```

- **Valor positivo = Depósito, valor negativo = Retiro.** El tipo se deduce del signo.
- La fecha la asigna el servidor.
- La respuesta incluye `saldo`, que es el saldo de la cuenta después del movimiento.
- **F3:** si el movimiento deja la cuenta en negativo, responde:

```json
{
  "timestamp": "2026-10-12T15:00:00Z",
  "status": 422,
  "error": "Unprocessable Content",
  "codigo": "SALDO_NO_DISPONIBLE",
  "mensaje": "Saldo no disponible",
  "path": "/api/movimientos"
}
```

- `PUT` y `DELETE` solo se permiten sobre el **último movimiento** de la cuenta (ver *Decisiones de diseño*).

### Reporte de estado de cuenta (F4) — puerto 8082

```
GET /api/reportes?fecha=2026-10-01,2026-10-31&cliente=mmontalvo
GET /api/reportes?fecha=2026-10-01,2026-10-31&cliente=mmontalvo&formato=plano
```

| Parámetro | Descripción |
|---|---|
| `fecha` | Rango inclusivo `AAAA-MM-DD,AAAA-MM-DD`, respetando la ruta pedida `/reportes?fecha=rango` |
| `cliente` | `clienteId` del cliente |
| `formato` | `detallado` (por defecto) o `plano` |

- **`detallado`:** datos del cliente, totales de créditos y débitos, y cada cuenta con su saldo disponible y el detalle de sus movimientos en el rango.
- **`plano`:** una fila por movimiento con exactamente las claves del ejemplo del enunciado:

```json
[
  {
    "Fecha": "10/2/2022",
    "Cliente": "Marianela Montalvo",
    "Numero Cuenta": "225487",
    "Tipo": "Corriente",
    "Saldo Inicial": 100,
    "Estado": true,
    "Movimiento": 600,
    "Saldo Disponible": 700
  }
]
```

### Formato de errores

Todas las respuestas de error comparten la misma estructura (`timestamp`, `status`, `error`, `codigo`, `mensaje`, `path` y `detalles` cuando aplica).

| Código | Cuándo |
|---|---|
| 400 | Validación de campos (con `detalles` por campo), JSON mal formado, parámetros inválidos |
| 404 | Recurso inexistente |
| 409 | Duplicados, cuenta con movimientos, conflicto de concurrencia |
| 422 | Regla de negocio: saldo no disponible, cuenta inactiva, movimiento no modificable |
| 500 | Error inesperado (sin exponer detalles internos; queda registrado en el log) |

---

## Decisiones de diseño

**Patrones aplicados**

| Patrón | Dónde | Por qué |
|---|---|---|
| Repository | Spring Data JPA | Aísla el acceso a datos |
| Service Layer (interfaz + implementación) | `service/` | Inversión de dependencias; facilita simular en pruebas |
| DTO + Mapper | `dto/`, `mapper/` | La API no expone el modelo de persistencia |
| Builder | `Cliente.builder()` | El cliente tiene 9 atributos; evita constructores ilegibles |
| Factory Method | `Movimiento.registrar()` | Impide crear un movimiento que no haya afectado el saldo |
| Modelo de dominio rico | `Cuenta` | La regla "el saldo nunca es negativo" vive en la entidad |
| Value Object | `RangoFechas` | Parsea y valida el rango del reporte en un solo lugar |
| Publish / Subscribe | RabbitMQ topic exchange | Desacopla los microservicios |
| Observer | `@TransactionalEventListener` | Publica eventos solo después del commit |

**Herencia JPA.** `Persona` es una `@Entity` abstracta con `InheritanceType.JOINED` y `Cliente` la extiende. JOINED deja el esquema normalizado y permite agregar otros tipos de persona sin columnas nulas (SINGLE_TABLE) ni columnas duplicadas (TABLE_PER_CLASS).

**Concurrencia en movimientos.** Dos retiros simultáneos sobre la misma cuenta podrían leer el mismo saldo y aprobarse ambos. Se evita con un **bloqueo pesimista** (`SELECT … FOR UPDATE`) dentro de la transacción, y `@Version` (bloqueo optimista) queda como red de seguridad. Los montos usan `BigDecimal` y `NUMERIC(15,2)`, nunca `double`.

**Solo se modifica o elimina el último movimiento.** Cada movimiento guarda el saldo resultante; cambiar uno intermedio dejaría incoherentes los saldos de todos los posteriores. En un sistema bancario real ni siquiera eso: se registraría un movimiento de reverso.

**Eventos después del commit.** `@TransactionalEventListener(phase = AFTER_COMMIT)` garantiza que nunca se anuncia un cliente cuya transacción hizo rollback.

**Consumidor idempotente.** El consumidor descarta eventos más antiguos que el último aplicado, porque RabbitMQ no garantiza el orden cuando hay reintentos. Reprocesar el mismo evento deja el mismo estado.

### Rendimiento, escalabilidad y resiliencia

| Factor | Implementado | Siguiente paso |
|---|---|---|
| **Rendimiento** | Reporte en 3 consultas fijas (`JOIN FETCH`, `@EntityGraph`, sin N+1); índices `(cuenta_id, fecha)` y `cliente_id`; transacciones `readOnly`; paginación; `open-in-view = false` | Caché de la réplica de clientes |
| **Escalabilidad** | Servicios sin estado (replicables); despliegue independiente; consumidores compitiendo por la misma cola | API Gateway con balanceo; Kubernetes con autoescalado |
| **Resiliencia** | Comunicación asíncrona; reintentos con backoff exponencial y **Dead Letter Queue**; consumidor idempotente; healthchecks y reinicio automático en Docker Compose | **Transactional Outbox**; Circuit Breaker (Resilience4j) si se agregan llamadas síncronas |
| **Seguridad** | Contraseñas con BCrypt; validación de entrada; errores 500 sin detalles internos; contenedores con usuario no-root | OAuth2 / JWT con Spring Security y un servidor de autorización (p. ej. Keycloak) |

---

## Pruebas

| Tipo | Clase | Qué verifica |
|---|---|---|
| Unitaria de dominio (**F5**) | `ClienteTest` | Herencia de Persona, estado por defecto, validaciones |
| Unitaria de dominio | `CuentaTest` | Depósitos, retiros, "Saldo no disponible", cuenta inactiva, anulación |
| Unitaria de servicio | `ClienteServiceImplTest`, `MovimientoServiceImplTest` | Lógica de negocio con Mockito; contraseña cifrada; eventos publicados |
| Unitaria de endpoints | `ClienteControllerTest`, `MovimientoControllerTest` | Códigos HTTP, validaciones y formato de errores (`@WebMvcTest`) |
| Integración (**F6**) | `EstadoCuentaIntegrationTest` | Flujo completo con contexto real y H2: evento de cliente → cuentas → movimientos → "Saldo no disponible" → reporte |

Ejecutar las pruebas (requiere JDK 21):

```bash
cd clientes-service && ./mvnw verify
cd ../cuentas-service && ./mvnw verify
```

Las pruebas no necesitan Docker, Postgres ni RabbitMQ: usan H2 en memoria y el listener de RabbitMQ desactivado (perfil `test`). El reporte de cobertura queda en `target/site/jacoco/index.html`.

### Integración continua

`.github/workflows/ci.yml` se ejecuta en cada push a `main` y `develop` y en cada pull request a `main`:

1. Compila y ejecuta todas las pruebas de ambos microservicios en paralelo.
2. Publica los reportes de cobertura y, si algo falla, los reportes de pruebas.
3. Verifica que las imágenes Docker se construyen.

---

## Ejecución sin Docker (desarrollo)

1. Levantar solo la infraestructura: `docker compose up -d postgres rabbitmq`
2. Ejecutar cada servicio desde el IDE o con `./mvnw spring-boot:run` dentro de su carpeta.

Variables de entorno disponibles (todas tienen valores por defecto para entorno local):

| Variable | Por defecto |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/clientes_db` (o `cuentas_db`) |
| `DB_USER` / `DB_PASSWORD` | `banca` / `banca123` |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `banca` / `banca123` |
| `SERVER_PORT` | `8081` (clientes) / `8082` (cuentas) |

---

## Estructura del repositorio

```
banca-ms-tata/
├── BaseDatos.sql                      # Esquema de ambas bases de datos
├── docker-compose.yml                 # Postgres + RabbitMQ + 2 microservicios
├── postman/
│   └── BancaMicroservicios.postman_collection.json
├── .github/workflows/ci.yml           # Pipeline de integración continua
├── clientes-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/banca/clientes/
│       │   ├── config/  controller/  domain/{model,exception}/  dto/
│       │   ├── event/   exception/   mapper/  repository/  service/{impl}/
│       └── test/java/com/banca/clientes/
└── cuentas-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/com/banca/cuentas/
        │   ├── config/  controller/  domain/{model,exception}/  dto/{reporte}/
        │   ├── exception/  mapper/  messaging/  repository/  service/{impl}/
        └── test/
            ├── java/com/banca/cuentas/
            └── resources/application-test.yml
```

---

**Autor:** Raúl Silva · raul29247@gmail.com