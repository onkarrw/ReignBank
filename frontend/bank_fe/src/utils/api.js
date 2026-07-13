export function getStoredAuthToken() {
    return (localStorage.getItem("authToken") ?? "").trim();
}
export function isAuthenticated() {
    return getStoredAuthToken().length > 0;
}
export function getAuthToken(authToken) {
    return (authToken ?? getStoredAuthToken()).trim();
}
export function authHeaders(authToken) {
    const headers = { "Content-Type": "application/json" };
    const token = getAuthToken(authToken);
    if (token) headers.Authorization = `Bearer ${token}`;
    return headers;
}
export async function parseApiResponse(res) {
    const text = await res.text();
    if (!text) return {};
    try { return JSON.parse(text); } catch { return { message: text }; }
}
