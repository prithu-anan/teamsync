# Notification Service WebSocket Integration

This document explains how the notification service now supports real-time WebSocket notifications for the frontend.

## Overview

The notification service has been enhanced with WebSocket functionality to provide real-time notifications to users. When a new notification is created, it's automatically broadcast to the user via WebSocket, and the frontend can display a notification counter badge and show notifications in a dropdown.

## Features

- **Real-time notifications**: New notifications are instantly sent to users via WebSocket
- **Unread count updates**: The unread notification count is updated in real-time
- **Read status updates**: When notifications are marked as read, the status is broadcast
- **User-specific channels**: Each user receives notifications on their own WebSocket channel
- **Retry mechanism**: WebSocket broadcasts include retry logic for reliability

## WebSocket Endpoints

### Connection
- **Endpoint**: `/ws`
- **Protocol**: WebSocket with SockJS fallback
- **CORS**: Configured for `localhost:3000` and `localhost:3001`

### User Notification Channel
- **Pattern**: `/topic/user/{userId}/notifications`
- **Purpose**: User-specific notification updates

## WebSocket Message Types

### 1. NEW_NOTIFICATION
Sent when a new notification is created for a user.

```json
{
  "type": "NEW_NOTIFICATION",
  "notificationId": "notification_id",
  "userId": 123,
  "notificationType": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "You have been assigned to task 'Fix Bug'",
  "metadata": {
    "taskId": 456,
    "projectId": 789
  },
  "isRead": false,
  "createdAt": "2024-01-15T10:30:00",
  "readAt": null,
  "unreadCount": 5
}
```

### 2. NOTIFICATION_READ
Sent when a notification is marked as read.

```json
{
  "type": "NOTIFICATION_READ",
  "notificationId": "notification_id",
  "userId": 123,
  "unreadCount": 4
}
```

### 3. NOTIFICATION_COUNT_UPDATE
Sent when the unread count changes (e.g., after marking all as read).

```json
{
  "type": "NOTIFICATION_COUNT_UPDATE",
  "userId": 123,
  "unreadCount": 0
}
```

## REST API Endpoints

### Get User Notifications
```
GET /api/notifications
```
Returns all notifications for the authenticated user.

### Get Unread Notifications
```
GET /api/notifications/unread
```
Returns only unread notifications for the authenticated user.

### Get Unread Count
```
GET /api/notifications/count
```
Returns the unread notification count.

### Mark Notification as Read
```
PUT /api/notifications/{notificationId}/read
```
Marks a specific notification as read and broadcasts the update.

### Mark All as Read
```
PUT /api/notifications/read-all
```
Marks all notifications as read and broadcasts the update.

### Delete Notification
```
DELETE /api/notifications/{notificationId}
```
Deletes a specific notification and broadcasts the count update.

### Delete All Notifications
```
DELETE /api/notifications
```
Deletes all notifications for the user and broadcasts the count update.

## Frontend Integration

### WebSocket Connection
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8092/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to user's notification channel
    const userId = getCurrentUserId(); // Get from auth context
    stompClient.subscribe(`/topic/user/${userId}/notifications`, function(message) {
        const notification = JSON.parse(message.body);
        handleNotificationUpdate(notification);
    });
});
```

### Handling Notifications
```javascript
function handleNotificationUpdate(notification) {
    switch(notification.type) {
        case 'NEW_NOTIFICATION':
            // Show notification badge with count
            updateNotificationBadge(notification.unreadCount);
            // Optionally show toast notification
            showToastNotification(notification);
            break;
            
        case 'NOTIFICATION_READ':
            // Update notification list UI
            markNotificationAsRead(notification.notificationId);
            // Update badge count
            updateNotificationBadge(notification.unreadCount);
            break;
            
        case 'NOTIFICATION_COUNT_UPDATE':
            // Update badge count
            updateNotificationBadge(notification.unreadCount);
            break;
    }
}
```

### Notification Badge Component
```javascript
function NotificationBadge() {
    const [unreadCount, setUnreadCount] = useState(0);
    
    useEffect(() => {
        // Fetch initial count
        fetchUnreadCount().then(setUnreadCount);
        
        // Listen for WebSocket updates
        // (WebSocket handling code as shown above)
    }, []);
    
    return (
        <div className="notification-badge">
            <BellIcon />
            {unreadCount > 0 && (
                <span className="badge">{unreadCount}</span>
            )}
        </div>
    );
}
```

### Notification Dropdown
```javascript
function NotificationDropdown() {
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    
    useEffect(() => {
        // Fetch notifications when dropdown opens
        if (isOpen) {
            fetchNotifications().then(setNotifications);
        }
    }, [isOpen]);
    
    const markAsRead = (notificationId) => {
        // Call API to mark as read
        markNotificationAsRead(notificationId);
        // WebSocket will handle the UI update
    };
    
    return (
        <div className={`notification-dropdown ${isOpen ? 'open' : ''}`}>
            {notifications.map(notification => (
                <div 
                    key={notification.id}
                    className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                    onClick={() => markAsRead(notification.id)}
                >
                    <h4>{notification.title}</h4>
                    <p>{notification.message}</p>
                    <span>{formatDate(notification.createdAt)}</span>
                </div>
            ))}
        </div>
    );
}
```

## Security

- WebSocket connections are allowed without authentication for the `/ws` endpoint
- REST API endpoints require authentication (USER or SERVICE authority)
- CORS is configured for frontend origins

## Error Handling

- WebSocket broadcasts include retry logic (3 attempts with 1-second delay)
- Failed broadcasts are logged but don't affect the notification creation
- Frontend should handle connection drops and reconnection

## Testing

### WebSocket Testing with curl
```bash
# Test WebSocket connection (requires websocat)
websocat ws://localhost:8092/ws
```

### API Testing
```bash
# Get notifications
curl -H "Authorization: Bearer <token>" http://localhost:8092/api/notifications

# Mark as read
curl -X PUT -H "Authorization: Bearer <token>" http://localhost:8092/api/notifications/{id}/read
```

## Configuration

The service is configured to run on port 8092 with WebSocket support enabled. All necessary dependencies have been added to the `pom.xml` file.

## Dependencies Added

- `spring-boot-starter-websocket`
- `spring-messaging`
- `spring-retry`
- `spring-aspects`

## Notes

- Notifications are created through the existing Kafka event listeners
- The service maintains backward compatibility with existing notification creation
- WebSocket broadcasts are asynchronous and don't block the main notification creation flow
- Each user has their own notification channel for privacy and security
