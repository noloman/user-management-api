# User Management API

A comprehensive Spring Boot application for user registration, authentication, and role-based authorization using JWT
tokens with PostgreSQL database and Docker support.

## 🚀 Features

- **User Registration & Authentication** - Secure user account creation and login with **email verification**
- **Email Verification** - Users must verify their email address before account activation
- **Password Reset** - Secure password reset functionality via email tokens
- **JWT Token-based Security** - Stateless authentication using JSON Web Tokens
- **Refresh Token Support** - Automatic token renewal with secure refresh tokens (7-day expiry)
- **User Profile Management** - Get and update user profiles with username, email, full name, bio, and image URL
- **Role-based Access Control (RBAC)** - Admin and User roles with different permissions
- **Auto Role Assignment** - First user gets ADMIN role, subsequent users get USER role
- **Secure Logout** - Token invalidation for proper session management
- **Email Integration** - SMTP email sending for verification and password reset
- **Comprehensive Logging** - Detailed logging throughout the application
- **API Documentation** - Interactive Swagger/OpenAPI documentation
- **PostgreSQL Database** (version 16) - Production-ready database with Docker support
- **Docker-first Development** - Fully containerized application and database
- **Production Ready** - Health checks, metrics, and monitoring endpoints

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Security** (included with Spring Boot 3.5.3)
- **Spring Data JPA**
- **JWT (JSON Web Tokens)**
- **PostgreSQL** (version 16 via Docker)
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

**Note**: Manual setup requires PostgreSQL 16+ installation and configuration.

## ⚙️ Configuration

### Unified Configuration

The application uses a single `application.yml` file with environment variables for flexibility:

**Quick Setup**: Copy `.env.example` to `.env` and customize:

```bash
cp .env.example .env
# Edit .env file with your configuration
```

**Key Configuration Options**:
```yaml
# Database (Docker by default, local development supported)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/usermanagement_dev
SPRING_DATASOURCE_USERNAME=userapp
SPRING_DATASOURCE_PASSWORD=userapp123

# Server configuration
SERVER_PORT=8082  # Docker and local both use 8082

# Development settings
SHOW_SQL=false          # Set to true for SQL debugging
FORMAT_SQL=false        # Set to true for pretty SQL output
LOG_LEVEL_APP=DEBUG     # Application logging level

# Email configuration
MAIL_HOST=localhost
MAIL_PORT=1025
APP_EMAIL_FROM=noreply@usermanagement.com
APP_BASE_URL=http://localhost:8082
```

### Environment Modes

#### Docker Development (Recommended)

```bash
./docker-scripts/start.sh
# Uses: postgres:5432, port 8082, containerized email
```

#### Local Development with Docker Database

```bash
./docker-scripts/db-only.sh  # Start PostgreSQL
mvn spring-boot:run           # Run Spring Boot locally
# Uses: localhost:5432, port 8082, local email
```

#### Production

Set environment variables for your deployment:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://your-db:5432/prod_db"
export JWT_SECRET="your-production-secret-key"
export MAIL_HOST="smtp.gmail.com"
export MAIL_PORT="587"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
```

### Key Environment Variables

| Variable                | Default                 | Description           |
|-------------------------|-------------------------|-----------------------|
| `SERVER_PORT`           | `8082`                  | Application port      |
| `SPRING_DATASOURCE_URL` | `localhost:5432`        | Database connection   |
| `SHOW_SQL`              | `false`                 | Enable SQL logging    |
| `LOG_LEVEL_APP`         | `DEBUG`                 | Application log level |
| `MAIL_HOST`             | `localhost`             | SMTP server           |
| `JWT_SECRET`            | `default-secret...`     | JWT signing key       |
| `APP_BASE_URL`          | `http://localhost:8082` | Base URL for emails   |

## 📧 Email Configuration

The application requires SMTP configuration for email verification and password reset functionality.

### Development Configuration

For development, the application uses a local SMTP server (like MailHog) by default:

```yaml
spring:
  mail:
    host: localhost
    port: 1025
    username: ""
    password: ""
```

### Production Configuration

For production, configure with your SMTP provider (Gmail, SendGrid, AWS SES, etc.):

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Environment Variables

You can configure email settings using environment variables:

- `MAIL_HOST` - SMTP server hostname
- `MAIL_PORT` - SMTP server port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `APP_EMAIL_FROM` - From email address for outgoing emails
- `APP_BASE_URL` - Base URL for email links (verification/reset links)

