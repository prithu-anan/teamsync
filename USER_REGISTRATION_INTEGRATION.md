# User Registration Integration

This document describes how user registration is now integrated between the `auth-service` and `user-management-service`.

## Overview

When a user registers through the `auth-service`, the system now automatically creates the user in both:
1. **auth-service database** - for authentication and authorization
2. **user-management-service database** - for profile management and user data

## Architecture

```
User Registration Flow:
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Client App    │───▶│   Auth Service   │───▶│ User Management     │
│                 │    │                  │    │ Service             │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                              │                           │
                              ▼                           ▼
                       ┌─────────────┐           ┌─────────────────┐
                       │ Auth DB     │           │ User Management │
                       │ (auth_db)   │           │ DB (user_       │
                       │             │           │  management_db) │
                       └─────────────┘           └─────────────────┘
```

## Implementation Details

### 1. UserManagementClient

The `auth-service` now includes a `UserManagementClient` that handles HTTP communication with the `user-management-service`.

**Location**: `auth-service/src/main/java/com/teamsync/auth/service/UserManagementClient.java`

**Features**:
- Automatic retry logic (3 attempts with exponential backoff)
- Proper error handling and logging
- Timeout configuration (5s connect, 10s read)

### 2. Configuration

**Docker Environment**:
```properties
user.management.service.url=http://user_management_service:8082
```

**Local Development**:
```properties
user.management.service.url=http://localhost:8082
```

**Profile-based Configuration**:
- Use `--spring.profiles.active=local` for local development
- Default profile uses Docker container names

### 3. Error Handling

The integration is designed to be resilient:
- If user creation in `user-management-service` fails, the user is still created in `auth-service`
- This ensures users can authenticate even if profile management is temporarily unavailable
- Comprehensive logging for debugging and monitoring

## Usage

### Starting the Services

1. **Create the shared network**:
```bash
docker network create teamsync_network
```

2. **Start user-management-service**:
```bash
cd user-management-service
docker compose up -d
```

3. **Start auth-service**:
```bash
cd auth-service
docker compose up -d
```

### Local Development

1. **Start user-management-service locally**:
```bash
cd user-management-service
mvn spring-boot:run
```

2. **Start auth-service with local profile**:
```bash
cd auth-service
mvn spring-boot:run --spring.profiles.active=local
```

## API Endpoints

### User Registration
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword"
}
```

**Response**: User is created in both services and authentication tokens are returned.

## Monitoring and Debugging

### Logs to Watch

**Auth Service**:
- `User created successfully in both auth-service and user-management-service: {email}`
- `Failed to create user in user-management-service: {error}`

**User Management Service**:
- `User created successfully: {email}`

### Health Checks

Both services include health check endpoints:
- Auth Service: `http://localhost:8081/actuator/health`
- User Management Service: `http://localhost:8082/api/health`

## Troubleshooting

### Common Issues

1. **Network Connectivity**:
   - Ensure both services are on the same Docker network
   - Check container names and ports

2. **Database Issues**:
   - Verify PostgreSQL containers are running and healthy
   - Check database connection strings

3. **Service Unavailable**:
   - Check if user-management-service is running
   - Review logs for connection errors

### Debug Commands

```bash
# Check network connectivity
docker network inspect teamsync_network

# View service logs
docker logs auth_service
docker logs user_management_service

# Test service endpoints
curl http://localhost:8081/actuator/health
curl http://localhost:8082/api/health
```

## Future Enhancements

- Circuit breaker pattern for better resilience
- Event-driven architecture using message queues
- Metrics and monitoring integration
- User synchronization for updates and deletions
