# User Management API - Test Commands

This file contains cURL commands to test all endpoints in the User Management application.

**Note:** We follow a Docker-first approach for development and testing. Ensure Docker is installed and running before
proceeding.

**Prerequisites**: Start the application with Docker using `./docker-scripts/start.sh` before running these commands.

## Email Verification & Password Reset

**Important**: This application now requires email verification for new accounts:

1. **Registration**: Creates account but leaves it disabled
2. **Email Verification**: User must verify email to activate account
3. **Login**: Only works after email verification
4. **Password Reset**: Available for users who forgot their password

### Development Testing

In development mode (without SMTP server), verification and reset tokens are **logged to the console** when emails fail
to send. Look for log messages like:

```
WARN  EmailService - Development - Verification token for user@example.com: abc123def456
WARN  EmailService - Development - Password reset token for user@example.com: xyz789abc123
```

You can use these tokens in the verification/reset endpoints below.

## User Roles & Permissions

This application uses Role-Based Access Control (RBAC) with two roles:

- **ADMIN** - can register, login, profile, and admin endpoints (role management, test, etc)
- **USER** - can register, login, access their own profile

### Assignment

- **First user registered**: assigned ADMIN role
- **All subsequent users**: assigned USER role
- **Admins** can add roles: `/api/admin/addRole` endpoint
- Users may have both ADMIN and USER roles

### Assigning a Role with curl

```bash
curl -X POST "http://localhost:8082/api/admin/addRole?username=user1&roleName=ADMIN" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Example JWT roles in token

JWT tokens (see [jwt.io](https://jwt.io/)) contain:

```json
{
  "sub": "user1",
  "roles": [
    { "authority": "ROLE_USER" },
    { "authority": "ROLE_ADMIN" }
  ],
  ...  
}
```

- You must use an ADMIN token for admin-protected endpoints
- Use refresh token after login for continuous authentication

## Base Configuration

```bash
# Docker setup (recommended) - port 8082
BASE_URL="http://localhost:8082"

# Local development with Docker database - port 8081
# BASE_URL="http://localhost:8081"
```

## OpenAPI v3 Specification

For direct import into API clients, use the OpenAPI v3 specification file at `User Management/openapi.yaml`.

### Authentication Endpoints

#### Register Admin User
```bash
curl -X POST "http://localhost:8082/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123", "email": "admin@example.com"}'
```

**Expected Response:**

```json
"User registered successfully. Please check your email to verify your account."
```

#### Register Regular User

```bash
curl -X POST "http://localhost:8082/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "user123", "email": "user1@example.com"}'
```

**Expected Response:**

```json
"User registered successfully. Please check your email to verify your account."
```

#### Verify Email

```bash
curl -X POST "http://localhost:8082/api/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "token": "YOUR_VERIFICATION_TOKEN"}'
```

**Expected Response:**

```json
"Email verification successful"
```

#### Resend Verification Email

```bash
curl -X POST "http://localhost:8082/api/auth/resend-verification?email=admin@example.com"
```

**Expected Response:**

```json
"Verification email sent"
```

#### Login Admin
```bash
curl -X POST "http://localhost:8082/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Expected Response:**

```json
{
  "token": "...",
  "refreshToken": "..."
}
```

#### Login User

```bash
curl -X POST "http://localhost:8082/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "user123"}'
```

**Expected Response:**

```json
{
  "token": "...",
  "refreshToken": "..."
}
```

#### Refresh Access Token
```bash
curl -X POST "http://localhost:8082/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}'
```

**Expected Response:**

```json
{
   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout

```bash
curl -X POST "http://localhost:8082/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}'
```

#### Request Password Reset

```bash
curl -X POST "http://localhost:8082/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com"}'
```

**Expected Response:**

```json
"Password reset email sent"
```

#### Reset Password

```bash
curl -X POST "http://localhost:8082/api/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "token": "YOUR_RESET_TOKEN", "newPassword": "newpassword123"}'
```

**Expected Response:**

```json
"Password reset successful"
```

### Profile Endpoints

#### Get Current User Profile

```bash
curl -X GET "http://localhost:8082/api/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### Update User Profile

```bash
curl -X PUT "http://localhost:8082/api/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{"email": "updated@example.com", "fullName": "Updated Name", "bio": "Updated bio"}'
```

### Admin Endpoints

#### Test Admin Access
```bash
curl -X POST "http://localhost:8082/api/admin/test" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

#### Add Role to User

```bash
curl -X POST "http://localhost:8082/api/admin/addRole?username=user1&roleName=USER" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Health & Monitoring

#### Health Check

