export const API_BASE_URL = 'http://13.60.242.32:8080';
// export const API_BASE_URL = 'http://localhost:8080';

export const getAuthHeaders = () => {
    const token = localStorage.getItem("teamsync_jwt");
    return {
        Authorization: `Bearer ${token}`,
    };
}; 