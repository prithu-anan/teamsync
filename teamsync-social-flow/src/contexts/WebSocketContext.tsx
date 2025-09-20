import React, { createContext, useContext, useEffect, useState, ReactNode, useCallback } from 'react';
import { webSocketService, WebSocketMessage } from '@/services/websocket';
import type { Message } from '@/types/messages';
import { useAuth } from '@/contexts/AuthContext';

interface WebSocketContextType {
  isConnected: boolean;
  connectionStatus: 'disconnected' | 'connecting' | 'connected' | 'error';
  retryCount: number;
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
  const { user, isAuthenticated } = useAuth();
  const [isConnected, setIsConnected] = useState(false);
  const [messageHandlers, setMessageHandlers] = useState<Map<string, (message: WebSocketMessage) => void>>(new Map());
  const [connectionAttempted, setConnectionAttempted] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const [maxRetries] = useState(3);
  const [connectionStatus, setConnectionStatus] = useState<'disconnected' | 'connecting' | 'connected' | 'error'>('disconnected');

  // WebSocket handles all cross-device communication automatically
  // No need for BroadcastChannel since it only works within same browser

  // Connect to WebSocket when user is authenticated
  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    const connectWebSocket = async (retryAttempt = 0) => {
      try {
        console.log(`Attempting WebSocket connection for authenticated user: ${user?.email} (attempt ${retryAttempt + 1})`);
        setConnectionAttempted(true);
        setConnectionStatus('connecting');
        setRetryCount(retryAttempt);
        
        await webSocketService.connect();
        console.log('WebSocket connected successfully');
        setIsConnected(true);
        setConnectionStatus('connected');
        setRetryCount(0); // Reset retry count on successful connection
      } catch (error) {
        console.warn(`WebSocket connection failed (attempt ${retryAttempt + 1}):`, error);
        setIsConnected(false);
        setConnectionStatus('error');

        // Retry logic
        if (retryAttempt < maxRetries) {
          const delay = Math.pow(2, retryAttempt) * 1000; // Exponential backoff: 1s, 2s, 4s
          console.log(`Retrying WebSocket connection in ${delay}ms...`);
          setRetryCount(retryAttempt + 1);
          setTimeout(() => {
            connectWebSocket(retryAttempt + 1);
          }, delay);
        } else {
          console.error('Max WebSocket connection retries reached');
          setConnectionAttempted(false);
          setRetryCount(0);
          setConnectionStatus('error');
        }
      }
    };

    // Only attempt connection if not already connected
    if (!isConnected && !connectionAttempted) {
      connectWebSocket();
    }

    // Listen for connection changes
    const handleConnectionChange = (connected: boolean) => {
      setIsConnected(connected);
      if (connected) {
        setRetryCount(0); // Reset retry count when connection is established
        setConnectionStatus('connected');
      } else {
        setConnectionStatus('disconnected');
      }
    };

    webSocketService.onConnectionChange(handleConnectionChange);

    // Cleanup on unmount or when user logs out
    return () => {
      if (!isAuthenticated) {
        webSocketService.disconnect();
        setConnectionAttempted(false);
        setIsConnected(false);
        setRetryCount(0);
      }
    };
  }, [isAuthenticated, user?.email, isConnected, connectionAttempted, maxRetries]);

  // Disconnect WebSocket when user logs out
  useEffect(() => {
    if (!isAuthenticated && connectionAttempted) {
      console.log('User logged out, disconnecting WebSocket');
      webSocketService.disconnect();
      setConnectionAttempted(false);
      setIsConnected(false);
    }
  }, [isAuthenticated, connectionAttempted]);

  const handleWebSocketMessage = useCallback((data: WebSocketMessage) => {
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
      messageHandlers.forEach(handler => handler(data));
    }
  }, [messageHandlers]);

  const subscribeToChannel = useCallback((channelId: string, handler: (message: WebSocketMessage) => void) => {
    if (!isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to channel');
      return;
    }

    // Create unique handler key with timestamp to allow multiple handlers per channel
    const key = `channel-${channelId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    const subscription = webSocketService.subscribeToChannel(channelId, handler);
    setMessageHandlers(prev => new Map(prev.set(key, handler)));
    return subscription;
  }, [isConnected]);

  const subscribeToUser = useCallback((userId: string, handler: (message: WebSocketMessage) => void) => {
    if (!isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to user');
      return;
    }

    // Create unique handler key with timestamp to allow multiple handlers per user
    const key = `user-${userId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    const subscription = webSocketService.subscribeToUser(userId, handler);
    setMessageHandlers(prev => new Map(prev.set(key, handler)));
    return subscription;
  }, [isConnected]);

  const unsubscribeFromChannel = useCallback((channelId: string) => {
    webSocketService.unsubscribeFromChannel(channelId);
    setMessageHandlers(prev => {
      const newMap = new Map(prev);
      // Remove all handlers for this channel
      for (const key of newMap.keys()) {
        if (key.startsWith(`channel-${channelId}-`)) {
          newMap.delete(key);
        }
      }
      return newMap;
    });
  }, []);

  const unsubscribeFromUser = useCallback((userId: string) => {
    webSocketService.unsubscribeFromUser(userId);
    setMessageHandlers(prev => {
      const newMap = new Map(prev);
      // Remove all handlers for this user
      for (const key of newMap.keys()) {
        if (key.startsWith(`user-${userId}-`)) {
          newMap.delete(key);
        }
      }
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

  // WebSocket automatically handles cross-device message synchronization
  // No need for manual broadcasting - the server broadcasts to all connected clients

  const value: WebSocketContextType = {
    isConnected,
    connectionStatus,
    retryCount,
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