```bash
curl -X GET "http://localhost:8082/actuator/health"
```

#### API Info

```bash
curl -X GET "http://localhost:8082/actuator/info"
```

### Error Test Cases

#### Register with Existing Username

```bash
curl -X POST "http://localhost:8082/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "different123", "email": "admin@example.com"}'
```

#### Login with Wrong Password

```bash
curl -X POST "http://localhost:8082/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "wrongpassword"}'
```

#### Access Admin Endpoint without Token

```bash
curl -X POST "http://localhost:8082/api/admin/test"
```

## 1. Authentication Endpoints

### Register First User (Will be ADMIN)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com"
  }'
```

### Register Second User (Will be USER)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123",
    "email": "user1@example.com"
  }'
```

### Register Third User (Will be USER)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "password": "user456",
    "email": "user2@example.com"
  }'
```

### Login as Admin (Save both tokens for operations)

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response includes both access token (15min) and refresh token (7 days):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Login as Regular User

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123"
  }'
```

**Response includes both access token (15min) and refresh token (7 days):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Refresh Access Token (When access token expires)

```bash
curl -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

**Response returns new access token:**

```json
{
   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Logout (Invalidates refresh token)

```bash
curl -X POST "${BASE_URL}/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

## 1b. Profile Endpoints

### Get Current User Profile

```bash
curl -X GET "${BASE_URL}/api/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Example Response:**

```json
{
  "username": "user1",
  "email": "user1@example.com",
  "fullName": "User One",
  "bio": "I like turtles.",
  "imageUrl": "https://cdn.example.com/user1.png"
}
```

### Update Current User Profile

```bash
curl -X PUT "${BASE_URL}/api/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "email": "newmail@example.com",
    "fullName": "New Name",
    "bio": "Bio updated!",
    "imageUrl": "https://cdn.example.com/newpic.png"
  }'
```

**Response:**

```json
{
  "username": "user1",
  "email": "newmail@example.com",
  "fullName": "New Name",
  "bio": "Bio updated!",
  "imageUrl": "https://cdn.example.com/newpic.png"
}
```

## 2. Admin Endpoints (Require ADMIN role)

**Note: Replace `YOUR_ADMIN_JWT_TOKEN` with the actual token received from admin login**

### Test Admin Access

```bash
curl -X POST "${BASE_URL}/api/admin/test" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Test Admin Access (No Auth Required - for debugging)

```bash
curl -X POST "${BASE_URL}/api/admin/test-no-auth"
```

### Add Role to User (Success case)

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=user1&roleName=USER" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Add Additional Role to User

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=user1&roleName=ADMIN" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Try to Add Non-existent Role (Error case)

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=user1&roleName=NONEXISTENT" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Try to Add Role to Non-existent User (Error case)

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=nonexistent&roleName=USER" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## 3. Error Cases and Edge Cases

### Register with Existing Username (Should fail)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "different123",
    "email": "admin@example.com"
  }'
```

### Login with Wrong Password (Should fail)

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

### Login with Non-existent User (Should fail)

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nonexistent",
    "password": "password"
  }'
```

### Refresh with Invalid Token (Should fail)

```bash
curl -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "invalid-refresh-token"
  }'
```

### Refresh with Expired Token (Should fail)

```bash
curl -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "expired-refresh-token-uuid"
  }'
```

### Logout with Invalid Token (Should succeed silently)

```bash
curl -X POST "${BASE_URL}/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "invalid-refresh-token"
  }'
```

### Try Admin Endpoint without Token (Should fail with 401)

```bash
curl -X POST "${BASE_URL}/api/admin/test"
```

### Try Admin Endpoint with Invalid Token (Should fail with 401)

```bash
curl -X POST "${BASE_URL}/api/admin/test" \
  -H "Authorization: Bearer invalid.token.here"
```

### Try Admin Endpoint with User Token (Should fail with 403)

**Note: Replace `YOUR_USER_JWT_TOKEN` with token from user1 login**

```bash
curl -X POST "${BASE_URL}/api/admin/test" \
  -H "Authorization: Bearer YOUR_USER_JWT_TOKEN"
```

## 4. Malformed Request Cases

### Register with Missing Username

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "password123",
    "email": "user@example.com"
  }'
```

### Register with Missing Password

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com"
  }'
```

### Register with Empty Body

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Login with Missing Credentials

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin"
  }'
```

### Refresh with Missing Token

```bash
curl -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Logout with Missing Token

```bash
curl -X POST "${BASE_URL}/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Add Role with Missing Parameters

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=user1" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## 5. Complete Test Workflow

