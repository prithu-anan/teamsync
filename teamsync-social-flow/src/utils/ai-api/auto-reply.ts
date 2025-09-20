import axios from 'axios';
import { AI_API_BASE_URL, getAuthHeaders } from './config';

export const channel_auto_reply = async ({ channel_id, sender_id }) => {
    try {
        const res = await axios.post(`${AI_API_BASE_URL}/channels/auto-reply`, { channel_id, sender_id }, {
            headers: getAuthHeaders()
        });

        if (res.status === 200) {
            return res.data;
        }

    } catch (err) {
        if (err.response) {
            return { error: err.response.data || "Invalid credentials" };
        } else if (err.request) {
            return { error: "No response from server. Check your connection." };
        } else {
            return { error: "An unexpected error occurred." };
        }
    }
}; 

export const direct_auto_reply = async ({ recipient_id, sender_id }) => {
    try {
        const res = await axios.post(`${AI_API_BASE_URL}/channels/auto-reply`, { recipient_id, sender_id }, {
            headers: getAuthHeaders()
        });

        if (res.status === 200) {
            return res.data;
        }

    } catch (err) {
        if (err.response) {
            return { error: err.response.data || "Invalid credentials" };
        } else if (err.request) {
            return { error: "No response from server. Check your connection." };
        } else {
            return { error: "An unexpected error occurred." };
        }
    }
};

export const thread_auto_reply = async ({ recipient_id, sender_id, parent_thread_id }) => {
    try {
        const res = await axios.post(`${AI_API_BASE_URL}/channels/auto-reply`, { recipient_id, sender_id, parent_thread_id }, {
            headers: getAuthHeaders()
        });

        if (res.status === 200) {
            return res.data;
        }

    } catch (err) {
        if (err.response) {
            return { error: err.response.data || "Invalid credentials" };
        } else if (err.request) {
            return { error: "No response from server. Check your connection." };
        } else {
            return { error: "An unexpected error occurred." };
        }
    }
}; 