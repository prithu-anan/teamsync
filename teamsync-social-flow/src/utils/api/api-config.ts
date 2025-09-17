export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const getAuthHeaders = () => {
    const token = localStorage.getItem("teamsync_jwt");
    return {
        Authorization: `Bearer ${token}`,
    };
}; 