Here's a complete workflow to test the entire application including email verification:

```bash
#!/bin/bash

BASE_URL="http://localhost:8082"

echo "=== 1. Registering Admin User ==="
ADMIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com"
  }')
echo "Response: $ADMIN_RESPONSE"
echo "Note: Check console logs for verification token"

echo -e "\n=== 2. Verify Admin Email (use token from logs) ==="
echo "Copy verification token from console logs and run:"
echo "curl -X POST \"${BASE_URL}/api/auth/verify-email\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"email\": \"admin@example.com\", \"token\": \"COPY_TOKEN_FROM_LOGS\"}'"
echo ""
echo "For this example, assuming email is verified..."

echo -e "\n=== 3. Registering Regular User ==="
USER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123",
    "email": "user1@example.com"
  }')
echo "Response: $USER_RESPONSE"
echo "Note: Check console logs for verification token"

echo -e "\n=== 4. Admin Login (assuming email verified) ==="
ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')
echo "Response: $ADMIN_LOGIN"

# Extract token (requires jq tool)
if command -v jq &> /dev/null; then
    ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.token')
    echo "Admin Token: $ADMIN_TOKEN"
    
    echo -e "\n=== 5. Testing Admin Access ==="
    ADMIN_TEST=$(curl -s -X POST "${BASE_URL}/api/admin/test" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    echo "Response: $ADMIN_TEST"
    
    echo -e "\n=== 6. Adding Role to User ==="
    ADD_ROLE=$(curl -s -X POST "${BASE_URL}/api/admin/addRole?username=user1&roleName=USER" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    echo "Response: $ADD_ROLE"
else
    echo "Note: Install 'jq' to automatically extract JWT tokens"
    echo "Manually copy the token from the login response above"
fi

echo -e "\n=== 7. User Login (requires email verification first) ==="
echo "Note: User must verify email before login will work"
USER_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123"
  }')
echo "Response: $USER_LOGIN"

echo -e "\n=== 8. Test Password Reset Flow ==="
FORGOT_PASSWORD=$(curl -s -X POST "${BASE_URL}/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com"
  }')
echo "Forgot Password Response: $FORGOT_PASSWORD"
echo "Note: Check console logs for reset token"
echo ""
echo "To complete password reset, run:"
echo "curl -X POST \"${BASE_URL}/api/auth/reset-password\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"email\": \"admin@example.com\", \"token\": \"COPY_RESET_TOKEN_FROM_LOGS\", \"newPassword\": \"newpass123\"}'"

echo -e "\n=== 9. Refresh Token Test ==="
if command -v jq &> /dev/null && [[ $USER_LOGIN == *"token"* ]]; then
    REFRESH_TOKEN=$(echo $USER_LOGIN | jq -r '.refreshToken')
    echo "Refresh Token: $REFRESH_TOKEN"

    echo -e "\n=== 10. Refresh Access Token ==="
    REFRESH_ACCESS_TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/refresh" \
      -H "Content-Type: application/json" \
      -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")
    echo "Response: $REFRESH_ACCESS_TOKEN"

    echo -e "\n=== 11. Logout ==="
    LOGOUT=$(curl -s -X POST "${BASE_URL}/api/auth/logout" \
      -H "Content-Type: application/json" \
      -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")
    echo "Response: $LOGOUT"
fi

echo -e "\n=== Test Complete ==="
echo ""
echo " Important Notes:"
echo "1. Email verification is required for all new accounts"
echo "2. Check console logs for verification/reset tokens in development"
echo "3. In production, users receive emails with verification links"
echo "4. Login will fail with 'Account is disabled' until email verified"
echo "5. Password reset tokens expire after 1 hour"
echo "6. Verification tokens expire after 24 hours"
```

## 6. API Client Import Instructions

### Using the OpenAPI v3 Specification (Recommended)

This repository includes a comprehensive OpenAPI v3 specification at `User Management/openapi.yaml` that can be imported
into any modern API client:

#### Bruno Import

1. **Open Bruno** and click "Import Collection"
2. **Select "OpenAPI v3"** as the import format
3. **Choose the file**: `User Management/openapi.yaml`
4. **Configure environment**: Bruno will automatically create environments for both Docker (8082) and Local (8081)

#### Postman Import

1. **Open Postman** and click "Import"
2. **Select "File"** and choose `User Management/openapi.yaml`
3. **Review and Import**: Postman will create a collection with all endpoints
4. **Set up environment variables**:
   - `baseUrl`: `http://localhost:8082` (or `http://localhost:8081` for local)
   - Add `accessToken` and `refreshToken` variables for authentication

