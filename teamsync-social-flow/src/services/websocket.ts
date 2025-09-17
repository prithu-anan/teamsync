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

class WebSocketService {
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

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    try {
      const socket = new SockJS('http://13.60.242.32:8091/ws');
      this.client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          console.log('WebSocket Debug:', str);
        },
      onConnect: () => {
        console.log('WebSocket connected');
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
      }, 10000);

      this.connectionHandlers.push((connected) => {
        if (connected) {
          clearTimeout(timeout);
          resolve();
        }
      });

      this.client.activate();
    });
  }

  public disconnect() {
    this.stopHeartbeat();
    if (this.client && this.isConnected) {
      this.client.deactivate();
    }
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

    // Check if already subscribed to avoid duplicates
    const handlerKey = `channel-${channelId}`;
    if (this.messageHandlers.has(handlerKey)) {
      console.log(`Already subscribed to channel ${channelId}, skipping duplicate subscription`);
      return;
    }

    const subscription = this.client.subscribe(`/topic/channel/${channelId}`, (message: IMessage) => {
      try {
        this.lastHeartbeat = Date.now(); // Update heartbeat on message received
        const data = JSON.parse(message.body);
        console.log('Received channel message:', data);
        handler(data);
      } catch (error) {
        console.error('Error parsing channel message:', error);
        // Try to handle malformed messages gracefully
        handler({
          type: 'ERROR',
          error: 'Failed to parse message',
          rawData: message.body
        });
      }
    }, {
      ack: 'client' // Enable acknowledgment
    });

    this.messageHandlers.set(handlerKey, handler);
    console.log(`Subscribed to channel ${channelId}`);
    return subscription;
  }

  public subscribeToUser(userId: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to user');
      return;
    }

    // Check if already subscribed to avoid duplicates
    const handlerKey = `user-${userId}`;
    if (this.messageHandlers.has(handlerKey)) {
      console.log(`Already subscribed to user ${userId}, skipping duplicate subscription`);
      return;
    }

    const subscription = this.client.subscribe(`/topic/user/${userId}`, (message: IMessage) => {
      try {
        this.lastHeartbeat = Date.now(); // Update heartbeat on message received
        const data = JSON.parse(message.body);
        console.log('Received user message:', data);
        handler(data);
      } catch (error) {
        console.error('Error parsing user message:', error);
        // Try to handle malformed messages gracefully
        handler({
          type: 'ERROR',
          error: 'Failed to parse message',
          rawData: message.body
        });
      }
    }, {
      ack: 'client' // Enable acknowledgment
    });

    this.messageHandlers.set(handlerKey, handler);
    console.log(`Subscribed to user ${userId}`);
    return subscription;
  }

  public unsubscribeFromChannel(channelId: string) {
    const handlerKey = `channel-${channelId}`;
    this.messageHandlers.delete(handlerKey);
  }

  public unsubscribeFromUser(userId: string) {
    const handlerKey = `user-${userId}`;
    this.messageHandlers.delete(handlerKey);
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
