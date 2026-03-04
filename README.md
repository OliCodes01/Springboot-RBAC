# JWT Authentication REST API

> Built by Olivier Heins — a portfolio project showing full-stack Java Spring Boot development with JWT security, RBAC, Docker, and CI/CD.

---

## Disclaimer

This repository intentionally includes the RSA private and public key files used to sign and verify JWT tokens, and the .env file.

> In a real production project, I would never commit private keys or secrets. The right approach is to use environment variables, a secrets manager (AWS Secrets Manager, Azure Key Vault, etc.), or CI/CD secret injection. These files are committed here only so that anyone can clone the repo and run `docker compose up --build` right away, without any extra setup.

---

## What This App Does

- Users can register, log in, and log out
- Authentication uses JWT tokens stored in HTTP-only cookies (secure, not readable by JavaScript)
- Tokens are signed with RSA-256 (asymmetric keys)
- There are two roles: USER and ADMIN
- Regular users can view and edit their own profile
- Admins can view, edit, and delete any user
- The app comes with 5 demo users already in the database

## Demo Accounts

| Username    | Password      | Role  |
|-------------|---------------|-------|
| john_doe    | password123   | ADMIN |
| jane_smith  | password123   | USER  |
| alice_wonder| password123   | USER  |
| bob_builder | password123   | USER  |
| carol_tech  | password123   | USER  |

## Architecture & Design Decisions

### Stateless Monolith

This is a stateless monolithic application. The API and frontend are separate services, but the backend is a single deployable unit. Stateless means the server does not store any session data. Every request must carry everything the server needs to identify and authorise the user. There is no session table in the database and no in-memory session store.

### How JWT Fits In

Because the server keeps no session state, the client needs to prove who they are on every request. This is done with a JSON Web Token (JWT).

A JWT is a small, self-contained piece of data with three parts:
- **Header** — the algorithm used to sign it (`RS256` in this app)
- **Payload** — the claims (data embedded in the token)
- **Signature** — a cryptographic signature that proves the token was issued by this server and hasn't been tampered with

When a user logs in, the server creates a JWT, signs it with its private RSA key, and sends it to the browser inside an HttpOnly cookie. On every request after that, the browser sends the cookie automatically. The server reads the token, verifies the signature using the public RSA key, and knows who is making the request.

### Why RSA-256 Instead of HS256?

There are two common signing strategies:

| Strategy | Keys | Risk |
|---|---|---|
| **HS256** (symmetric) | One shared secret key | If the secret leaks, anyone can create tokens |
| **RS256** (asymmetric) | Private key signs, public key verifies | The private key never leaves the server, the public key can be shared freely |

This app uses RS256. The private key lives only on the server and is used to sign tokens. The public key is used to verify them. Even if someone got the public key, they could not forge a token without the private key.

In a real environment, the private key would never be in the codebase. Common approaches are:
**Environment variable** — the PEM content is injected at runtime by the deployment platform (e.g. a CI/CD pipeline, Kubernetes secret, or Heroku config var) and read into memory on startup.
**Secrets manager** — services like AWS Secrets Manager or Azure Key Vault store the key, and the app fetches it on boot. Access is controlled by IAM roles so only the app can retrieve it.
**Key Management Service (KMS)** — for higher security, the private key never leaves the KMS at all. You send data to the KMS and it does the signing for you — your app never holds the raw key bytes.

### What Is Stored in the JWT — and Why

The JWT payload in this app contains only one claim: the user's userId (a UUID, stored as the subject).
```
Subject: "550e8400-e29b-41d4-a716-446655440000"  ← userId only
```

**No username. No role. No email.**

This is a deliberate choice. Here's the reasoning:

- **Minimal exposure** — the less data in a token, the less information is exposed if a token is intercepted.
- **Always up-to-date permissions** — if you store the user's role in the JWT and an admin then changes that role, the old token still shows the old role until it expires. By storing only the userId, every request fetches the current role from the database — permissions are always current.
- **Single source of truth** — the database is authoritative. The JWT is just a trusted pointer to a user record, not a copy of it.
The trade-off is one extra database lookup per authenticated request. For most applications this is fine.

