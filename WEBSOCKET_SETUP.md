# WebSocket Setup for Real-time Messaging

This document describes the WebSocket implementation for real-time messaging between the message service and frontend.

## Backend (Message Service)

### Dependencies Added
- `spring-boot-starter-websocket` - WebSocket support for Spring Boot

### Configuration
- **WebSocketConfig.java**: Configures STOMP endpoints and message broker
  - Endpoint: `/ws` with SockJS fallback
  - Message broker: `/topic` for broadcasting
  - Application prefix: `/app`

### Services
- **WebSocketService.java**: Handles message broadcasting
  - `broadcastNewMessage()` - Broadcasts new channel messages
  - `broadcastMessageUpdate()` - Broadcasts message updates
  - `broadcastMessageDeletion()` - Broadcasts message deletions
  - `broadcastDirectMessage()` - Broadcasts direct messages
  - `broadcastDirectMessageUpdate()` - Broadcasts direct message updates
  - `broadcastDirectMessageDeletion()` - Broadcasts direct message deletions

### Controller Updates
- **MessageController.java**: Integrated WebSocket broadcasting
  - POST `/channels/{channelId}/messages` - Broadcasts new messages
  - PUT `/channels/{channelId}/messages/{messageId}` - Broadcasts updates
  - DELETE `/channels/{channelId}/messages/{messageId}` - Broadcasts deletions
  - POST `/files` - Broadcasts file messages

## Frontend (React)

### Dependencies Added
- `sockjs-client` - WebSocket client library
- `@stomp/stompjs` - STOMP protocol implementation

### Services
- **websocket.ts**: WebSocket service singleton
  - Connection management with auto-reconnection
  - Subscription management for channels and users
  - Message handling and parsing

### Hooks
- **useWebSocket.ts**: React hook for WebSocket integration
  - Manages subscriptions based on selected channel
  - Handles message events (create, update, delete)
  - Automatic cleanup on component unmount

### Component Updates
- **Messages.tsx**: Integrated WebSocket functionality
  - Real-time message updates without page refresh
  - Connection status indicator
  - Optimistic updates when WebSocket is disconnected

## WebSocket Topics

### Channel Messages
- **Topic**: `/topic/channel/{channelId}`
- **Events**: CREATE, UPDATE, DELETE
- **Payload**: MessageResponseDTO or deletion info

### Direct Messages
- **Topic**: `/topic/user/{userId}`
- **Events**: CREATE, UPDATE, DELETE
- **Payload**: MessageResponseDTO or deletion info

## Connection Flow

1. Frontend connects to `/ws` endpoint
2. Subscribes to relevant topics based on selected channel
3. Receives real-time updates for:
   - New messages
   - Message edits
   - Message deletions
4. Updates UI without page refresh

## Error Handling

- Automatic reconnection with exponential backoff
- Fallback to optimistic updates when WebSocket is disconnected
- Connection status indicator in UI
- Graceful degradation when WebSocket is unavailable

## Testing

1. Start the message service (port 8091)
2. Start the frontend application
3. Open multiple browser tabs/windows
4. Send messages in one tab - they should appear in real-time in other tabs
5. Edit/delete messages - changes should be reflected immediately

## Configuration

- **Backend Port**: 8091 (configurable in application.properties)
- **WebSocket Endpoint**: `/ws`
- **CORS**: Currently allows all origins (configure for production)
- **Reconnection**: Max 5 attempts with exponential backoff
