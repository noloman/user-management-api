# User Management API

A comprehensive Spring Boot application for user registration, authentication, and role-based authorization using JWT
tokens with PostgreSQL database and Docker support.

## 🚀 Features

- **User Registration & Authentication** - Secure user account creation and login
- **JWT Token-based Security** - Stateless authentication using JSON Web Tokens
- **Refresh Token Support** - Automatic token renewal with secure refresh tokens (7-day expiry)
- **User Profile Management** - Get and update user profiles with username, email, full name, bio, and image URL
- **Role-based Access Control (RBAC)** - Admin and User roles with different permissions
- **Auto Role Assignment** - First user gets ADMIN role, subsequent users get USER role
- **Secure Logout** - Token invalidation for proper session management
- **Comprehensive Logging** - Detailed logging throughout the application
- **API Documentation** - Interactive Swagger/OpenAPI documentation
- **PostgreSQL Database** - Production-ready database with Docker support
- **Docker Integration** - Full containerization with Docker Compose
- **Multiple Profiles** - Separate configurations for local development and Docker deployment
- **Health Checks** - Built-in health monitoring endpoints
- **Extensive Testing** - Unit and integration tests with high coverage

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Security 6.5.1**
- **Spring Data JPA**
- **JWT (JSON Web Tokens)**
- **PostgreSQL 16** - Primary database
- **H2 Database** - For testing only
- **Docker & Docker Compose**
- **Lombok**
- **OpenAPI/Swagger**
- **Maven**
- **JUnit 5 & Mockito**

## 📋 Prerequisites

### Required (for Docker setup):

- **Docker Desktop** or Docker Engine
- **Docker Compose**

### Optional (for local development only):
- Java 17 or higher
- Maven 3.6 or higher
- IDE (IntelliJ IDEA, VS Code, or Eclipse)

**Note**: PostgreSQL is not required locally - it runs in Docker containers.

## 🏃‍♂️ Getting Started

### Option 1: Full Docker Setup (Recommended)

This is the easiest way to get everything running with no local dependencies.

```bash
# Clone the repository
git clone <repository-url>
cd UserManagement

# Make scripts executable
chmod +x docker-scripts/*.sh

# Start everything with Docker
./docker-scripts/start.sh
```

The application will be available at:

- **Application**: http://localhost:8082
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **Health Check**: http://localhost:8082/actuator/health

### Option 2: Local Development with Docker Database

For development with hot reload while keeping the database in Docker:

```bash
# Start only PostgreSQL with Docker
./docker-scripts/db-only.sh

# Run the Spring Boot app locally (requires Java 17+ and Maven)
mvn spring-boot:run
```

The application will be available at:

- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html

**Note**: This option requires Java 17+ and Maven locally, but is useful for development with Spring Boot DevTools hot
reload.

### Hot Reload Development

For the best development experience with automatic restarts:

```bash
# Start PostgreSQL database in Docker
./docker-scripts/db-only.sh

# Run app locally with Spring Boot DevTools (in another terminal)
mvn spring-boot:run
```

**Benefits of this approach:**

- Fast application restarts on code changes
- Live reload of static resources
- Direct database access for debugging
- No local PostgreSQL installation required
- Database persists between app restarts

**Alternative**: Use full Docker setup and rebuild container for changes:

```bash
# Make code changes, then rebuild and restart
docker-compose up --build -d app
```

### ~~Option 3: Traditional Local Setup~~

**Not recommended** - Requires manual PostgreSQL installation and configuration. Use Docker options above instead.

## 🗄️ Database Setup

### Docker Setup (Default)

The Docker setup automatically handles PostgreSQL configuration - no manual setup required!

**Automatic Configuration:**
- **Database**: `usermanagement_dev`
- **Username**: `userapp`
- **Password**: `userapp123`
- **Port**: `5432` (mapped to host)

**Database Operations:**

```bash
# View database logs
docker-compose logs postgres

# Connect to database (requires psql client)
docker-compose exec postgres psql -U userapp -d usermanagement_dev

# Reset database (removes all data)
docker-compose down -v
docker-compose up -d postgres
```

### Manual PostgreSQL Setup (Not Recommended)

Only needed if you want to run PostgreSQL outside Docker:

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE usermanagement_dev;
CREATE USER userapp WITH PASSWORD 'userapp123';
GRANT ALL PRIVILEGES ON DATABASE usermanagement_dev TO userapp;
```

**Note**: Manual setup requires PostgreSQL 12+ installation and configuration.

## 🐳 Docker Commands

### Essential Docker Scripts

```bash
# Start everything (database + application)
./docker-scripts/start.sh

# Start only PostgreSQL database
./docker-scripts/db-only.sh

