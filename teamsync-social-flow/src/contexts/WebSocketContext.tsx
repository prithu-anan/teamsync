import React, { createContext, useContext, useEffect, useState, ReactNode, useCallback } from 'react';
import { webSocketService, WebSocketMessage } from '@/services/websocket';
import type { Message } from '@/types/messages';

interface WebSocketContextType {
  isConnected: boolean;
  onMessageReceived: (message: Message) => void;
  onMessageUpdated: (message: Message) => void;
  onMessageDeleted: (messageId: string) => void;
  subscribeToChannel: (channelId: string, handler: (message: WebSocketMessage) => void) => void;
  subscribeToUser: (userId: string, handler: (message: WebSocketMessage) => void) => void;
  unsubscribeFromChannel: (channelId: string) => void;
  unsubscribeFromUser: (userId: string) => void;
}

const WebSocketContext = createContext<WebSocketContextType | undefined>(undefined);

export const useWebSocketContext = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocketContext must be used within a WebSocketProvider');
  }
  return context;
};

interface WebSocketProviderProps {
  children: ReactNode;
}

export const WebSocketProvider = ({ children }: WebSocketProviderProps) => {
  const [isConnected, setIsConnected] = useState(false);
  const [messageHandlers, setMessageHandlers] = useState<Map<string, (message: WebSocketMessage) => void>>(new Map());

  // Connect to WebSocket when provider mounts
  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        await webSocketService.connect();
        console.log('WebSocket connected successfully');
        setIsConnected(true);
      } catch (error) {
        console.warn('WebSocket connection failed, continuing without real-time updates:', error);
        setIsConnected(false);
      }
    };

    connectWebSocket();

    // Listen for connection changes
    const handleConnectionChange = (connected: boolean) => {
      setIsConnected(connected);
    };

    webSocketService.onConnectionChange(handleConnectionChange);

    // Cleanup on unmount
    return () => {
      webSocketService.disconnect();
    };
  }, []);

  const handleWebSocketMessage = (data: WebSocketMessage) => {
    console.log('WebSocket message received:', data);

    // Handle different message types
    if (data.type === 'DELETE') {
      if (data.messageId) {
        messageHandlers.forEach(handler => handler(data));
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

      messageHandlers.forEach(handler => handler(data));
    }
  };

  const subscribeToChannel = useCallback((channelId: string, handler: (message: WebSocketMessage) => void) => {
    if (!isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to channel');
      return;
    }

    // Check if already subscribed to avoid duplicates
    const key = `channel-${channelId}`;
    if (messageHandlers.has(key)) {
      console.log(`Already subscribed to channel ${channelId}, skipping duplicate subscription`);
      return;
    }

    const subscription = webSocketService.subscribeToChannel(channelId, handler);
    setMessageHandlers(prev => new Map(prev.set(key, handler)));
    return subscription;
  }, [isConnected, messageHandlers]);

  const subscribeToUser = useCallback((userId: string, handler: (message: WebSocketMessage) => void) => {
    if (!isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to user');
      return;
    }

    // Check if already subscribed to avoid duplicates
    const key = `user-${userId}`;
    if (messageHandlers.has(key)) {
      console.log(`Already subscribed to user ${userId}, skipping duplicate subscription`);
      return;
    }

    const subscription = webSocketService.subscribeToUser(userId, handler);
    setMessageHandlers(prev => new Map(prev.set(key, handler)));
    return subscription;
  }, [isConnected, messageHandlers]);

  const unsubscribeFromChannel = useCallback((channelId: string) => {
    webSocketService.unsubscribeFromChannel(channelId);
    setMessageHandlers(prev => {
      const newMap = new Map(prev);
      newMap.delete(`channel-${channelId}`);
      return newMap;
    });
  }, []);

  const unsubscribeFromUser = useCallback((userId: string) => {
    webSocketService.unsubscribeFromUser(userId);
    setMessageHandlers(prev => {
      const newMap = new Map(prev);
      newMap.delete(`user-${userId}`);
      return newMap;
    });
  }, []);

  const onMessageReceived = (message: Message) => {
    // This will be implemented by components that use this context
    console.log('Message received:', message);
  };

  const onMessageUpdated = (message: Message) => {
    // This will be implemented by components that use this context
    console.log('Message updated:', message);
  };

  const onMessageDeleted = (messageId: string) => {
    // This will be implemented by components that use this context
    console.log('Message deleted:', messageId);
  };

  const value: WebSocketContextType = {
    isConnected,
    onMessageReceived,
    onMessageUpdated,
    onMessageDeleted,
    subscribeToChannel,
    subscribeToUser,
    unsubscribeFromChannel,
    unsubscribeFromUser,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
};
