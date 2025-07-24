# User Management API - Test Commands

This file contains cURL commands to test all endpoints in the User Management application.

**Note:** We follow a Docker-first approach for development and testing. Ensure Docker is installed and running before
proceeding.

**Prerequisites**: Start the application with Docker using `./docker-scripts/start.sh` before running these commands.

## Base Configuration

```bash
# Docker setup (recommended) - port 8082
BASE_URL="http://localhost:8082"

# Local development with Docker database - port 8081
# BASE_URL="http://localhost:8081"
```

## 1. Authentication Endpoints

### Register First User (Will be ADMIN)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Register Second User (Will be USER)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123"
  }'
```

### Register Third User (Will be USER)

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "password": "user456"
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
    "password": "different123"
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
    "password": "password123"
  }'
```

### Register with Missing Password

```bash
curl -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser"
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

Here's a complete workflow to test the entire application:

```bash
#!/bin/bash

BASE_URL="http://localhost:8082"

echo "=== 1. Registering Admin User ==="
ADMIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}')
echo "Response: $ADMIN_RESPONSE"

echo -e "\n=== 2. Registering Regular User ==="
USER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "user123"}')
echo "Response: $USER_RESPONSE"

echo -e "\n=== 3. Admin Login ==="
ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}')
echo "Response: $ADMIN_LOGIN"

# Extract token (requires jq tool)
if command -v jq &> /dev/null; then
    ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.token')
    echo "Admin Token: $ADMIN_TOKEN"
    
    echo -e "\n=== 4. Testing Admin Access ==="
    ADMIN_TEST=$(curl -s -X POST "${BASE_URL}/api/admin/test" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    echo "Response: $ADMIN_TEST"
    
    echo -e "\n=== 5. Adding Role to User ==="
    ADD_ROLE=$(curl -s -X POST "${BASE_URL}/api/admin/addRole?username=user1&roleName=USER" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    echo "Response: $ADD_ROLE"
else
    echo "Note: Install 'jq' to automatically extract JWT tokens"
    echo "Manually copy the token from the login response above"
fi

echo -e "\n=== 6. User Login ==="
USER_LOGIN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "user123"}')
echo "Response: $USER_LOGIN"

echo -e "\n=== 7. Refresh Token Test ==="
REFRESH_TOKEN=$(echo $USER_LOGIN | jq -r '.refreshToken')
echo "Refresh Token: $REFRESH_TOKEN"

echo -e "\n=== 8. Refresh Access Token ==="
REFRESH_ACCESS_TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")
echo "Response: $REFRESH_ACCESS_TOKEN"

echo -e "\n=== 9. Logout ==="
LOGOUT=$(curl -s -X POST "${BASE_URL}/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")
echo "Response: $LOGOUT"

echo -e "\n=== Test Complete ==="
```

## 6. Postman Collection Format

To import into Postman, create a collection with these requests:

### Environment Variables (Create in Postman)

- `baseUrl`: `http://localhost:8082`
- `adminToken`: (set after admin login)
- `userToken`: (set after user login)

### Collection Structure:

```
📁 User Management API
├── 📁 Authentication
│   ├── POST Register Admin
│   ├── POST Register User
│   ├── POST Login Admin
│   ├── POST Login User
│   ├── POST Refresh Token
│   └── POST Logout
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