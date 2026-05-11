# ExtractorInfoDB | Enterprise Data Extraction Suite 🚀

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Microsoft SQL Server](https://img.shields.io/badge/SQL%20Server-2022-CC2927?logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20SOLID-blue)](#)

**ExtractorInfoDB** is a high-performance, enterprise-grade microservice designed for the secure extraction and streaming of massive datasets from Microsoft SQL Server environments. Engineered with a "Performance-First" mindset, it bypasses traditional ORM overhead to provide near-native data throughput.

---

## 💎 Core Enterprise Capabilities

### 1. Dynamic Multitenancy & Session-Based Routing 🌐
Unlike standard applications with static connections, ExtractorInfoDB implements a **Dynamic Routing DataSource**. 
- **On-the-fly Connectivity:** Authenticates and routes database traffic based on active user sessions.
- **Secure Isolation:** Each request is executed within the context of the user's specific credentials, ensuring strict data governance.

### 2. High-Throughput Streaming Exports 📊
Engineered for stability during long-running operations:
- **Zero-Memory Footprint:** Uses `StreamingResponseBody` to pipe data directly from the SQL cursor to the HTTP response.
- **Multiple Formats:** Native support for **CSV**, **JSON**, and **XLSX** (Excel) using specialized streaming strategies.
- **Progress Tracking:** Real-time feedback via **Server-Sent Events (SSE)** for large-scale extractions.

### 3. Smart Metadata Explorer 🔍
A built-in engine that provides a structured view of the database ecosystem:
- Automatic schema resolution.
- Real-time table and column discovery.
- Strategy-based engine detection (adaptable to multiple SQL dialects).

### 4. Security-First Architecture 🛡️
- **Injection Prevention:** Metadata-validated table and column identifiers.
- **Context Awareness:** Automatic propagation of session security context to asynchronous background workers.
- **Standardized Error Handling:** RFC 7807 (Problem Details) compliant responses, shielding internal system details.

---

## 🏛️ System Architecture

Adhering to **Clean Architecture** and **SOLID** principles, the codebase is decoupled for maximum maintainability:

```text
com.LlaveMaestra.ExtractorInfoDB
├── config/             # Infrastructure: Dynamic DataSource, SSE, Interceptors
├── controller/         # Presentation: REST Endpoints & Async Handlers
├── service/            # Domain Logic: Export Orchestration, Metadata Discovery
├── strategy/           # Extensibility: Format-specific writers (CSV, Excel, JSON)
├── dto/                # Contracts: API Request/Response models
├── repository/         # Data Access: Performance-tuned JDBC implementations
└── utils/              # Cross-cutting concerns: Global Exception Mapping
```

### Why This Matters (The "Senior" Choice)
- **Performance:** By using `JdbcTemplate` instead of JPA/Hibernate, we eliminate the 20-40% overhead of object-state management during mass extractions.
- **Scalability:** Stateless design combined with HikariCP connection pooling allows for high concurrency with minimal resource usage.

---

## 🛠️ Technology Stack

- **Runtime:** Java 21 LTS (utilizing Records & Virtual Thread compatibility).
- **Framework:** Spring Boot 3.3 (Web, JDBC, Validation).
- **Database Logic:** Spring JDBC + HikariCP.
- **Data Protocols:** SSE (Server-Sent Events) for live updates.
- **Build Tool:** Maven.

---

## 🚀 Getting Started

### Prerequisites
- JDK 21+
- SQL Server instance
- Maven 3.9+

### Configuration (`application.yaml`)
The application is environment-aware. Standard configuration uses placeholders for enterprise deployment:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://${DB_SERVER};databaseName=${DB_DATABASE};encrypt=true;trustServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      leak-detection-threshold: 60000
```

### Execution
```bash
mvn clean spring-boot:run
```

---

## 🔌 API Summary

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/database/explorer` | `GET` | Catalog and Schema discovery. |
| `/api/export` | `POST` | Trigger high-speed data stream. |
| `/api/export/progress/{id}`| `GET` | SSE Stream for real-time progress. |

---

## 📝 License
Proprietary - Developed by **LlaveMaestra** Enterprise Solutions.
