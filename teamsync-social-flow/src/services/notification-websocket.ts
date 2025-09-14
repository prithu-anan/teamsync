import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { NotificationWebSocketMessage } from '@/types/notifications';

class NotificationWebSocketService {
  private client: Client | null = null;
  private isConnected = false;
  private connectionHandlers: Array<(connected: boolean) => void> = [];

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    try {
      const socket = new SockJS('http://notification-service:8092/ws');
      this.client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          // console.log('[Notification WS]', str);
        },
        onConnect: () => {
          this.isConnected = true;
          this.connectionHandlers.forEach(h => h(true));
        },
        onDisconnect: () => {
          this.isConnected = false;
          this.connectionHandlers.forEach(h => h(false));
        },
        onStompError: () => {
          this.isConnected = false;
          this.connectionHandlers.forEach(h => h(false));
        }
      });
    } catch (e) {
      this.client = null;
    }
  }

  public async connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.client) {
        this.initializeClient();
      }
      if (!this.client) return reject(new Error('Notification WS client not initialized'));
      if (this.isConnected) return resolve();

      const timeout = setTimeout(() => reject(new Error('Notification WS connection timeout')), 10000);
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

  public onConnectionChange(handler: (connected: boolean) => void) {
    this.connectionHandlers.push(handler);
  }

  public subscribeToUserNotifications(userId: string, handler: (msg: NotificationWebSocketMessage) => void) {
    if (!this.client || !this.isConnected) return;
    return this.client.subscribe(`/topic/user/${userId}/notifications`, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body) as NotificationWebSocketMessage;
        handler(data);
      } catch (e) {
        // swallow
      }
    });
  }
}

export const notificationWebSocketService = new NotificationWebSocketService();
export default notificationWebSocketService;
