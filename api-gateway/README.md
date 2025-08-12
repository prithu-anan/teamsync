# API Gateway with JWT Authentication

This API Gateway provides secure access to all microservices using JWT authentication from the auth service.

## Features

- JWT token validation for all secured endpoints
- Inter-service communication with auth service for token validation
- Route forwarding to appropriate microservices
- CORS configuration for frontend integration
- Health check endpoints

## Architecture

```
Client Request → API Gateway → JWT Validation → Route to Microservice
```

## Security Configuration

### Public Endpoints (No Authentication Required)
- `/health` - Gateway health check
- `/auth/**` - Authentication endpoints (login, register, etc.)
- `/test/public` - Public test endpoint

### Secured Endpoints (JWT Required)
- `/api/**` - All API endpoints from teamsync-backend
- `/api/users/**` - User management endpoints
- `/test/secure` - Secure test endpoint

## JWT Token Format

All secured requests must include a valid JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Getting Started

### 1. Start the Network
```bash
docker network create teamsync_network
```

### 2. Start Auth Service
```bash
cd auth-service
docker compose up -d
```

### 3. Start API Gateway
```bash
cd api-gateway
docker compose up -d
```

### 4. Test Authentication

#### Get a JWT Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

#### Access Secured Endpoint
```bash
curl -X GET http://localhost:8080/test/secure \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Configuration

### JWT Secret
The JWT secret must match between the auth service and API gateway:

```properties
jwt.secret=RcAmnC4JkqqjiIt+V12ZlkKcRfPG4k4Hna+IRuGDloI=
```

### Service URLs
```properties
auth.service.url=http://auth-service:8081
user-management.service.url=http://user-management-service:8082
teamsync-backend.service.url=http://teamsync-backend:8083
```

## Troubleshooting

### Common Issues

1. **JWT Validation Failed**: Ensure the JWT secret matches between auth service and gateway
2. **Service Unreachable**: Check if all services are running and connected to the same network
3. **CORS Issues**: Verify the CORS configuration matches your frontend requirements

### Logs
Enable debug logging by setting:
```properties
logging.level.com.teamsync.api_gateway=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Development

### Adding New Routes
Update the `Routes.java` file to add new service routes:

```java
@Bean
public RouterFunction<ServerResponse> newService() {
    return GatewayRouterFunctions.route("new_service")
        .route(RequestPredicates.path("/api/new/**"), 
               HandlerFunctions.http("http://new-service:8084"))
        .build();
}
```

### Custom Security Rules
Modify `SecurityConfig.java` to add custom security rules for new endpoints.
