import axios from 'axios';
import { API_BASE_URL, getAuthHeaders } from './api-config';
import type { Notification, NotificationCount } from '@/types/notifications';

const BASE = `${API_BASE_URL}/notifications`;

export const getNotifications = async () => {
  try {
    const res = await axios.get<Notification[]>(`${BASE}`, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to fetch notifications' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const getUnreadNotifications = async () => {
  try {
    const res = await axios.get<Notification[]>(`${BASE}/unread`, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to fetch unread notifications' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const getUnreadCount = async () => {
  try {
    const res = await axios.get<NotificationCount>(`${BASE}/count`, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to fetch unread count' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const markNotificationAsRead = async (notificationId: string) => {
  try {
    const res = await axios.put<void>(`${BASE}/${notificationId}/read`, {}, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to mark as read' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const markAllNotificationsAsRead = async () => {
  try {
    const res = await axios.put<void>(`${BASE}/read-all`, {}, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to mark all as read' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const deleteNotification = async (notificationId: string) => {
  try {
    const res = await axios.delete<void>(`${BASE}/${notificationId}`, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to delete notification' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};

export const deleteAllNotifications = async () => {
  try {
    const res = await axios.delete<void>(`${BASE}`, { headers: getAuthHeaders() });
    return res.data;
  } catch (err: any) {
    if (err.response) return { error: err.response.data || 'Failed to delete all notifications' };
    if (err.request) return { error: 'No response from server. Check your connection.' };
    return { error: 'An unexpected error occurred.' };
  }
};
