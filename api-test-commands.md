# User Management API - Test Commands

This file contains cURL commands to test all endpoints in the User Management application.

## Base Configuration

```bash
# Set base URL (adjust port if needed)
BASE_URL="http://localhost:8081"
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

### Login as Admin (Save the token for admin operations)

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
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

### Add Role with Missing Parameters

```bash
curl -X POST "${BASE_URL}/api/admin/addRole?username=user1" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## 5. Complete Test Workflow

Here's a complete workflow to test the entire application:

```bash
#!/bin/bash

BASE_URL="http://localhost:8081"

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

echo -e "\n=== Test Complete ==="
```

## 6. Postman Collection Format

To import into Postman, create a collection with these requests:

### Environment Variables (Create in Postman)

- `baseUrl`: `http://localhost:8081`
- `adminToken`: (set after admin login)
- `userToken`: (set after user login)

### Collection Structure:

```
📁 User Management API
├── 📁 Authentication
│   ├── POST Register Admin
│   ├── POST Register User
│   ├── POST Login Admin
│   └── POST Login User
├── 📁 Admin Operations
│   ├── POST Test Admin Access
│   ├── POST Test No Auth
│   ├── POST Add Role to User
│   └── POST Add Role (Error Cases)
└── 📁 Error Cases
    ├── POST Register Duplicate
    ├── POST Login Wrong Password
    └── POST Unauthorized Access
```

## Notes:

1. **Port**: Adjust the port from 8081 to 8080 if needed
2. **JWT Tokens**: Copy the token from login responses and use in subsequent requests
3. **Role Assignment**: First user gets ADMIN role, subsequent users get USER role
4. **Error Handling**: The API returns descriptive error messages for debugging
5. **Logging**: Check application logs for detailed information about each request

Save this file and use it as a reference for testing your User Management API!