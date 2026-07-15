import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [authToken, setAuthToken] = useState(sessionStorage.getItem("authToken") || "");
    const [username, setUsername] = useState(sessionStorage.getItem("username") || "");
    const [role, setRole] = useState(sessionStorage.getItem("role") || "");

    const login = (token, loginUsername, loginRole) => {
        sessionStorage.setItem("authToken", token);
        sessionStorage.setItem("username", loginUsername);
        sessionStorage.setItem("role", loginRole);
        setAuthToken(token);
        setUsername(loginUsername);
        setRole(loginRole);
    };

    const logout = () => {
        sessionStorage.removeItem("authToken");
        sessionStorage.removeItem("username");
        sessionStorage.removeItem("role");
        setAuthToken("");
        setUsername("");
        setRole("");
    };

    const value = useMemo(() => {
        const isStaff = role === "EMPLOYEE" || role === "ADMIN";
        return {
            authToken,
            username,
            role,
            login,
            logout,
            isStaff,
            dashboardPath: isStaff ? "/admin" : "/dashboard"
        };
    }, [authToken, username, role]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error("useAuth must be used within AuthProvider");
    }
    return ctx;
}
