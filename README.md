# User Management API

A comprehensive Spring Boot application for user registration, authentication, and role-based authorization using JWT
tokens with PostgreSQL database and Docker support.

## 🚀 Features

- **User Registration & Authentication** - Secure user account creation and login
- **JWT Token-based Security** - Stateless authentication using JSON Web Tokens
- **Role-based Access Control (RBAC)** - Admin and User roles with different permissions
- **Auto Role Assignment** - First user gets ADMIN role, subsequent users get USER role
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

### For Local Development:
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12+ (or use Docker for database)
- IDE (IntelliJ IDEA, VS Code, or Eclipse)

### For Docker Deployment:

- Docker Desktop or Docker Engine
- Docker Compose

## 🏃‍♂️ Getting Started

### Option 1: Full Docker Setup (Recommended)

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

```bash
# Start only PostgreSQL with Docker
./docker-scripts/db-only.sh

# Run the Spring Boot app locally
mvn spring-boot:run
```

The application will be available at:

- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html

### Option 3: Traditional Local Setup

```bash
# Install and start PostgreSQL locally
# Create database and user (see Database Setup section)

# Run the application
mvn spring-boot:run
```

## 🗄️ Database Setup

### Docker (Recommended)

The Docker setup automatically configures PostgreSQL with:

- **Database**: `usermanagement_dev`
- **Username**: `userapp`
- **Password**: `userapp123`
- **Port**: `5432`

### Manual PostgreSQL Setup

If installing PostgreSQL manually:

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE usermanagement_dev;
CREATE USER userapp WITH PASSWORD 'userapp123';
GRANT ALL PRIVILEGES ON DATABASE usermanagement_dev TO userapp;
```

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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
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

### Health & Monitoring

```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 🔐 Authentication & Authorization

### JWT Token Usage

After successful login, include the JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

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

### Hot Reload Development

For the best development experience:

```bash
# Start database only
./docker-scripts/db-only.sh

# Run app with Spring Boot DevTools
mvn spring-boot:run
```

This allows:

- Fast application restarts
- Live reload of static resources
- Direct database access for debugging

## 📖 API Testing

### Using cURL (Docker setup - port 8082)

#### 1. Register first user (becomes ADMIN)

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### 2. Login to get JWT token

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### 3. Use JWT token for protected endpoints

```bash
curl -X POST http://localhost:8082/api/admin/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
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
│   │   │   └── RegisterRequest.java
│   │   ├── entity/
│   │   │   ├── Role.java                   # Role entity
│   │   │   └── User.java                   # User entity
│   │   ├── repository/
│   │   │   ├── RoleRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java          # JWT authentication filter
│   │   │   └── JwtUtil.java                # JWT utility methods
│   │   ├── service/
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── UserService.java
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

## 🚧 Future Enhancements

- [ ] Password reset functionality
- [ ] User profile management
- [ ] Email verification
- [ ] OAuth2 integration (Google, GitHub)
- [ ] Rate limiting and request throttling
- [ ] Audit logging
- [ ] User permissions beyond roles
- [ ] File upload/profile pictures
- [ ] Redis for session management
- [ ] Kubernetes deployment manifests
- [ ] CI/CD pipeline integration

## 🔍 Monitoring & Observability

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

- [ ] Clone repository
- [ ] Install Docker Desktop
- [ ] Run `./docker-scripts/start.sh`
- [ ] Open http://localhost:8082/swagger-ui.html
- [ ] Register a user (becomes admin)
- [ ] Login to get JWT token
- [ ] Test admin endpoints
- [ ] Explore API documentation

---