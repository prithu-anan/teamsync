import { useEffect, useRef, useCallback } from 'react';
import { webSocketService, WebSocketMessage } from '@/services/websocket';
import type { Message, Channel } from '@/types/messages';

interface UseWebSocketProps {
  selectedChannel: Channel | null;
  userId: string | undefined;
  onMessageReceived: (message: Message) => void;
  onMessageUpdated: (message: Message) => void;
  onMessageDeleted: (messageId: string) => void;
}

export const useWebSocket = ({
  selectedChannel,
  userId,
  onMessageReceived,
  onMessageUpdated,
  onMessageDeleted,
}: UseWebSocketProps) => {
  const subscriptionsRef = useRef<{ channel?: any; user?: any }>({});

  const handleWebSocketMessage = useCallback((data: WebSocketMessage) => {
    console.log('WebSocket message received:', data);

    // Handle different message types
    if (data.type === 'DELETE') {
      if (data.messageId) {
        onMessageDeleted(data.messageId.toString());
      }
      return;
    }

    // Handle CREATE and UPDATE messages
    if (data.id || data.messageId) {
      const message: Message = {
        id: data.id?.toString() || data.messageId?.toString() || '',
        sender_id: data.sender_id?.toString() || '',
        channel_id: data.channel_id?.toString() || null,
        recipient_id: data.recipient_id?.toString() || null,
        content: data.content || '',
        timestamp: data.timestamp || new Date().toISOString(),
        thread_parent_id: data.thread_parent_id?.toString() || null,
        userName: data.userName || 'Unknown User',
        userAvatar: data.userAvatar || '/placeholder.svg',
        file_url: data.file_url || null,
        file_type: data.file_type || null,
        fileUrl: data.file_url || data.fileUrl || null,
        fileName: data.file_name || data.fileName || 'file',
        imageUrl: data.file_type?.startsWith('image/') ? data.file_url : null,
        reactions: data.reactions || [],
      };

      if (data.type === 'UPDATE') {
        onMessageUpdated(message);
      } else {
        onMessageReceived(message);
      }
    }
  }, [onMessageReceived, onMessageUpdated, onMessageDeleted]);

  // Connect to WebSocket when component mounts
  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        await webSocketService.connect();
        console.log('WebSocket connected successfully');
      } catch (error) {
        console.warn('WebSocket connection failed, continuing without real-time updates:', error);
        // Don't throw error, just log it and continue without WebSocket
      }
    };

    connectWebSocket();

    // Cleanup on unmount
    return () => {
      webSocketService.disconnect();
    };
  }, []);

  // Subscribe to channel messages when selectedChannel changes
  useEffect(() => {
    if (!selectedChannel || !webSocketService.getConnectionStatus()) {
      return;
    }

    // Unsubscribe from previous subscriptions
    if (subscriptionsRef.current.channel) {
      subscriptionsRef.current.channel.unsubscribe();
    }
    if (subscriptionsRef.current.user) {
      subscriptionsRef.current.user.unsubscribe();
    }

    // Subscribe to channel messages
    if (selectedChannel.type === 'group' && selectedChannel.channel_id) {
      const channelSubscription = webSocketService.subscribeToChannel(
        selectedChannel.channel_id,
        handleWebSocketMessage
      );
      subscriptionsRef.current.channel = channelSubscription;
    } else if (selectedChannel.type === 'direct' && selectedChannel.recipient_id) {
      // For direct messages, subscribe to user-specific messages
      const userSubscription = webSocketService.subscribeToUser(
        selectedChannel.recipient_id,
        handleWebSocketMessage
      );
      subscriptionsRef.current.user = userSubscription;
    }

    // Cleanup function
    return () => {
      if (subscriptionsRef.current.channel) {
        subscriptionsRef.current.channel.unsubscribe();
        subscriptionsRef.current.channel = null;
      }
      if (subscriptionsRef.current.user) {
        subscriptionsRef.current.user.unsubscribe();
        subscriptionsRef.current.user = null;
      }
    };
  }, [selectedChannel, handleWebSocketMessage]);

  // Subscribe to user-specific messages for direct messages
  useEffect(() => {
    if (!userId || !webSocketService.getConnectionStatus()) {
      return;
    }

    // Subscribe to messages directed to this user
    const userSubscription = webSocketService.subscribeToUser(
      userId,
      handleWebSocketMessage
    );

    return () => {
      if (userSubscription) {
        userSubscription.unsubscribe();
      }
    };
  }, [userId, handleWebSocketMessage]);

  return {
    isConnected: webSocketService.getConnectionStatus(),
  };
};
