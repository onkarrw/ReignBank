import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAuthenticated } from "../utils/api";

export default function ProtectedRoute({ children, staffOnly = false, customerOnly = false }) {
    const { isStaff, dashboardPath } = useAuth();
    const location = useLocation();

    if (!isAuthenticated()) {
        return <Navigate to="/login" replace state={{ from: location.pathname }} />;
    }
    if (staffOnly && !isStaff) {
        return <Navigate to={dashboardPath} replace />;
    }
    if (customerOnly && isStaff) {
        return <Navigate to={dashboardPath} replace />;
    }
    return children;
}
