# 💰 Finance Dashboard — Spring Boot Backend

A production-ready backend system built with Spring Boot featuring JWT authentication, role-based access control, financial record management, and analytics.

---

## 🌐 Live Deployment

* **Base URL:**
  https://finance-dashboard-tbf8.onrender.com

* **Swagger UI:**
  https://finance-dashboard-tbf8.onrender.com/swagger-ui/index.html

* **API Docs:**
  https://finance-dashboard-tbf8.onrender.com/v3/api-docs

---

## 🧰 Tech Stack

| Layer     | Technology                  |
| --------- | --------------------------- |
| Backend   | Spring Boot 3               |
| Security  | Spring Security + JWT       |
| Database  | PostgreSQL (Render)         |
| ORM       | Hibernate / JPA             |
| Docs      | Swagger (SpringDoc OpenAPI) |
| Build     | Maven                       |
| Utilities | Lombok                      |

---

## 🔐 Authentication (JWT)

This project uses **JSON Web Tokens (JWT)** for stateless authentication.

### 🔁 Flow:

1. User logs in via `/api/auth/login`
2. Server generates JWT token
3. Client sends token in headers:

   ```
   Authorization: Bearer <token>
   ```
4. Backend validates token for each request

---

## 🔑 JWT Configuration

```properties
app.jwt.secret=${APP_JWT_SECRET}
app.jwt.expiration-ms=86400000
```

* Secret is used to **sign and validate tokens**
* Expiry = **24 hours**

⚠️ Never expose JWT secret in code

---

## ⚙️ Environment Variables (Render)

Set these in Render:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>

APP_JWT_SECRET=your_secure_random_key
APP_JWT_EXPIRATION_MS=86400000
```

---

## 🏗️ Project Structure

```
com.finance.dashboard
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── security
└── service
```

---

## 🔐 Default Users (Seeded)

| Username | Password   | Role    |
| -------- | ---------- | ------- |
| admin    | admin123   | ADMIN   |
| analyst  | analyst123 | ANALYST |
| viewer   | viewer123  | VIEWER  |

---

## 📊 API Overview

### 🔐 Auth

* `POST /api/auth/login`

---

### 💸 Transactions

* `GET /api/transactions`
* `GET /api/transactions/{id}`
* `POST /api/transactions` (ADMIN)
* `PUT /api/transactions/{id}` (ADMIN)
* `DELETE /api/transactions/{id}` (ADMIN)

---

### 📊 Dashboard

* `GET /api/dashboard/summary` (ANALYST, ADMIN)

---

### 👤 Users (ADMIN only)

* `GET /api/users`
* `GET /api/users/{id}`
* `POST /api/users`
* `PUT /api/users/{id}`
* `DELETE /api/users/{id}`

---

## 🧪 Example Login

### Request

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

---

## 🔐 Access Control

| Endpoint                       | VIEWER | ANALYST | ADMIN |
| ------------------------------ | :----: | :-----: | :---: |
| Transactions (GET)             |    ✅   |    ✅    |   ✅   |
| Transactions (POST/PUT/DELETE) |    ❌   |    ❌    |   ✅   |
| Dashboard                      |    ❌   |    ✅    |   ✅   |
| Users                          |    ❌   |    ❌    |   ✅   |

---

## ▶️ How to Run Locally

```bash
git clone <repo-url>
cd finance-dashboard
mvn spring-boot:run
```

App runs on:

```
http://localhost:8080
```

Swagger:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧠 Key Features

* JWT Authentication
* Role-based Authorization
* RESTful APIs
* PostgreSQL Integration
* Swagger Documentation
* Production Deployment (Render)

---

## 🚀 Future Improvements

* Refresh tokens
* Rate limiting
* Docker support
* CI/CD pipeline
* Flyway migrations

---

## 🎯 Author

Punit Ranjan
Backend Developer | Spring Boot | System Design