#### Insomnia Import

1. **Open Insomnia** and click "Import/Export"
2. **Select "Import Data"** → "From File"
3. **Choose**: `User Management/openapi.yaml`
4. **Set up environment**: Create environment variables for `baseUrl`, `accessToken`, `refreshToken`

### What's Included in the OpenAPI Spec

✅ **Complete API Coverage**: All endpoints with detailed descriptions
✅ **Authentication Schemas**: Bearer token configuration
✅ **Request/Response Examples**: Real sample data for all endpoints  
✅ **Multiple Environments**: Docker (8082) and Local (8081) servers
✅ **Comprehensive Schemas**: All DTOs with validation rules
✅ **Error Responses**: Detailed error handling documentation
✅ **Security Definitions**: JWT Bearer token setup

### Manual Bruno Import (Alternative)
1. **Copy the Bruno-friendly cURL commands** from the section above (with full URLs)
2. **In Bruno**:
   - Click "Import" → "cURL"
   - Paste the cURL command
   - Bruno will automatically parse the request
3. **Set up environment variables** in Bruno:
   - Go to Environments
   - Create a new environment with:
      - `baseUrl`: `http://localhost:8082`
      - `accessToken`: (set after login)
      - `refreshToken`: (set after login)
4. **Update imported requests** to use variables:
   - Change `http://localhost:8082` to `{{baseUrl}}`
   - Change `YOUR_JWT_TOKEN_HERE` to `{{accessToken}}`
   - Change `YOUR_REFRESH_TOKEN_HERE` to `{{refreshToken}}`

### Postman Import

1. **Copy any cURL command** (Bruno-friendly versions work best)
2. **In Postman**:
   - Click "Import" → "Raw text"
   - Paste the cURL command
   - Click "Continue" → "Import"
3. **Environment Variables** (Create in Postman):
   - `baseUrl`: `http://localhost:8082`
   - `accessToken`: (set after login)
   - `refreshToken`: (set after login)

### Insomnia Import

1. **Copy any Bruno-friendly cURL command**
2. **In Insomnia**:
   - Click "Import/Export" → "Import Data" → "From Clipboard"
   - Or use "Create" → "HTTP Request" and paste the cURL command
3. **Environment Setup**:
   - Create environment with `baseUrl`, `accessToken`, `refreshToken`
   - Use `{{ _.baseUrl }}` syntax for variables

### Testing Workflow

1. **Start the application**: `./docker-scripts/start.sh`
2. **Register admin user** (gets ADMIN role automatically)
3. **Login admin** → copy `token` to `accessToken` and `adminToken` variables
4. **Register regular user**
5. **Login user** → copy `token` to `accessToken`, `refreshToken` to `refreshToken`
6. **Test protected endpoints** using the stored tokens
7. **Try admin operations** with admin token
8. **Test profile management** with user token

### Collection Structure for API Clients:
```
📁 User Management API
├── 📁 Authentication
│   ├── POST Register Admin
│   ├── POST Register User  
│   ├── POST Login Admin
│   ├── POST Login User
│   ├── POST Refresh Token
│   ├── POST Logout
│   ├── POST Verify Email
│   ├── POST Resend Verification Email
│   ├── POST Forgot Password
│   └── POST Reset Password
├── 📁 Admin Operations
│   ├── POST Test Admin Access
│   ├── POST Test No Auth
│   ├── POST Add Role to User
│   └── POST Add Role (Error Cases)
├── 📁 Profile Endpoints
│   ├── GET Get Current User Profile
│   └── PUT Update Current User Profile
└── 📁 Error Cases
    ├── POST Register Duplicate
    ├── POST Login Wrong Password
    └── POST Unauthorized Access
```

### Quick Import Tip for Bruno Users:

Use the **Bruno-friendly cURL commands** at the top of this document for easiest import. They have full URLs instead of
shell variables like `${BASE_URL}`.

## Notes:

1. **Port**: Adjust the port from 8082 to 8081 if needed for local development without Docker
2. **JWT Tokens**:
    - **Access Token**: Expires in 15 minutes, used for API authorization
    - **Refresh Token**: Expires in 7 days, used to get new access tokens
    - Copy tokens from login responses and use in subsequent requests
3. **Token Flow**: When access token expires, use refresh token to get a new one
4. **Role Assignment**: First user gets ADMIN role, subsequent users get USER role
5. **Error Handling**: The API returns descriptive error messages for debugging
6. **Logging**: Check application logs for detailed information about each request
7. **Security**: One refresh token per user (logging in invalidates previous refresh token)

Save this file and use it as a reference for testing your User Management API!