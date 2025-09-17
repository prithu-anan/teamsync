export const AI_API_BASE_URL = `${import.meta.env.VITE_AI_BACKEND_URL || 'http://localhost:8000'}/api`;

export const getAuthHeaders = () => {
    const token = localStorage.getItem("teamsync_jwt");
    return {
        Authorization: `Bearer ${token}`,
    };
}; 