export interface Notification {
  id: string;
  userId: number;
  type: string;
  title: string;
  message: string;
  metadata?: Record<string, any>;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export interface NotificationCount {
  userId: number;
  unreadCount: number;
}

export interface NotificationWebSocketMessage {
  type: 'NEW_NOTIFICATION' | 'NOTIFICATION_READ' | 'NOTIFICATION_COUNT_UPDATE';
  notificationId?: string;
  userId?: number;
  notificationType?: string;
  title?: string;
  message?: string;
  metadata?: Record<string, any>;
  isRead?: boolean;
  createdAt?: string;
  readAt?: string;
  unreadCount?: number;
}

export interface NotificationApiResponse {
  code: number;
  status: string;
  message: string;
  data: Notification[] | NotificationCount | Notification;
}
