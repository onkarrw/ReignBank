import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAuthenticated } from "../utils/api";

export default function GuestRoute({ children }) {
    const { dashboardPath } = useAuth();
    if (isAuthenticated()) {
        return <Navigate to={dashboardPath} replace />;
    }
    return children;
}