### Testing Without Email

During development, verification and reset tokens are logged to the console when email sending fails, allowing you to
test the functionality without an actual SMTP server.

## 🔐 Authentication Workflow

The application now uses a secure email verification workflow:

### Registration & Verification Flow

1. **User Registers** → Account created but disabled
2. **Verification Email Sent** → Contains unique token (24-hour expiry)
3. **User Verifies Email** → Account enabled, welcome email sent
4. **User Can Login** → Receive access + refresh tokens

### Password Reset Flow

1. **User Requests Reset** → Via email address
2. **Reset Email Sent** → Contains unique token (1-hour expiry)
3. **User Resets Password** → Using token + new password
4. **Token Invalidated** → Reset token deleted from database

### Development Testing (No SMTP)

When SMTP is not configured, tokens are logged to console:

```
2024-01-20 10:30:15 WARN  EmailService - Development - Verification token for john@example.com: abc123def456
2024-01-20 10:31:22 WARN  EmailService - Development - Password reset token for john@example.com: xyz789abc123
```

**To test email verification:**

1. Register a user and note the logged token
2. Use the token in `/api/auth/verify-email` endpoint
3. User can now login successfully

**To test password reset:**

1. Request password reset and note the logged token
2. Use the token in `/api/auth/reset-password` endpoint
3. User can login with new password

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

#### Register User (Email Verification Required)

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "mySecurePassword123"
}
```

**Response:**

```json
"User registered successfully. Please check your email to verify your account."
```

**Note**: Account is created but disabled until email verification is completed.

#### Verify Email Address

```http
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "token": "verification-token-from-email"
}
```

**Response:**

```json
"Email verification successful"
```

**Note**: This activates the account and sends a welcome email.

#### Resend Verification Email

```http
POST /api/auth/resend-verification?email=john@example.com
```

**Response:**

```json
"Verification email sent"
```

#### Login User (Requires Verified Email)

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

**Note**: Login will fail with "Account is disabled" if email is not verified.

#### Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh-token-value"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

#### Forgot Password

```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Response:**

```json
"Password reset email sent"
```

**Note**: Sends email with reset token (expires in 1 hour).

#### Reset Password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "john@example.com",
  "token": "reset-token-from-email",
  "newPassword": "myNewSecurePassword123"
}
```

**Response:**

```json
"Password reset successful"
```

### Admin Endpoints (Requires ADMIN role)

#### Add Role to User

```bash
curl -X POST "http://localhost:8082/api/admin/addRole?username=jane&roleName=ADMIN" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
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

## User Roles & Permissions

This application uses Role-Based Access Control (RBAC) using the following roles:

### Available Roles

- **ADMIN**: Can access all API endpoints, including admin and role management endpoints
- **USER**: Access to standard user endpoints (profile etc). Cannot perform admin operations

### Role Assignment Logic

- The **first registered user** is assigned the ADMIN role automatically
- All **subsequent users** are assigned the USER role
- **Admin users** can assign additional roles to any account using the `/api/admin/addRole` endpoint
- Users can have multiple roles

### How to Assign a Role (as Admin)

