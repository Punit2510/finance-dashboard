# Finance Dashboard — Spring Boot Backend

A production-ready backend for a finance dashboard system with JWT authentication, role-based access control, financial record management, and aggregated analytics.

---

## Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Framework | Spring Boot 3.2 | Industry standard, mature ecosystem |
| Security | Spring Security + JWT (jjwt 0.11) | Stateless auth, scalable |
| Database | H2 in-memory | Zero-setup for evaluation; swap with any RDBMS |
| ORM | Spring Data JPA / Hibernate | Clean repository abstraction |
| Validation | Jakarta Bean Validation | Declarative, composable constraints |
| Docs | SpringDoc OpenAPI (Swagger UI) | Auto-generated, always in sync |
| Tests | JUnit 5 + MockMvc + Mockito | Integration + unit coverage |
| Utilities | Lombok | Reduces boilerplate |

---

## Project Structure

```
src/main/java/com/finance/dashboard/
├── config/
│   ├── AppConfig.java          # JPA auditing, seed data
│   ├── OpenApiConfig.java      # Swagger + JWT bearer scheme
│   └── SecurityConfig.java     # Filter chain, CORS, method security
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── TransactionController.java
│   └── UserController.java
├── dto/
│   ├── request/                # Validated inbound payloads
│   └── response/               # Outbound shapes (never expose entities)
├── entity/
│   ├── Transaction.java        # Soft-deletable, audited
│   └── User.java               # Audited, active flag
├── enums/
│   ├── Role.java               # VIEWER | ANALYST | ADMIN
│   └── TransactionType.java    # INCOME | EXPENSE
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── *Exception.java
├── repository/
│   ├── TransactionRepository.java  # Custom JPQL filters + aggregates
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── *Service.java           # Interfaces
    └── impl/*ServiceImpl.java  # Implementations
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
git clone <repo-url>
cd finance-dashboard
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

### Swagger UI
Open **http://localhost:8080/swagger-ui.html** in your browser.

### H2 Console (dev only)
Open **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa` / Password: *(empty)*

---

## Seeded Credentials

Three users are created automatically on startup:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `analyst` | `analyst123` | ANALYST |
| `viewer` | `viewer123` | VIEWER |

10 sample transactions spanning the last 2 months are also seeded.

---

## API Reference

### Authentication

#### `POST /api/auth/login`
```json
// Request
{ "username": "admin", "password": "admin123" }

// Response 200
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

Use the token as: `Authorization: Bearer <token>`

---

### Transactions — `/api/transactions`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `GET` | `/api/transactions` | ALL | List with filters + pagination |
| `GET` | `/api/transactions/{id}` | ALL | Single record |
| `POST` | `/api/transactions` | ADMIN | Create |
| `PUT` | `/api/transactions/{id}` | ADMIN | Update |
| `DELETE` | `/api/transactions/{id}` | ADMIN | Soft delete |

#### Query Parameters for `GET /api/transactions`

| Param | Type | Example | Description |
|---|---|---|---|
| `type` | enum | `INCOME` or `EXPENSE` | Filter by type |
| `category` | string | `Salary` | Partial, case-insensitive |
| `from` | date | `2025-01-01` | Start date (inclusive) |
| `to` | date | `2025-03-31` | End date (inclusive) |
| `page` | int | `0` | Zero-based page number |
| `size` | int | `10` | Page size |

#### Create / Update Request Body
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2025-03-01",
  "notes": "Monthly salary"
}
```

---

### Dashboard — `/api/dashboard`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | ANALYST, ADMIN | Full aggregated summary |

#### Response shape
```json
{
  "totalIncome": 183500.00,
  "totalExpenses": 4750.00,
  "netBalance": 178750.00,
  "incomeByCategory": { "Salary": 175000.00, "Freelance": 5000.00, "Investments": 3500.00 },
  "expenseByCategory": { "Rent": 1200.00, "Groceries": 1050.00, "Utilities": 200.00 },
  "monthlyTrends": [
    { "year": 2025, "month": 2, "income": 93500.00, "expenses": 2300.00, "net": 91200.00 },
    { "year": 2025, "month": 3, "income": 90000.00, "expenses": 2450.00, "net": 87550.00 }
  ],
  "recentTransactions": [ ... ]
}
```

---

### User Management — `/api/users`  *(ADMIN only)*

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `POST` | `/api/users` | Create user |
| `PUT` | `/api/users/{id}` | Update email / role / status |
| `DELETE` | `/api/users/{id}` | Deactivate user |

#### Create User Request
```json
{
  "username": "jane",
  "email": "jane@finance.com",
  "password": "securepass",
  "role": "ANALYST"
}
```

---

## Access Control Matrix

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| `GET /api/transactions` | ✅ | ✅ | ✅ |
| `POST /api/transactions` | ❌ | ❌ | ✅ |
| `PUT /api/transactions/{id}` | ❌ | ❌ | ✅ |
| `DELETE /api/transactions/{id}` | ❌ | ❌ | ✅ |
| `GET /api/dashboard/summary` | ❌ | ✅ | ✅ |
| `GET /api/users` | ❌ | ❌ | ✅ |
| `POST /api/users` | ❌ | ❌ | ✅ |
| `PUT/DELETE /api/users/{id}` | ❌ | ❌ | ✅ |

Access control is enforced via Spring Security `@PreAuthorize` annotations — not just at the route level but per-method in the service layer, making it impossible to bypass via internal calls.

---

## Running Tests

```bash
mvn test
```

Tests include:
- **AuthControllerTest** — login happy path, bad credentials, validation
- **TransactionControllerTest** — full role matrix (VIEWER/ANALYST/ADMIN), filters, soft delete, validation
- **DashboardControllerTest** — role gate (ANALYST+ADMIN allowed, VIEWER denied)
- **UserServiceTest** — unit tests for duplicate detection and user creation logic

---

## Design Decisions & Assumptions

### Soft Deletes
Transactions are never physically removed. A `deleted` boolean flag hides them from all queries. This preserves audit trails and allows recovery.

### Users deactivated, not deleted
`DELETE /api/users/{id}` sets `active = false`. A deactivated user's JWT is rejected by Spring Security's `UserDetails.isEnabled()` check automatically — no extra logic needed.

### Roles are flat (no hierarchy)
VIEWER, ANALYST, and ADMIN are sibling roles. ADMIN does not automatically inherit ANALYST or VIEWER permissions — each endpoint specifies exactly which roles are allowed. This is intentional: it keeps access rules explicit and auditable.

### Password hashing
BCrypt with default strength (10 rounds). Passwords are never returned in any response DTO.

### JWT expiry
Tokens expire in 24 hours (configurable via `app.jwt.expiration-ms`). No refresh token mechanism is included (out of scope for this assessment).

### In-memory database
H2 is used for zero-setup evaluation. Switching to PostgreSQL or MySQL requires only changing three lines in `application.properties` and adding the JDBC driver dependency — no code changes.

### Category field
Category is a free-text string (not a separate entity). This avoids over-engineering for a dashboard system where categories are user-defined and flexible.

### No ANALYST write access
ANALYST can read transactions and access analytics but cannot create or modify records. This matches the common real-world pattern where analysts consume data but do not author it.

---

## Potential Production Enhancements

- Refresh token endpoint
- Rate limiting (Bucket4j or Spring's built-in)
- PostgreSQL with Flyway migrations
- Actuator health / metrics endpoints
- Docker + docker-compose setup
- CI pipeline (GitHub Actions)
