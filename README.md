# Sistema de Gestión de Préstamos Bancarios

Sistema Full Stack para gestión de préstamos bancarios desarrollado con Java 21, Spring Boot 3, Angular 19 y PostgreSQL.

## Inicio Rápido con Docker

> **Requisitos**: Solo [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado. No requiere Java, Maven, Node ni Angular CLI.

```bash
# 1. Clonar y navegar al proyecto
cd Test_Loans_Makers

# 2. Copiar variables de entorno
cp .env.example .env
# Editar .env y reemplazar los valores CHANGE_ME (o usar los defaults para desarrollo)

# 3. Construir y levantar todos los servicios
docker compose up --build -d

# 4. Verificar que todos los servicios están saludables
docker compose ps

# 5. Ver logs en tiempo real
docker compose logs -f

# 6. Detener servicios
docker compose down
```

### Usuarios de Demostración

| Rol | Email | Contraseña |
|---|---|---|
| Usuario | usuario@test.com | 123 |
| Administrador | admin@test.com | 123 |

> Solo disponibles con perfil `dev` (activado por defecto en Docker).

### Limpieza y Reinicio

```bash
# Detener servicios (conservar datos)
docker compose down

# Detener y eliminar volúmenes (reset completo de BD y Redis)
docker compose down -v

# Reconstruir imágenes desde cero (sin caché)
docker compose build --no-cache

# Reconstruir y levantar
docker compose up --build -d
```

### Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `DB_NAME` | `loans_db` | Nombre de la base de datos PostgreSQL |
| `DB_USERNAME` | `bankloan` | Usuario de PostgreSQL |
| `DB_PASSWORD` | (ver `.env`) | Contraseña de PostgreSQL |
| `DB_PORT` | `5432` | Puerto externo de PostgreSQL |
| `REDIS_PORT` | `6379` | Puerto externo de Redis |
| `JWT_SECRET` | (ver `.env`) | Clave secreta JWT (Base64, mín. 256 bits) |
| `JWT_EXPIRATION_MS` | `3600000` | Tiempo de expiración del token JWT (ms) |

---

## Preguntas Teóricas

### 1. Optimización en sistemas financieros

**¿Qué estrategias aplicarías para mejorar el rendimiento y la escalabilidad?**

Implementaría una arquitectura de microservicios con Spring Boot, utilizando WebFlux para operaciones de alta concurrencia cuando el flujo sea realmente no bloqueante, aplicaría Redis para cachear información de consulta, evitando almacenar saldos como fuente de verdad. Utilizaría patrones como Circuit Breaker, CQRS y procesamiento asíncrono con Kafka, también, optimizaría las consultas SQL, implementaría índices y escalaría horizontalmente según la demanda, garantizando siempre la consistencia de las transacciones.

**¿Cómo protegerías una API contra SQL Injection, CSRF, XSS y otros ataques?**

Implementaría Spring Security con OAuth 2.0 y JWT, aplicando autenticación, autorización por roles y principio de mínimo privilegio. Para prevenir SQL Injection utilizaría consultas parametrizadas; para XSS, validación y codificación de salidas; y para CSRF, tokens de protección cuando se utilice autenticación basada en cookies. Complementaría con HTTPS, cifrando datos sensibles, rate limiting, auditoría y gestión segura de secretos.

**¿Cómo implementarías la consistencia y el manejo de errores en transferencias bancarias?**

Utilizaría el patrón Saga con orquestación para coordinar las operaciones entre microservicios, implementando compensaciones cuando alguna etapa falle, aplicaría Transactional Outbox para garantizar la persistencia de eventos, idempotencia para evitar transacciones duplicadas y reintentos controlados, si se necesita realizar movimientos de dinero, mantendría débito y crédito dentro de una misma transacción ACID cuando sea posible, utilizando conciliación y estados transaccionales para garantizar la integridad financiera.

**¿Cómo diseñarías una suite de pruebas para una API bancaria?**

Implementaría una estrategia de pruebas por niveles. Para las unitarias utilizaría JUnit 5 y Mockito, validando reglas de negocio, saldos, límites y excepciones. Para integración utilizaría Testcontainers con PostgreSQL y Spring Boot Test, verificando persistencia, concurrencia y transacciones. Complementaría con pruebas de contratos mediante Pact, pruebas de rendimiento con k6 y análisis de calidad con SonarQube, priorizando escenarios críticos sobre el porcentaje de cobertura.

**¿Cómo gestionarías el estado y la autenticación en una aplicación bancaria Angular?**

Utilizaría Angular con Signals para estados locales y NgRx para estados globales complejos, implementaría autenticación mediante OAuth 2.0 con OpenID Connect, preferiblemente usando un Backend for Frontend con sesiones seguras y cookies HttpOnly, aplico Guards e Interceptors, teniendo claro que la autorización real debe validarse en el backend.

### 2. Spring Boot

**¿Qué sucede internamente cuando utilizas @SpringBootApplication?**

Cuando utilizo @SpringBootApplication, Spring Boot combina tres anotaciones principales: @Configuration, @EnableAutoConfiguration y @ComponentScan esto permite registrar configuraciones, detectar componentes y configurar automáticamente dependencias según el classpath. Durante el arranque, Spring crea el ApplicationContext, inicializa los beans y levanta el servidor embebido cuando corresponde, reduciendo considerablemente la configuración manual.

**¿Cómo funciona el ciclo de vida de un bean en Spring y cómo podrías intervenir?**

Spring se encarga de crear el bean, inyectar sus dependencias, ejecutar los procesos de inicialización y administrarlo durante su ciclo de vida, puedo intervenir utilizando @PostConstruct para inicializar recursos, @PreDestroy para liberarlos y BeanPostProcessor para personalizar su comportamiento antes o después de la inicialización, me permite controlar recursos y ejecutar lógica específica sin afectar la responsabilidad principal del componente.

**¿Cómo personalizarías la autoconfiguración sin romper la filosofía de convención sobre configuración?**

Primero aprovecharía las configuraciones disponibles en application.yml y utilizaría @ConfigurationProperties para manejar propiedades tipadas. Cuando necesite un comportamiento específico, definiría beans personalizados mediante @Configuration, aprovechando que muchas autoconfiguraciones utilizan @ConditionalOnMissingBean, evito excluir autoconfiguraciones innecesariamente, buscando mantener una solución sencilla, extensible y alineada con las convenciones de Spring Boot.
