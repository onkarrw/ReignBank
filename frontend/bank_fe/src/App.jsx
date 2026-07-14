import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import GuestRoute from "./components/GuestRoute";
import ProtectedRoute from "./components/ProtectedRoute";
import Menu from "./components/Menu";
import LoginForm from "./components/LoginForm";
import CustomerForm from "./components/CustomerForm";
import OtpForm from "./components/OtpForm";
import SetPasswordForm from "./components/SetPasswordForm";

export default function App() {
    return (
        <Layout>
            <Routes>
                <Route path="/" element={<GuestRoute><Menu /></GuestRoute>} />
                <Route path="/login" element={<GuestRoute><LoginForm /></GuestRoute>} />
                <Route path="/onboarding" element={<GuestRoute><CustomerForm /></GuestRoute>} />
                <Route path="/onboarding/otp" element={<GuestRoute><OtpForm /></GuestRoute>} />
                <Route path="/onboarding/password" element={<GuestRoute><SetPasswordForm /></GuestRoute>} />
                <Route path="/dashboard" element={<ProtectedRoute customerOnly><div>todo dashboard</div></ProtectedRoute>} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Layout>
    );
}
