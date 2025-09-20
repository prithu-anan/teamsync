import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Message } from '@/types/messages';

export interface WebSocketMessage {
  type?: 'CREATE' | 'UPDATE' | 'DELETE' | 'ERROR';
  messageId?: string;
  channelId?: string;
  recipientId?: string;
  [key: string]: any;
}

export class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // Start with 1 second
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private lastHeartbeat = 0;
  private heartbeatTimeout = 30000; // 30 seconds

  private messageHandlers: Map<string, (message: WebSocketMessage) => void> = new Map();
  private connectionHandlers: Array<(connected: boolean) => void> = [];
  private subscriptions: Map<string, any> = new Map(); // Store STOMP subscriptions to keep them alive

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    try {
      const websocketUrl = import.meta.env.VITE_MESSAGE_WEBSOCKET_URL || 'http://localhost:8091/ws';
      console.log('Initializing WebSocket client with URL:', websocketUrl);
      
      const socket = new SockJS(websocketUrl);
      
      // Add error handling for SockJS
      socket.onerror = (error) => {
        console.error('SockJS error:', error);
      };
      
      socket.onopen = () => {
        console.log('SockJS connection opened');
      };
      
      socket.onclose = (event) => {
        console.log('SockJS connection closed:', event.code, event.reason);
      };
      
      this.client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          console.log('WebSocket Debug:', str);
        },
        onConnect: () => {
          console.log('WebSocket connected successfully');
          this.isConnected = true;
          this.reconnectAttempts = 0;
          this.reconnectDelay = 1000;
          this.lastHeartbeat = Date.now();
          this.startHeartbeat();
          this.connectionHandlers.forEach(handler => handler(true));
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.isConnected = false;
          this.stopHeartbeat();
          this.connectionHandlers.forEach(handler => handler(false));
          this.attemptReconnect();
        },
        onStompError: (frame) => {
          console.error('WebSocket STOMP error:', frame);
          this.attemptReconnect();
        }
      });
    } catch (error) {
      console.error('Failed to initialize WebSocket client:', error);
      this.client = null;
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${this.reconnectDelay}ms`);
      
      setTimeout(() => {
        if (this.client && !this.isConnected) {
          this.client.activate();
        }
      }, this.reconnectDelay);
      
      // Exponential backoff
      this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30000);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  public connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Clean up any existing connection first
      if (this.client) {
        try {
          this.client.deactivate();
        } catch (error) {
          console.warn('Error deactivating existing client:', error);
        }
      }

      // Reinitialize the client to ensure clean state
      this.initializeClient();
      
      if (!this.client) {
        console.warn('WebSocket client not initialized, skipping connection');
        reject(new Error('WebSocket client not initialized'));
        return;
      }

      if (this.isConnected) {
        resolve();
        return;
      }

      const timeout = setTimeout(() => {
        reject(new Error('Connection timeout'));
      }, 15000); // Increased timeout to 15 seconds

      this.connectionHandlers.push((connected) => {
        if (connected) {
          clearTimeout(timeout);
          resolve();
        }
      });

      try {
        this.client.activate();
      } catch (error) {
        console.error('Error activating WebSocket client:', error);
        reject(error);
      }
    });
  }

  public disconnect() {
    this.stopHeartbeat();
    this.isConnected = false;
    
    // Unsubscribe from all subscriptions
    this.subscriptions.forEach(subscription => {
      try {
        subscription.unsubscribe();
      } catch (error) {
        console.warn('Error unsubscribing:', error);
      }
    });
    
    // Clear all maps
    this.messageHandlers.clear();
    this.subscriptions.clear();
    
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (error) {
        console.warn('Error during disconnect:', error);
      }
    }
    // Clear connection handlers
    this.connectionHandlers = [];
  }

  private startHeartbeat() {
    this.stopHeartbeat(); // Clear any existing heartbeat
    this.heartbeatInterval = setInterval(() => {
      if (this.isConnected && this.client) {
        const now = Date.now();
        if (now - this.lastHeartbeat > this.heartbeatTimeout) {
          console.warn('Heartbeat timeout, attempting to reconnect');
          this.isConnected = false;
          this.attemptReconnect();
        } else {
          // Send heartbeat
          try {
            this.client.publish({
              destination: '/app/heartbeat',
              body: JSON.stringify({ timestamp: now })
            });
          } catch (error) {
            console.error('Failed to send heartbeat:', error);
          }
        }
      }
    }, 10000); // Send heartbeat every 10 seconds
  }

  private stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  public subscribeToChannel(channelId: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to channel');
      return;
    }

    // Create unique handler key with timestamp to allow multiple handlers per channel
    const handlerKey = `channel-${channelId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    
    // Check if we already have a subscription for this channel
    const existingSubscriptionKey = `channel-subscription-${channelId}`;
    let subscription = this.subscriptions.get(existingSubscriptionKey);
    
    if (!subscription) {
      // Create new subscription for this channel
      subscription = this.client.subscribe(`/topic/channel/${channelId}`, (message: IMessage) => {
        try {
          this.lastHeartbeat = Date.now(); // Update heartbeat on message received
          const data = JSON.parse(message.body);
          console.log('Received channel message:', data);
          
          // Broadcast to all handlers for this channel
          for (const [key, handler] of this.messageHandlers.entries()) {
            if (key.startsWith(`channel-${channelId}-`)) {
              handler(data);
            }
          }
        } catch (error) {
          console.error('Error parsing channel message:', error);
          // Try to handle malformed messages gracefully
          const errorData: WebSocketMessage = {
            type: 'ERROR',
            error: 'Failed to parse message',
            rawData: message.body
          };
          for (const [key, handler] of this.messageHandlers.entries()) {
            if (key.startsWith(`channel-${channelId}-`)) {
              handler(errorData);
            }
          }
        }
      }, {
        ack: 'client' // Enable acknowledgment
      });
      
      // Store the subscription
      this.subscriptions.set(existingSubscriptionKey, subscription);
      console.log(`Created new subscription for channel ${channelId}`);
    }

    // Store the handler
    this.messageHandlers.set(handlerKey, handler);
    console.log(`Added handler ${handlerKey} to channel ${channelId} (total handlers: ${Array.from(this.messageHandlers.keys()).filter(k => k.startsWith(`channel-${channelId}-`)).length})`);
    return subscription;
  }

  public subscribeToUser(userId: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to user');
      return;
    }

    // Create unique handler key with timestamp to allow multiple handlers per user
    const handlerKey = `user-${userId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    
    // Check if we already have a subscription for this user
    const existingSubscriptionKey = `user-subscription-${userId}`;
    let subscription = this.subscriptions.get(existingSubscriptionKey);
    
    if (!subscription) {
      // Create new subscription for this user
      subscription = this.client.subscribe(`/topic/user/${userId}`, (message: IMessage) => {
        try {
          this.lastHeartbeat = Date.now(); // Update heartbeat on message received
          const data = JSON.parse(message.body);
          console.log('Received user message:', data);
          
          // Broadcast to all handlers for this user
          for (const [key, handler] of this.messageHandlers.entries()) {
            if (key.startsWith(`user-${userId}-`)) {
              handler(data);
            }
          }
        } catch (error) {
          console.error('Error parsing user message:', error);
          // Try to handle malformed messages gracefully
          const errorData: WebSocketMessage = {
            type: 'ERROR',
            error: 'Failed to parse message',
            rawData: message.body
          };
          for (const [key, handler] of this.messageHandlers.entries()) {
            if (key.startsWith(`user-${userId}-`)) {
              handler(errorData);
            }
          }
        }
      }, {
        ack: 'client' // Enable acknowledgment
      });
      
      // Store the subscription
      this.subscriptions.set(existingSubscriptionKey, subscription);
      console.log(`Created new subscription for user ${userId}`);
    }

    // Store the handler
    this.messageHandlers.set(handlerKey, handler);
    console.log(`Added handler ${handlerKey} to user ${userId} (total handlers: ${Array.from(this.messageHandlers.keys()).filter(k => k.startsWith(`user-${userId}-`)).length})`);
    return subscription;
  }

  public unsubscribeFromChannel(channelId: string) {
    // Find and remove all handlers for this channel
    const keysToDelete: string[] = [];
    for (const key of this.messageHandlers.keys()) {
      if (key.startsWith(`channel-${channelId}-`)) {
        keysToDelete.push(key);
      }
    }
    
    keysToDelete.forEach(key => {
      this.messageHandlers.delete(key);
    });
    
    // If no more handlers for this channel, unsubscribe from STOMP
    const hasRemainingHandlers = Array.from(this.messageHandlers.keys()).some(key => key.startsWith(`channel-${channelId}-`));
    if (!hasRemainingHandlers) {
      const subscriptionKey = `channel-subscription-${channelId}`;
      const subscription = this.subscriptions.get(subscriptionKey);
      if (subscription) {
        subscription.unsubscribe();
        this.subscriptions.delete(subscriptionKey);
        console.log(`Unsubscribed from STOMP for channel ${channelId}`);
      }
    }
    
    console.log(`Removed ${keysToDelete.length} handlers from channel ${channelId}`);
  }

  public unsubscribeFromUser(userId: string) {
    // Find and remove all handlers for this user
    const keysToDelete: string[] = [];
    for (const key of this.messageHandlers.keys()) {
      if (key.startsWith(`user-${userId}-`)) {
        keysToDelete.push(key);
      }
    }
    
    keysToDelete.forEach(key => {
      this.messageHandlers.delete(key);
    });
    
    // If no more handlers for this user, unsubscribe from STOMP
    const hasRemainingHandlers = Array.from(this.messageHandlers.keys()).some(key => key.startsWith(`user-${userId}-`));
    if (!hasRemainingHandlers) {
      const subscriptionKey = `user-subscription-${userId}`;
      const subscription = this.subscriptions.get(subscriptionKey);
      if (subscription) {
        subscription.unsubscribe();
        this.subscriptions.delete(subscriptionKey);
        console.log(`Unsubscribed from STOMP for user ${userId}`);
      }
    }
    
    console.log(`Removed ${keysToDelete.length} handlers from user ${userId}`);
  }

  public onConnectionChange(handler: (connected: boolean) => void) {
    this.connectionHandlers.push(handler);
  }

  public getConnectionStatus(): boolean {
    return this.isConnected;
  }

  public sendMessage(message: WebSocketMessage) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot send message');
      return;
    }

    try {
      this.client.publish({
        destination: '/app/message',
        body: JSON.stringify(message)
      });
      console.log('Message sent via WebSocket:', message);
    } catch (error) {
      console.error('Failed to send WebSocket message:', error);
    }
  }

  public subscribe(messageType: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to message type');
      return;
    }

    // Subscribe to messages with specific type
    const handlerKey = `type-${messageType}`;
    if (this.messageHandlers.has(handlerKey)) {
      console.log(`Already subscribed to message type ${messageType}`);
      return;
    }

    // For now, we'll use a generic message subscription
    // In a real implementation, you might want to set up specific subscriptions
    this.messageHandlers.set(handlerKey, handler);
    console.log(`Subscribed to message type ${messageType}`);
    
    return () => {
      this.messageHandlers.delete(handlerKey);
    };
  }
}

// Create a singleton instance
export const webSocketService = new WebSocketService();
export default webSocketService;
