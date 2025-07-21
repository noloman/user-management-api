# User Management API

A comprehensive Spring Boot application for user registration, authentication, and role-based authorization using JWT
tokens.

## 🚀 Features

- **User Registration & Authentication** - Secure user account creation and login
- **JWT Token-based Security** - Stateless authentication using JSON Web Tokens
- **Role-based Access Control (RBAC)** - Admin and User roles with different permissions
- **Auto Role Assignment** - First user gets ADMIN role, subsequent users get USER role
- **Comprehensive Logging** - Detailed logging throughout the application
- **API Documentation** - Interactive Swagger/OpenAPI documentation
- **H2 Database** - In-memory database for development and testing
- **Extensive Testing** - Unit and integration tests with high coverage

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Security 6.5.1**
- **Spring Data JPA**
- **JWT (JSON Web Tokens)**
- **H2 Database**
- **Lombok**
- **OpenAPI/Swagger**
- **Maven**
- **JUnit 5 & Mockito**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- IDE (IntelliJ IDEA, VS Code, or Eclipse)

## 🏃‍♂️ Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd UserManagement
```

### 2. Build the project

```bash
mvn clean compile
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### 4. Access the API Documentation

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

### 5. Access H2 Database Console (Development)

- **URL**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:mem:usermanagement`
- **Username**: `sa`
- **Password**: *(leave empty)*

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
- **Integration Tests**: Repository layer, database operations
- **Controller Tests**: API endpoints (without security context)

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

## 🛠️ Development

### Database Initialization

The application automatically seeds initial roles:

- `ADMIN` role
- `USER` role

This is handled by the `RoleSeeder` component on application startup.

### Configuration

Key configuration in `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

logging:
  level:
    me.manulorenzo.usermanagement: DEBUG

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### Security Configuration

- JWT tokens expire after 1 hour
- Passwords are encrypted using BCrypt
- CSRF protection is disabled (stateless API)
- Session management is stateless

## 📖 API Testing

### Using cURL

#### 1. Register first user (becomes ADMIN)

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### 2. Login to get JWT token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

#### 3. Use JWT token for protected endpoints

```bash
curl -X POST http://localhost:8081/api/admin/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Using Swagger UI

1. Navigate to http://localhost:8081/swagger-ui.html
2. Click "Authorize" button
3. Enter: `Bearer your-jwt-token-here`
4. Test endpoints interactively

## 🔧 Project Structure

```
src/
├── main/java/me/manulorenzo/usermanagement/
│   ├── config/
│   │   ├── OpenApiConfig.java          # Swagger configuration
│   │   └── SecurityConfig.java         # Security configuration
│   ├── controller/
│   │   ├── AdminController.java        # Admin endpoints
│   │   └── AuthController.java         # Authentication endpoints
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── RegisterRequest.java
│   ├── entity/
│   │   ├── Role.java                   # Role entity
│   │   └── User.java                   # User entity
│   ├── repository/
│   │   ├── RoleRepository.java
│   │   └── UserRepository.java
│   ├── security/
│   │   ├── JwtAuthFilter.java          # JWT authentication filter
│   │   └── JwtUtil.java                # JWT utility methods
│   ├── service/
│   │   ├── CustomUserDetailsService.java
│   │   └── UserService.java
│   ├── RoleSeeder.java                 # Database seeding
│   └── UserManagementApplication.java
└── test/java/                          # Comprehensive test suite
```

## 🚧 Future Enhancements

- [ ] Password reset functionality
- [ ] User profile management
- [ ] Email verification
- [ ] OAuth2 integration
- [ ] Rate limiting
- [ ] Audit logging
- [ ] User permissions beyond roles
- [ ] File upload/profile pictures

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Manuel Lorenzo**

- GitHub: [@noloman](https://github.com/noloman

## 📞 Support

If you have any questions or need help:

1. Check the [API Documentation](http://localhost:8081/swagger-ui.html) when running locally
2. Review the test files for usage examples
3. Open an issue on GitHub
4. Check the application logs for detailed error information

---

⭐ **Don't forget to give the project a star if you found it helpful!**