```bash
curl -X POST "http://localhost:8082/api/admin/addRole?username=jane&roleName=ADMIN" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Example: Adding Additional Roles to a User

After execution, user `jane` will have both USER and ADMIN roles and will be able to use all admin endpoints.

### Checking a User's Roles

Roles are returned in the JWT token's payload (under `roles`) and are also visible as authorities for the authenticated
user.

#### Example JWT Payload

```json
{
  "sub": "jane",
  "roles": [
    { "authority": "ROLE_ADMIN" }, 
    { "authority": "ROLE_USER" }
  ],
  ...
}
```

- Endpoints that require the ADMIN role are documented accordingly in the API section.

## Possible Features to Add

This section outlines potential enhancements organized by category and implementation priority to help guide future
development.

### Enhanced Security Features

#### Multi-Factor Authentication (MFA)

- [ ] **TOTP Support** - Google Authenticator, Authy integration
- [ ] **SMS OTP** - Phone number verification via Twilio/AWS SNS
- [ ] **Email OTP** - Alternative to authenticator apps
- [ ] **Backup Recovery Codes** - One-time codes when MFA device is lost
- [ ] **WebAuthn/FIDO2** - Hardware security keys, biometric authentication

#### Advanced Account Security

- [ ] **Account Lockout** - Lock accounts after N failed login attempts
- [ ] **Login Attempt Monitoring** - Track suspicious activity patterns
- [ ] **Device Fingerprinting** - Remember and validate trusted devices
- [ ] **Session Management** - View and revoke active sessions across devices
- [ ] **Advanced Password Policies** - Complexity rules, expiration, breach detection
- [ ] **Password History** - Prevent reusing recent passwords
- [ ] **Suspicious Activity Alerts** - Email notifications for unusual login patterns

### Advanced User Management

#### Enhanced Profile Management

- [ ] **Profile Picture Upload** - File upload with image processing/cropping
- [ ] **Custom Profile Fields** - Configurable additional user attributes
- [ ] **User Preferences** - Theme, language, notification settings
- [ ] **Privacy Controls** - Granular visibility settings for profile data
- [ ] **Account Data Export** - GDPR-compliant user data portability
- [ ] **Account Deletion** - Complete data removal with confirmation workflow

#### Social & Collaboration Features

- [ ] **OAuth2 Providers** - Google, GitHub, Microsoft, Facebook, LinkedIn login
- [ ] **User Groups/Teams** - Organize users into hierarchical groups
- [ ] **User Connections** - Follow/friend relationships
- [ ] **Activity Feeds** - Timeline of user actions and updates
- [ ] **User Directory** - Searchable user listings with filters

### Advanced Authorization & Permissions

#### Fine-Grained Access Control

- [ ] **Resource-Based Permissions** - Per-object access control

```java
@PreAuthorize("hasPermission(#userId, 'User', 'READ')")
@PreAuthorize("hasPermission(#reportId, 'Report', 'WRITE')")
```

- [ ] **Dynamic Roles** - Runtime role creation and assignment
- [ ] **Permission Templates** - Predefined permission sets
- [ ] **Delegation System** - Temporary permission sharing
- [ ] **Time-Based Access** - Permissions with expiration dates
- [ ] **Conditional Access** - Context-aware permissions (location, time, device)

#### Multi-Tenancy & Organizations

- [ ] **Tenant Isolation** - Complete data separation per organization
- [ ] **Organization Hierarchy** - Departments, teams, sub-organizations
- [ ] **Cross-Tenant Access** - Controlled resource sharing
- [ ] **Tenant-Specific Branding** - Custom themes per organization
- [ ] **Tenant Admin Dashboard** - Organization management interface

### Analytics & Monitoring

#### User Analytics

- [ ] **Login Statistics** - Track user engagement patterns
- [ ] **Feature Usage Analytics** - Monitor API endpoint usage
- [ ] **User Journey Tracking** - Understand user behavior flows
- [ ] **Retention Metrics** - User activity over time
- [ ] **Geographic Analytics** - Login locations and patterns
- [ ] **Device Analytics** - Track device types and browsers

#### Security Monitoring

- [ ] **Comprehensive Audit Logs** - All user actions with full context
- [ ] **Security Event Dashboard** - Real-time security monitoring
- [ ] **Compliance Reporting** - SOX, GDPR, HIPAA compliance reports
- [ ] **Anomaly Detection** - AI-powered suspicious activity detection
- [ ] **Real-Time Alerts** - Webhook/email notifications for security events
- [ ] **Forensic Logging** - Detailed investigation capabilities

### Advanced API Features

#### Performance & Scalability

- [ ] **Rate Limiting** - Configurable limits per user type/endpoint

```yaml
rate-limiting:
  free-users: 100/hour
  premium-users: 1000/hour
  admin-users: unlimited