# Stop all services
./docker-scripts/stop.sh
```

### Direct Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services (keep data)
docker-compose down

# Stop and remove all data
docker-compose down -v
```

### Rebuild Application Container

```bash
# Rebuild and restart app container
docker-compose up --build -d app

# Force rebuild from scratch
docker-compose build --no-cache app
```

## 📚 API Endpoints

### Authentication Endpoints

#### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "mySecurePassword123"
}
```

#### Login User

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "mySecurePassword123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-value"
}
```

#### Refresh Token

```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "refresh-token-value"
}
```

**Response:**

```json
{
  "token": "new-jwt-token-value",
  "refreshToken": "new-refresh-token-value"
}
```

#### Logout

```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**

```json
"Logged out successfully"
```

### Admin Endpoints (Requires ADMIN role)

#### Add Role to User

```http
POST /api/admin/addRole?username=john_doe&roleName=USER
Authorization: Bearer <jwt-token>
```

#### Test Admin Access

```http
POST /api/admin/test
Authorization: Bearer <jwt-token>
```

### Profile Endpoints

#### Get User Profile

```http
GET /api/profile
Authorization: Bearer <jwt-token>
```

**Response:**

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "bio": "Software developer",
  "imageUrl": "https://example.com/image.jpg"
}
```

#### Update User Profile

```http
PUT /api/profile
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "email": "john2@example.com",
  "fullName": "John Doe Updated",
  "bio": "Software developer and architect"
}
```

### Health & Monitoring

```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 🔐 Authentication & Authorization

### JWT Token Usage

The application uses a dual-token system for enhanced security:

#### Access Tokens

- **Lifetime**: 15 minutes
- **Usage**: Include in Authorization header for API requests
- **Format**: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

#### Refresh Tokens

- **Lifetime**: 7 days
- **Purpose**: Generate new access tokens when they expire
- **Storage**: Securely stored in database, one per user
- **Rotation**: New refresh token issued on each login (invalidates previous)

### Token Flow

1. **Login**: Receive both access token and refresh token
2. **API Requests**: Use access token in Authorization header
3. **Token Expiry**: When access token expires (15min), use refresh token
4. **Refresh**: Send refresh token to `/api/auth/refresh` for new access token
5. **Logout**: Invalidate refresh token via `/api/auth/logout`

### Roles

- **ADMIN**: Full access to all endpoints including user management
- **USER**: Standard user access (can be extended for future features)

### Role Assignment Logic

- **First registered user**: Automatically assigned ADMIN role
- **Subsequent users**: Automatically assigned USER role
- **Additional roles**: Can be added by ADMIN users via `/api/admin/addRole`

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Test Coverage

The project includes comprehensive tests:

- **Unit Tests**: Service layer, security components, utilities
- **Integration Tests**: Repository layer, database operations (uses H2)
- **Controller Tests**: API endpoints (standalone MockMvc tests)

### Test Structure

```
src/test/java/
├── controller/
│   ├── AdminControllerTest.java
│   └── AuthControllerTest.java
├── repository/
│   ├── RoleRepositoryTest.java
│   └── UserRepositoryTest.java
├── security/
│   └── JwtUtilTest.java
└── service/
    ├── CustomUserDetailsServiceTest.java
    └── UserServiceTest.java
```

## ⚙️ Configuration Profiles

### Default Profile (`application.yml`)

Used for local development:

- Database: `localhost:5432`
- Port: `8081`
- Verbose logging and SQL output

### Docker Profile (`application-docker.yml`)

Used in Docker containers:

- Database: `postgres:5432` (Docker service name)
- Environment variable configuration
- Optimized logging and performance settings
- File logging to `/app/logs/`

### Profile Activation

```bash
# Local development (default)
mvn spring-boot:run

# Docker profile (automatically set in docker-compose)
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run

# Custom profile
mvn spring-boot:run -Dspring.profiles.active=production
```

## 🛠️ Development

### Database Initialization

The application automatically seeds initial roles:

- `ADMIN` role
- `USER` role

This is handled by the `RoleSeeder` component on application startup.

## 📖 API Testing

### Using cURL (Docker setup - port 8082)

#### 1. Register first user (becomes ADMIN)

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### 2. Login to get JWT token and refresh token

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Response includes both tokens:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 3. Use JWT token for protected endpoints

```bash
curl -X POST http://localhost:8082/api/admin/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 4. Refresh access token when expired

```bash
curl -X POST http://localhost:8082/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}'
```

#### 5. Logout to invalidate refresh token

```bash
curl -X POST http://localhost:8082/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}'
```

### Using Swagger UI

1. Navigate to http://localhost:8082/swagger-ui.html (Docker) or http://localhost:8081/swagger-ui.html (local)
2. Click "Authorize" button
3. Enter: `Bearer your-jwt-token-here`
4. Test endpoints interactively

