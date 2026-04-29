# ExtractorInfoDB

ExtractorInfoDB es un microservicio construido con **Spring Boot** diseñado para extraer y exponer información histórica de proyectos y honorarios desde una base de datos SQL Server. El proyecto ha sido refactorizado para seguir estrictamente los principios de **Clean Architecture** y **SOLID**, garantizando un código empresarial altamente mantenible, escalable y con un rendimiento óptimo.

---

## 🏛️ Arquitectura del Proyecto

El proyecto está estructurado en capas bien definidas para asegurar la separación de responsabilidades:

```text
com.LlaveMaestra.ExtractorInfoDB
├── config/             # Configuraciones de la aplicación (propiedades, beans)
├── controller/         # Capa de presentación (Endpoints REST HTTP)
├── domain/             # Entidades puras de negocio (Sin dependencias externas)
├── dto/response/       # Objetos de Transferencia de Datos (DTOs) hacia el cliente
├── repository/         # Contratos (Interfaces) de acceso a datos
│   └── Impl/           # Implementaciones concretas de acceso a datos (JDBC)
├── service/            # Lógica de negocio y orquestación
└── utils/Exception/    # Manejo global y centralizado de errores
```

### Ventajas de esta Estructura (Nivel Senior / Enterprise)

1. **Single Responsibility (Principio de Responsabilidad Única - S):**
   - El **Controller** solo sabe de HTTP (Rutas, Códigos de Estado).
   - El **Service** solo sabe de reglas de negocio y orquestación.
   - El **Repository** solo sabe de SQL y mapeo de datos.
   - Los **DTOs** solo sirven para moldear la respuesta al cliente.

2. **Dependency Inversion (Inversión de Dependencias - D):**
   - La lógica de negocio no depende de la base de datos directamente, sino de una interfaz (`HistoricoProyectoRepository`). Esto permite que el día de mañana se pueda cambiar de SQL Server a MongoDB o PostgreSQL creando una nueva implementación sin tocar la lógica central.

3. **Domain-Driven:**
   - La entidad `HistoricoProyecto` es un `record` puro de Java. No tiene anotaciones de frameworks (como `@Table` o `@Entity` de JPA), lo que la hace ultra-ligera e independiente.

4. **Alto Rendimiento (Sin ORM):**
   - Se descartó el uso de Hibernate/JPA en favor de **`JdbcTemplate`** nativo con un `RowMapper` estático. Esto reduce drásticamente el consumo de memoria (overhead) y ofrece la máxima velocidad posible para extracciones masivas de datos.

5. **Seguridad y Robustez:**
   - El uso del `GlobalExceptionHandler` captura excepciones y las traduce a formato estándar `ProblemDetail` (RFC 7807), evitando que el usuario o atacantes vean el stacktrace interno de la base de datos o de la aplicación.

---

## 🔌 Conexión a la Base de Datos

La gestión de la conexión a SQL Server está totalmente delegada a la auto-configuración nativa de **Spring Boot**. 

### ¿Cómo funciona?

1. **Dependencia Principal:** En el `pom.xml` se utiliza `spring-boot-starter-jdbc`. Esto le indica a Spring Boot que debe preparar el entorno para acceso a datos.
2. **Pool de Conexiones (HikariCP):** Spring Boot incluye y configura automáticamente **HikariCP**, el pool de conexiones JDBC más rápido del mercado para Java.
3. **Inyección Transparente:** Spring Boot lee las credenciales desde `application.yaml` (las cuales se inyectan dinámicamente mediante variables de entorno `.env`) y crea un `DataSource` (el pool).
4. **JdbcTemplate:** Spring Boot toma ese `DataSource` y construye un `JdbcTemplate`, el cual se inyecta automáticamente en nuestro `HistoricoProyectoJdbcRepositoryImpl`.

### Configuración (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://${DB_SERVER};databaseName=${DB_DATABASE_DOS};encrypt=true;trustServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      pool-name: SQLServer-Pool
      maximum-pool-size: 10
      connection-timeout: 30000
```
*No es necesario abrir, cerrar conexiones, ni manejar `try-catch` manualmente. Spring Boot gestiona el ciclo de vida, devuelve la conexión al pool de forma segura y maneja la liberación de recursos.*

---

## 🚀 Endpoints Disponibles

La API expone los siguientes recursos bajo la ruta base `/api/v1/historico-proyectos`:

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/top` | Obtiene los primeros registros (Top) del histórico de proyectos. |
| `GET` | `?anio={anio}&mes={mes}` | Filtra los registros históricos especificando el año y el mes exacto. |

---

## 🛠️ Stack Tecnológico

- **Java 21**: Uso de características modernas como `records`, Text Blocks (`"""`), y pattern matching.
- **Spring Boot 3.x / 4.x**: Auto-configuración, IoC Container, y servidor Tomcat embebido.
- **Spring JDBC (`JdbcTemplate`)**: Acceso a datos de alto rendimiento.
- **HikariCP**: Connection Pooling óptimo.
- **Microsoft SQL Server JDBC Driver**: Controlador nativo para SQL Server.
- **Lombok**: Reducción de código repetitivo (Boilerplate) como getters, setters y constructores.
