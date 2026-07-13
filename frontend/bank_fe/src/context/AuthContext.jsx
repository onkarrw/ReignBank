import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken") || "");
    const [username, setUsername] = useState(localStorage.getItem("username") || "");
    const [role, setRole] = useState(localStorage.getItem("role") || "");

    const login = (token, loginUsername, loginRole) => {
        localStorage.setItem("authToken", token);
        localStorage.setItem("username", loginUsername);
        localStorage.setItem("role", loginRole);
        setAuthToken(token);
        setUsername(loginUsername);
        setRole(loginRole);
    };

    const logout = () => {
        localStorage.removeItem("authToken");
        localStorage.removeItem("username");
        localStorage.removeItem("role");
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
    if (!ctx) throw new Error("useAuth must be used within AuthProvider");
    return ctx;
}
