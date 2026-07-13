import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAuthenticated } from "../utils/api";
import { clearOnboardingData } from "../utils/onboardingStorage";

export default function Layout({ children }) {
    const { username, role, logout, isStaff } = useAuth();
    const navigate = useNavigate();
    const loggedIn = isAuthenticated();

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    return (
        <div className="app-shell">
            <header className="app-header">
                <Link to="/" className="brand">Reign Bank</Link>
                <nav className="main-nav">
                    {!loggedIn && (
                        <>
                            <NavLink to="/" end>Home</NavLink>
                            <NavLink to="/onboarding">Register</NavLink>
                            <NavLink to="/login" onClick={clearOnboardingData}>Login</NavLink>
                        </>
                    )}
                    {loggedIn && !isStaff && <NavLink to="/dashboard">Dashboard</NavLink>}
                    {loggedIn && isStaff && <NavLink to="/admin">Admin</NavLink>}
                </nav>
                {loggedIn && (
                    <div className="user-bar">
                        <span>{username} ({role})</span>
                        <button type="button" onClick={handleLogout}>Logout</button>
                    </div>
                )}
            </header>
            <main className="app-main">{children}</main>
        </div>
    );
}
