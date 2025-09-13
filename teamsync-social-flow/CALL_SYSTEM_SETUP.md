# Video Call System Setup

## Overview
The video call system uses ZEGOCLOUD for video calling with a call state management system for tracking active calls and rejoin functionality.

## Environment Variables

Add these to your `.env` file:

```bash
# ZEGOCLOUD Configuration
VITE_ZEGO_APP_ID=your_app_id_here
VITE_ZEGO_SERVER_SECRET=your_server_secret_here
```

## Features Implemented

### 1. Call State Management
- Active calls are tracked across the application
- Multiple calls can be active simultaneously
- Call participants and metadata are managed

### 2. Call Rejoin Functionality
- After closing the call window (not ending the call), users see a rejoin notification
- Rejoin notifications show call duration and participant count
- Users can rejoin or dismiss the notification

### 3. Call Invitation System (Ready for Backend Integration)
- Call invitation infrastructure is in place
- Currently simulated for testing
- Ready to integrate with your backend notification system

## How It Works

### Starting a Call
1. User clicks call button in conversation
2. Call state stored in CallContext
3. Video call modal opens
4. Call invitation logged (ready for backend integration)

### Call Rejoin
1. User closes call window (call remains active)
2. Rejoin notification appears in bottom-right corner
3. Shows call duration and participant count
4. User can rejoin or dismiss

## Backend Integration for Full Functionality

To add real-time call invitations, your backend needs to:

1. **Call Invitation API**:
   - Endpoint to send call invitations to channel members
   - Track which users are in which channels
   - Send notifications via your preferred method (WebSocket, push notifications, etc.)

2. **Call State Management**:
   - Store active calls in database
   - Handle user reconnections
   - Clean up ended calls

3. **Integration Options**:
   - Use your existing WebSocket infrastructure
   - Send push notifications to mobile devices
   - Send email/SMS notifications
   - Use your existing notification system

## Message Types

### Call Invitation
```json
{
  "type": "call_invitation",
  "roomID": "room_123_456789",
  "channel": { /* channel object */ },
  "caller": { /* user object */ },
  "callType": "video",
  "timestamp": "2025-01-13T00:00:00.000Z"
}
```

### Call Response
```json
{
  "type": "call_response",
  "roomID": "room_123_456789",
  "userID": "user_456",
  "response": "accept",
  "timestamp": "2025-01-13T00:00:00.000Z"
}
```

### Call End
```json
{
  "type": "call_end",
  "roomID": "room_123_456789",
  "timestamp": "2025-01-13T00:00:00.000Z"
}
```

## Testing

1. Open two browser windows/tabs
2. Login as different users
3. Start a call from one user
4. The other user should receive an incoming call notification
5. Accept the call to join the video call
6. Close the call window and verify rejoin notification appears

## Notes

- The system works with both direct messages and group channels
- Call invitations are only sent to other channel members (not the caller)
- WebSocket connection is established automatically
- Call state persists until explicitly ended or user dismisses rejoin notification
