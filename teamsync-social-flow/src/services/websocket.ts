import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Message } from '@/types/messages';

export interface WebSocketMessage {
  type?: 'CREATE' | 'UPDATE' | 'DELETE';
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

  private messageHandlers: Map<string, (message: WebSocketMessage) => void> = new Map();
  private connectionHandlers: Array<(connected: boolean) => void> = [];

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    try {
      const socket = new SockJS('http://localhost:8091/ws');
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
        this.connectionHandlers.forEach(handler => handler(true));
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.isConnected = false;
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
    if (this.client && this.isConnected) {
      this.client.deactivate();
    }
  }

  public subscribeToChannel(channelId: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to channel');
      return;
    }

    const subscription = this.client.subscribe(`/topic/channel/${channelId}`, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        console.log('Received channel message:', data);
        handler(data);
      } catch (error) {
        console.error('Error parsing channel message:', error);
      }
    });

    this.messageHandlers.set(`channel-${channelId}`, handler);
    return subscription;
  }

  public subscribeToUser(userId: string, handler: (message: WebSocketMessage) => void) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe to user');
      return;
    }

    const subscription = this.client.subscribe(`/topic/user/${userId}`, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        console.log('Received user message:', data);
        handler(data);
      } catch (error) {
        console.error('Error parsing user message:', error);
      }
    });

    this.messageHandlers.set(`user-${userId}`, handler);
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
}

// Create a singleton instance
export const webSocketService = new WebSocketService();
export default webSocketService;
