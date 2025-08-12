# JWT Security Setup for API Gateway

## Overview

This document describes the complete JWT authentication setup implemented for the TeamSync API Gateway. The gateway now secures all endpoints using JWT tokens from the auth service, with inter-service communication for additional validation.

## Architecture

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   Client    │───▶│ API Gateway  │───▶│ Microservice│
│             │    │              │    │             │
└─────────────┘    └──────────────┘    └─────────────┘
                          │
                          ▼
                   ┌──────────────┐
                   │ Auth Service │
                   │ (JWT Issuer) │
                   └──────────────┘
```

## Components Implemented

### 1. API Gateway Security

#### JWT Provider (`JwtProvider.java`)
- Validates JWT tokens using the same secret key as auth service
- Extracts user information from tokens
- Checks token expiration

#### JWT Authentication Filter (`JwtAuthenticationFilter.java`)
- Intercepts all requests
- Validates JWT tokens from Authorization header
- Sets Spring Security context for authenticated users

#### Security Configuration (`SecurityConfig.java`)
- Configures which endpoints require authentication
- Sets up CORS for frontend integration
- Applies JWT filter to all requests

### 2. Inter-Service Communication

#### Auth Service Client (`AuthServiceClient.java`)
- WebClient-based communication with auth service
- Additional token validation endpoint
- User information retrieval

#### Token Validation Endpoint
- Added `/auth/validate` endpoint to auth service
- Returns boolean indicating token validity
- Checks both JWT validity and blacklist status

### 3. Route Configuration

#### Updated Routes (`Routes.java`)
- Auth service: `/auth/**`
- User management: `/api/users/**`
- Teamsync backend: `/api/**`
- Health check: `/health`

## Security Rules

### Public Endpoints (No Authentication)
- `/health` - Gateway health check
- `/auth/**` - Authentication endpoints
- `/test/public` - Public test endpoint

### Secured Endpoints (JWT Required)
- `/api/**` - All API endpoints
- `/api/users/**` - User management
- `/test/secure` - Secure test endpoint

## Configuration

### JWT Secret
Both auth service and API gateway must use the same JWT secret:
```properties
jwt.secret=RcAmnC4JkqqjiIt+V12ZlkKcRfPG4k4Hna+IRuGDloI=
```

### Service URLs
```properties
auth.service.url=http://auth-service:8081
user-management.service.url=http://user-management-service:8082
teamsync-backend.service.url=http://teamsync-backend:8083
```

## How It Works

### 1. Client Request
```
GET /api/users/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. JWT Validation
- Gateway extracts JWT from Authorization header
- Validates token signature and expiration
- Checks if token is blacklisted (via auth service)

### 3. Route Forwarding
- If valid: Request forwarded to appropriate microservice
- If invalid: Returns 401 Unauthorized

### 4. Response
- Microservice processes request
- Response returned through gateway to client

## Testing

### Test Scripts
- `test-setup.sh` (Linux/Mac)
- `test-setup.bat` (Windows)

### Manual Testing
```bash
# 1. Get JWT token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# 2. Use token for secured endpoint
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/test/secure
```

## Deployment

### 1. Create Network
```bash
docker network create teamsync_network
```

### 2. Start Services
```bash
# Auth Service
cd auth-service && docker compose up -d

# API Gateway
cd api-gateway && docker compose up -d

# Other services as needed
```

### 3. Verify Setup
```bash
# Test health endpoint
curl http://localhost:8080/health

# Test secured endpoint (should fail without token)
curl http://localhost:8080/test/secure
```

## Security Features

### JWT Validation
- Signature verification using HMAC-SHA256
- Expiration time checking
- Token blacklist validation

### CORS Configuration
- Configurable allowed origins
- Support for credentials
- Pre-flight request handling

### Error Handling
- Graceful failure for invalid tokens
- Proper HTTP status codes
- Security context clearing on logout

## Monitoring and Debugging

### Logging
Enable debug logging in `application.properties`:
```properties
logging.level.com.teamsync.api_gateway=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Health Checks
- Gateway health: `/health`
- Service connectivity monitoring
- JWT validation status

## Best Practices

### Token Management
- Use short-lived JWT tokens (configurable)
- Implement refresh token mechanism
- Blacklist revoked tokens

### Security Headers
- CORS properly configured
- CSRF disabled (stateless API)
- Secure session management

### Error Responses
- Don't leak sensitive information
- Consistent error format
- Proper HTTP status codes

## Troubleshooting

### Common Issues

1. **JWT Validation Fails**
   - Check JWT secret matches between services
   - Verify token format and expiration
   - Check auth service connectivity

2. **Service Unreachable**
   - Verify Docker network configuration
   - Check service ports and health
   - Ensure proper service discovery

3. **CORS Issues**
   - Verify CORS configuration
   - Check frontend origin settings
   - Test pre-flight requests

### Debug Steps

1. Check gateway logs for JWT validation
2. Verify auth service is responding
3. Test token validation endpoint directly
4. Check network connectivity between services

## Future Enhancements

### Planned Features
- Rate limiting per user/IP
- Token refresh handling
- Role-based access control
- Audit logging
- Metrics and monitoring

### Scalability Considerations
- JWT validation caching
- Load balancing support
- Circuit breaker patterns
- Distributed tracing