---

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3.4
- Spring Security (stateless JWT)
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven

**Frontend**
- Vue.js 3 (Composition API)
- Axios
- Vite
- Nginx (production)

**DevOps**
- Docker & Docker Compose
- GitHub Actions CI/CD

## Quick Start with Docker

The easiest way to run the full application:

```bash
git clone https://github.com/OliCodes01/Springboot-RBAC.git
cd Springboot-RBAC
docker compose up --build
```

Then open **http://localhost** in your browser.

- Frontend runs on port **80**
- Backend API runs on port **8080**
- PostgreSQL runs on port **5433**

To stop everything:

```bash
docker compose down
```

To also remove the database data:

```bash
docker compose down -v
```

## Run Without Docker (Development)

### Prerequisites

- Java 21
- Maven
- Node.js 20+
- PostgreSQL

### 1. Start PostgreSQL

Make sure PostgreSQL is running and create a database:

```sql
CREATE DATABASE jwt_app_db;
```

### 2. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:3000**.

## API Endpoints

### Authentication (no token required)

| Method | Endpoint         | Description          |
|--------|------------------|----------------------|
| POST   | `/auth/register` | Register a new user  |
| POST   | `/auth/login`    | Log in               |
| POST   | `/auth/logout`   | Log out              |

### User (token required)

| Method | Endpoint               | Description                    | Access |
|--------|------------------------|--------------------------------|--------|
| GET    | `/user/profile`        | Get your profile               | Any    |
| PUT    | `/user/profile`        | Update your profile            | Any    |
| POST   | `/user/change-password`| Change your password           | Any    |
| GET    | `/user/all`            | List all users                 | Admin  |
| PUT    | `/user/{id}`           | Update any user                | Admin  |
| DELETE | `/user/{id}`           | Delete a user                  | Admin  |

### Health Check

| Method | Endpoint             | Description         |
|--------|----------------------|---------------------|
| GET    | `/actuator/health`   | Application health  |

## Running Tests

```bash
cd backend
mvn clean test
```

Tests use an in-memory H2 database so no PostgreSQL is needed.

## Project Structure

```
├── backend/                  # Spring Boot API
│   ├── src/main/java/com/jwtapp/
│   │   ├── config/           # Security, CORS configuration
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # JPA entities
│   │   ├── exception/        # Custom exceptions & global handler
│   │   ├── filter/           # JWT authentication filter
│   │   ├── init/             # Database seeder with demo data
│   │   ├── repository/       # JPA repositories
│   │   ├── service/          # Business logic
│   │   └── util/             # JWT utility, constants
│   ├── src/main/resources/
│   │   ├── application.yml   # App configuration
│   │   └── keys/             # RSA key pair for JWT signing
│   ├── src/test/             # Unit & integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                 # Vue.js SPA
│   ├── src/
│   │   ├── pages/            # Login, Register, Home, Profile, Users
│   │   ├── services/         # Auth service (API calls)
│   │   ├── api.js            # Axios instance
│   │   └── router.js         # Simple client-side router
│   ├── nginx.conf            # Production Nginx config
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml        # One-command setup
├── .github/workflows/ci.yml  # CI/CD pipeline
└── README.md
```

## Environment Variables

The backend reads these environment variables (all have sensible defaults for development):

| Variable               | Default             | Description                |
|------------------------|---------------------|----------------------------|
| `DB_HOST`              | localhost           | PostgreSQL host            |
| `DB_PORT`              | 5432                | PostgreSQL port            |
| `DB_NAME`              | jwt_app_db          | Database name              |
| `DB_USER`              | postgres            | Database user              |
| `DB_PASSWORD`          | postgres            | Database password          |
| `SERVER_PORT`          | 8080                | Backend port               |
| `JWT_EXPIRATION`       | 3600000             | Token lifetime (ms)        |
| `CORS_ALLOWED_ORIGINS` | http://localhost:3000| Allowed CORS origins       |


