# User Management API

A Spring Boot system for secure registration, login, JWT authentication, email verification, password reset, and role
management. Docker-native, PostgreSQL by default.

## Features

- Registration, login, email verification
- JWT + refresh tokens + logout
- Password reset (email, token)
- Role-based access (ADMIN/USER)
- Swagger docs
- Container/development scripts

## Stack

- Java 17, Spring Boot 3.x
- Spring Security, JPA, JWT
- PostgreSQL 16 (prod/dev), H2 (test)
- Docker, Maven
- Lombok

## Getting Started

### 1. Docker (Recommended)
```bash
git clone <repo>
cd UserManagement
chmod +x docker-scripts/*.sh
./docker-scripts/start.sh
```

App: http://localhost:8082
Swagger: http://localhost:8082/swagger-ui.html

### 2. Dev: Local App, Docker DB
```bash
./docker-scripts/db-only.sh
mvn spring-boot:run
```

App: http://localhost:8081

### 3. Run Tests
```bash
mvn test
```

## Config

Settings via environment/.env. Most important:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/usermanagement_dev
SPRING_DATASOURCE_USERNAME=userapp
SPRING_DATASOURCE_PASSWORD=userapp123
SERVER_PORT=8082
MAIL_HOST=localhost
MAIL_PORT=1025
APP_EMAIL_FROM=noreply@usermanagement.com
APP_BASE_URL=http://localhost:8082
JWT_SECRET=... # set in production
```

## Database

Dockerized PostgreSQL is default. See compose scripts for DB admin.

## Email

- Dev: MailHog on port 1025
- Production: Set real SMTP credentials
- Verification/reset tokens are logged if email send fails.

## API Overview

For full details use Swagger. Main endpoints:

- `/api/auth/register`
- `/api/auth/verify-email`
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/logout`
- `/api/auth/forgot-password`
- `/api/auth/reset-password`
- `/api/profile` (GET/PUT)
- `/api/admin/addRole` (ADMIN only)
- `/actuator/health`

## RBAC & Roles

- First registered user: `ADMIN`. All others: `USER`.
- Admins can assign new roles via API.
- Roles embedded in JWT tokens.

## Docker Scripts

- `start.sh`: App + DB
- `db-only.sh`: DB only
- `stop.sh`: Stop all

## Dev Notes

- Use Docker for DB/dev unless you have PostgreSQL
- Hot reload: `mvn spring-boot:run` with local DB
- Contributions: branch, PR, include tests, follow code style

## Contributing

1. Fork and branch (`git checkout ...`)
2. Commit changes + tests
3. Push, open PR

## Support
- Email: manulorenzop@gmail.com
- Github: open issues for bugs/requests
- Logs: Always check logs for details

## License

MIT

**Author:** Manuel Lorenzo (`noloman`)