```

- [ ] **API Caching** - Redis-based response caching
- [ ] **Request Batching** - Bulk operations support
- [ ] **Async Processing** - Long-running operations with status tracking
- [ ] **GraphQL Support** - Flexible query interface
- [ ] **API Versioning** - Semantic versioning with deprecation handling

#### Integration Capabilities

- [ ] **Webhook System** - Configurable event notifications
- [ ] **External API Integrations** - Slack, Teams, email providers
- [ ] **LDAP/Active Directory Sync** - Enterprise directory integration
- [ ] **SAML SSO** - Enterprise single sign-on support
- [ ] **API Gateway Integration** - Kong, Zuul, AWS API Gateway support

### Modern User Experience

#### Real-Time Features

- [ ] **WebSocket Support** - Real-time notifications and updates
- [ ] **Server-Sent Events** - Push updates to web clients
- [ ] **Live User Status** - Online/offline/away indicators
- [ ] **Real-Time Chat** - Basic messaging system
- [ ] **Collaborative Features** - Real-time document editing, comments

#### Mobile & Progressive Web App

- [ ] **Push Notifications** - Mobile and web push notifications
- [ ] **Offline Support** - Service worker for offline functionality
- [ ] **Biometric Authentication** - Fingerprint, Face ID, Touch ID
- [ ] **Mobile-Optimized Endpoints** - Bandwidth-efficient responses
- [ ] **App Store Distribution** - Native mobile app versions

### Business & Enterprise Features

#### Subscription & Billing

- [ ] **User Tiers** - Free, Premium, Enterprise plans
- [ ] **Usage Tracking** - API calls, storage, feature usage limits
- [ ] **Billing Integration** - Stripe, PayPal, enterprise billing
- [ ] **Trial Management** - Free trial periods with automatic conversion
- [ ] **Usage Analytics** - Cost tracking and optimization insights

#### Advanced Workflow Management

- [ ] **User Approval Workflows** - Admin approval for registration/changes
- [ ] **Role Request System** - Self-service role upgrade requests
- [ ] **Automated User Provisioning** - Rule-based account setup
- [ ] **User Lifecycle Management** - Onboarding/offboarding automation
- [ ] **Bulk User Operations** - CSV import/export, batch updates
- [ ] **User Deactivation** - Soft delete with data retention policies

### Developer & DevOps Features

#### Development Tools

- [ ] **API SDK Generation** - Auto-generated client libraries
- [ ] **Interactive API Explorer** - Enhanced Swagger UI with examples
- [ ] **API Testing Tools** - Built-in testing and validation
- [ ] **Mock Data Generation** - Realistic test data creation
- [ ] **API Documentation Automation** - Auto-updating docs from code

#### Deployment & Operations

- [ ] **Kubernetes Manifests** - Production-ready K8s deployment
- [ ] **Helm Charts** - Configurable Kubernetes deployments
- [ ] **CI/CD Pipeline** - GitHub Actions, Jenkins integration
- [ ] **Health Check Improvements** - Custom health indicators
- [ ] **Metrics & Observability** - Prometheus, Grafana integration
- [ ] **Log Aggregation** - ELK stack, Splunk integration

### Implementation Priority Roadmap

#### Phase 1: Security Foundations (High Impact, Medium Effort)

1. **Account Lockout** - Prevent brute force attacks
2. **Comprehensive Audit Logging** - Track all user actions
3. **Rate Limiting** - Protect against API abuse
4. **Profile Picture Upload** - Basic file handling
5. **OAuth2 Integration** - Google/GitHub login

#### Phase 2: User Experience (Medium Impact, High Value)

1. **Multi-Factor Authentication** - TOTP support
2. **Advanced Permissions** - Resource-based access control
3. **Real-Time Notifications** - WebSocket implementation
4. **Bulk Operations** - CSV import/export functionality
5. **User Analytics Dashboard** - Basic usage statistics

#### Phase 3: Enterprise Features (High Value, High Effort)

1. **Multi-Tenancy** - Complete tenant isolation
2. **LDAP Integration** - Enterprise directory sync
3. **Advanced Workflows** - Approval and provisioning systems
4. **Compliance Features** - GDPR, SOX reporting
5. **Mobile App Support** - Native applications

#### Phase 4: Advanced Platform (Innovation, High Effort)

1. **AI-Powered Features** - Anomaly detection, smart insights
2. **Microservices Architecture** - Service decomposition
3. **Advanced Analytics** - Machine learning insights
4. **API Marketplace** - Third-party integrations
5. **White-Label Solutions** - Customizable branding

### Implementation Examples

#### Account Lockout Feature

```java
@Entity
public class User {
    // ... existing fields
    private int failedLoginAttempts = 0;
    private LocalDateTime lastFailedLogin;
    private LocalDateTime lockedUntil;
    private boolean accountLocked = false;
}

