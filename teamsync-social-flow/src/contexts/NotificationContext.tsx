import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { notificationWebSocketService } from '@/services/notification-websocket';
import {
  getNotifications,
  getUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
} from '@/utils/api/notifications-api';
import type { Notification, NotificationCount, NotificationWebSocketMessage } from '@/types/notifications';

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  remove: (id: string) => Promise<void>;
  removeAll: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotifications = () => {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error('useNotifications must be used within NotificationProvider');
  return ctx;
};

export const NotificationProvider = ({ children }: { children: React.ReactNode }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    let subscription: any;
    const bootstrap = async () => {
      if (!user?.id) return;
      try {
        setLoading(true);
        const [listRes, countRes] = await Promise.all([
          getNotifications(),
          getUnreadCount(),
        ]);
        const list = Array.isArray(listRes) ? (listRes as Notification[]) : [];
        const count = (countRes as any)?.unreadCount || 0;
        setNotifications(list);
        setUnreadCount(count);
      } finally {
        setLoading(false);
      }

      try {
        await notificationWebSocketService.connect();
        subscription = notificationWebSocketService.subscribeToUserNotifications(String(user.id), (msg: NotificationWebSocketMessage) => {
          if (msg.type === 'NEW_NOTIFICATION') {
            const newItem: Notification = {
              id: msg.notificationId || '',
              userId: msg.userId || Number(user.id),
              type: msg.notificationType || 'INFO',
              title: msg.title || '',
              message: msg.message || '',
              metadata: msg.metadata || {},
              isRead: Boolean(msg.isRead) || false,
              createdAt: msg.createdAt || new Date().toISOString(),
              readAt: msg.readAt,
            };
            setNotifications(prev => [newItem, ...prev]);
            if (typeof msg.unreadCount === 'number') setUnreadCount(msg.unreadCount);
            else setUnreadCount(prev => prev + 1);
          } else if (msg.type === 'NOTIFICATION_READ') {
            setNotifications(prev => prev.map(n => n.id === msg.notificationId ? { ...n, isRead: true, readAt: new Date().toISOString() } : n));
            if (typeof msg.unreadCount === 'number') setUnreadCount(msg.unreadCount);
            else setUnreadCount(prev => Math.max(0, prev - 1));
          } else if (msg.type === 'NOTIFICATION_COUNT_UPDATE') {
            if (typeof msg.unreadCount === 'number') setUnreadCount(msg.unreadCount);
          }
        });
      } catch {}
    };

    bootstrap();

    return () => {
      if (subscription && typeof subscription.unsubscribe === 'function') subscription.unsubscribe();
    };
  }, [user?.id]);

  const api = useMemo<NotificationContextType>(() => ({
    notifications,
    unreadCount,
    loading,
    markAsRead: async (id: string) => {
      await markNotificationAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true, readAt: new Date().toISOString() } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    },
    markAllAsRead: async () => {
      await markAllNotificationsAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true, readAt: new Date().toISOString() })));
      setUnreadCount(0);
    },
    remove: async (id: string) => {
      await deleteNotification(id);
      setNotifications(prev => prev.filter(n => n.id !== id));
    },
    removeAll: async () => {
      await deleteAllNotifications();
      setNotifications([]);
      setUnreadCount(0);
    },
  }), [notifications, unreadCount, loading]);

  return (
    <NotificationContext.Provider value={api}>
      {children}
    </NotificationContext.Provider>
  );
};