### Complete Test Suite

See `api-test-commands.md` for comprehensive cURL commands that can be imported into Postman, Insomnia, or other API
testing tools.

## 🔧 Project Structure

```
src/
├── main/
│   ├── java/me/manulorenzo/usermanagement/
│   │   ├── config/
│   │   │   ├── OpenApiConfig.java          # Swagger/OpenAPI configuration
│   │   │   └── SecurityConfig.java         # Security configuration
│   │   ├── controller/
│   │   │   ├── AdminController.java        # Admin endpoints
│   │   │   └── AuthController.java         # Authentication endpoints
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── RefreshTokenRequest.java    # Refresh token request DTO
│   │   │   └── RefreshTokenResponse.java   # Refresh token response DTO
│   │   ├── entity/
│   │   │   ├── Role.java                   # Role entity
│   │   │   ├── User.java                   # User entity
│   │   │   └── RefreshToken.java           # Refresh token entity
│   │   ├── repository/
│   │   │   ├── RoleRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── RefreshTokenRepository.java # Refresh token repository
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java          # JWT authentication filter
│   │   │   └── JwtUtil.java                # JWT utility methods
│   │   ├── service/
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── UserService.java
│   │   │   └── RefreshTokenService.java    # Refresh token service
│   │   ├── RoleSeeder.java                 # Database seeding
│   │   └── UserManagementApplication.java
│   └── resources/
│       ├── application.yml                 # Default configuration
│       └── application-docker.yml          # Docker configuration
├── test/java/                              # Comprehensive test suite
├── docker-scripts/                         # Docker helper scripts
├── init-db/                               # Database initialization scripts
├── docker-compose.yml                     # Docker Compose configuration
├── Dockerfile                             # Multi-stage Docker build
└── api-test-commands.md                   # API testing examples
```

## 🚀 Deployment

### Docker Production Deployment

```bash
# Production-like deployment
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Environment Variables

Key environment variables for production:

```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-secure-password
JWT_SECRET=your-very-secure-jwt-secret
```

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Add tests for new functionality
- Update documentation for API changes
- Test with both local and Docker setups

## 📚 Additional Resources

### Spring Boot Documentation

- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
- [Spring Web](https://docs.spring.io/spring-boot/3.5.3/reference/web/servlet.html)
- [Spring Security](https://docs.spring.io/spring-boot/3.5.3/reference/web/spring-security.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.3/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Helpful Guides

- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Manuel Lorenzo**

- GitHub: [@noloman](https://github.com/noloman)
- Email: manulorenzop@gmail.com

## 📞 Support

If you have any questions or need help:

1. **API Documentation**: Check the interactive Swagger UI when running
2. **Docker Issues**: Use `docker-compose logs -f` for debugging
3. **Database Issues**: Verify connection with `docker-compose ps postgres`
4. **Test Examples**: Review `api-test-commands.md` for usage examples
5. **GitHub Issues**: Open an issue for bugs or feature requests
6. **Application Logs**: Check detailed error information in console/file logs

## 🎯 Quick Start Checklist

- [ ] **Install Docker Desktop** (only requirement)
- [ ] Clone repository: `git clone <repository-url>`
- [ ] Navigate to project: `cd UserManagement`
- [ ] Make scripts executable: `chmod +x docker-scripts/*.sh`
- [ ] **Start everything**: `./docker-scripts/start.sh`
- [ ] **Open Swagger UI**: http://localhost:8082/swagger-ui.html
- [ ] **Register first user** (becomes admin automatically)
- [ ] **Login to get JWT tokens**
- [ ] **Test API endpoints**
- [ ] **Explore interactive documentation**

**That's it!** No local Java, Maven, or PostgreSQL installation required.

## Future Enhancements

- [ ] Password reset functionality
- [ ] Email verification
- [ ] OAuth2 integration (Google, GitHub)
- [ ] Rate limiting and request throttling
- [ ] Audit logging
- [ ] User permissions beyond roles
- [ ] File upload/profile pictures
- [ ] Redis for session management
- [ ] Kubernetes deployment manifests
- [ ] CI/CD pipeline integration
- [ ] Token blacklisting for immediate revocation
- [ ] Multi-factor authentication (MFA)

## Monitoring & Observability

### Health Checks

- **Application Health**: `/actuator/health`
- **Database Health**: Included in health endpoint
- **Docker Health**: Built-in container health checks

### Logging

- **Console Logging**: Structured JSON format
- **File Logging**: (Docker only) `/app/logs/usermanagement.log`
- **Log Levels**: Configurable per package
- **Request Tracing**: Detailed security and business logic logging

### Metrics

Basic Spring Boot Actuator metrics available at `/actuator/metrics`