@Service
public class AccountLockoutService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 30;
    
    public void recordFailedLogin(String username) {
        User user = userRepository.findByUsername(username);
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
        }
        userRepository.save(user);
    }
}
```

#### Rate Limiting Implementation

```java
@Component
public class RateLimitingFilter implements Filter {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        String userKey = extractUserKey(request);
        String rateLimitKey = "rate_limit:" + userKey;
        
        Long requests = redisTemplate.opsForValue().increment(rateLimitKey);
        if (requests == 1) {
            redisTemplate.expire(rateLimitKey, Duration.ofHours(1));
        }
        
        if (requests > getRateLimit(userKey)) {
            ((HttpServletResponse) response).setStatus(429);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

### UI/UX Enhancements

#### Admin Dashboard

- [ ] **User Management Interface** - Web-based admin panel
- [ ] **Analytics Dashboard** - Charts and metrics visualization
- [ ] **System Configuration** - Runtime configuration management
- [ ] **Log Viewer** - Web-based log browsing and search
- [ ] **API Usage Monitor** - Real-time API usage tracking

#### User Portal

- [ ] **Self-Service Portal** - Profile management, password reset
- [ ] **Activity History** - Personal activity timeline
- [ ] **Privacy Dashboard** - Data usage and privacy controls
- [ ] **Notification Center** - Centralized notification management
- [ ] **Account Settings** - Comprehensive account management

This roadmap provides a structured approach to evolving the User Management API into a comprehensive, enterprise-grade
platform while maintaining backward compatibility and focusing on high-impact features first.

## Testing

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
  -d '{"username": "admin", "password": "admin123", "email": "admin@example.com"}'
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

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
}
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
│   │   ├── config/                # Application and security configuration
│   │   ├── controller/            # Auth, Admin, Profile controllers  
│   │   ├── dto/                   # Request/response data objects
│   │   ├── entity/                # JPA entity classes (User, Role, RefreshToken)
│   │   ├── repository/            # Spring Data repositories
│   │   ├── security/              # JWT and security utilities
│   │   ├── service/               # Business logic and email services
│   │   ├── RoleSeeder.java        # Initial role seeding
│   │   └── UserManagementApplication.java
│   └── resources/
│       └── application.yml        # Unified configuration
├── test/
│   └── java/                      # Comprehensive test suite
├── docker-scripts/                # Docker helper scripts
├── docker-compose.yml             # Docker Compose configuration
├── Dockerfile                     # Application Docker image
└── api-test-commands.md           # API testing examples
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

## 🔐 Authentication & Authorization

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

## User Roles & Permissions

This application uses Role-Based Access Control (RBAC) using the following roles:

### Available Roles

- **ADMIN**: Can access all API endpoints, including admin and role management endpoints
- **USER**: Access to standard user endpoints (profile etc). Cannot perform admin operations

### Role Assignment Logic

- The **first registered user** is assigned the ADMIN role automatically
- All **subsequent users** are assigned the USER role
- **Admin users** can assign additional roles to any account using the `/api/admin/addRole` endpoint
- Users can have multiple roles

### How to Assign a Role (as Admin)

```bash
curl -X POST "http://localhost:8082/api/admin/addRole?username=jane&roleName=ADMIN" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Example: Adding Additional Roles to a User

After execution, user `jane` will have both USER and ADMIN roles and will be able to use all admin endpoints.

### Checking a User's Roles

Roles are returned in the JWT token's payload (under `roles`) and are also visible as authorities for the authenticated
user.

#### Example JWT Payload

```json
{
  "sub": "jane",
  "roles": [
    { "authority": "ROLE_ADMIN" }, 
    { "authority": "ROLE_USER" }
  ],
  ...
}
```

- Endpoints that require the ADMIN role are documented accordingly in the API section.

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
- [ ] **Register first user** (becomes admin automatically, but account disabled)
- [ ] **Check console logs** for verification token (look for WARN EmailService messages)
- [ ] **Verify email** using `/api/auth/verify-email` endpoint with token from logs
- [ ] **Login to get JWT tokens** (now works since account is verified)
- [ ] **Use "Authorize" button** in Swagger UI with access token
- [ ] **Test API endpoints** and explore interactive documentation

**That's it!** No local Java, Maven, or PostgreSQL installation required.

### Email Verification Example

After registration, you'll see in console logs:

```
WARN  EmailService - Development - Verification token for admin@example.com: abc123def456
```

Then verify with:

```bash
curl -X POST http://localhost:8082/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "token": "abc123def456"}'
```

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