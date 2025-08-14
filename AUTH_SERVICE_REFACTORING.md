# Auth Service Refactoring: Minimal User Data Storage

## Overview
The auth-service has been refactored to store only minimal user information required for authentication, while delegating user profile management to the user-management-service.

## Changes Made

### 1. Database Schema Changes
- **Removed columns**: `profile_picture`, `designation`, `birthdate`, `join_date`, `predicted_burnout_risk`
- **Added columns**: `is_active`, `created_at`, `last_login_at`
- **New migration**: `V2__refactor_users_table.sql`

### 2. Entity Changes (`Users.java`)
**Before:**
```java
@Entity
public class Users {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String profilePicture;
    private String designation;
    private LocalDate birthdate;
    private LocalDate joinDate;
    private Boolean predictedBurnoutRisk;
}
```

**After:**
```java
@Entity
public class Users {
    private Long id;
    private String email;
    private String password;
    private String name;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

### 3. DTO Changes
- **UserResponseDTO**: Now only contains minimal auth-related fields
- **UserUpdateRequestDTO**: Simplified to only handle name updates

### 4. Service Layer Changes
- **AuthService**: 
  - User profile updates now delegate to user-management-service
  - Only handles authentication-related operations
  - Tracks last login time
- **UserManagementClient**: Added `updateUserProfile()` method

### 5. Mapper Changes
- **UserMapper**: Removed mappings for non-existent fields
- **AuthMapper**: No changes needed

## Benefits

1. **Separation of Concerns**: Auth service focuses solely on authentication
2. **Reduced Data Duplication**: User profile data stored in one place only
3. **Better Scalability**: Auth service database is lighter and faster
4. **Clearer API Boundaries**: Each service has a well-defined responsibility

## Migration Notes

1. **Database Migration**: Run the new Flyway migration `V2__refactor_users_table.sql`
2. **Data Loss**: Profile-related data will be lost during migration
3. **Service Coordination**: Ensure user-management-service is running before auth-service

## API Changes

- User profile updates now go through user-management-service
- Auth service only handles authentication, password changes, and basic user info
- User creation still creates records in both services (auth for login, user-management for profile)

## Testing

- Updated test files to reflect new entity structure
- Removed references to old profile fields
- Tests now use minimal user data structure
