import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import GuestRoute from "./components/GuestRoute";
import Menu from "./components/Menu";
import CustomerForm from "./components/CustomerForm";
import OtpForm from "./components/OtpForm";
import SetPasswordForm from "./components/SetPasswordForm";
import LoginForm from "./components/LoginForm";
import CustomerDashboard from "./components/CustomerDashboard";
import BankAccountCreation from "./components/BankAccountCreation";
import AccountBalance from "./components/AccountBalance";
import SendMoney from "./components/SendMoney";
import EmployeeDashboard from "./components/EmployeeDashboard";

export default function App() {
    return (
        <Layout>
            <Routes>
                <Route path="/" element={<GuestRoute><Menu /></GuestRoute>} />
                <Route path="/login" element={<GuestRoute><LoginForm /></GuestRoute>} />
                <Route path="/onboarding" element={<GuestRoute><CustomerForm /></GuestRoute>} />
                <Route path="/onboarding/otp" element={<GuestRoute><OtpForm /></GuestRoute>} />
                <Route path="/onboarding/password" element={<GuestRoute><SetPasswordForm /></GuestRoute>} />
                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute customerOnly>
                            <CustomerDashboard />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/account"
                    element={
                        <ProtectedRoute customerOnly>
                            <BankAccountCreation />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/balance"
                    element={
                        <ProtectedRoute customerOnly>
                            <AccountBalance />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/send-money"
                    element={
                        <ProtectedRoute customerOnly>
                            <SendMoney />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute staffOnly>
                            <EmployeeDashboard />
                        </ProtectedRoute>
                    }
                />
                <Route path="/employee" element={<Navigate to="/admin" replace />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Layout>
    );
}
