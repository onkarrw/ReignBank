import { useState } from "react";
import OnboardingSteps from "./OnboardingSteps";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

import { clearOnboardingData, getOnboardingEmail } from "../utils/onboardingStorage";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

function resolveDashboardPath(role) {
    return role === "EMPLOYEE" || role === "ADMIN" ? "/admin" : "/dashboard";
}

export default function LoginForm() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    const fromOnboarding = !!getOnboardingEmail();
    const sessionMessage = location.state?.message || "";
    const [form, setForm] = useState({
        username: sessionStorage.getItem("username") || "",
        password: ""
    });
    const [message, setMessage] = useState(sessionMessage);
    const [onboardingMode, setOnboardingMode] = useState(fromOnboarding);

    const exitRegistration = () => {
        clearOnboardingData();
        sessionStorage.removeItem("username");
        setForm({ username: "", password: "" });
        setOnboardingMode(false);
        setMessage("");
    };

    const submit = async () => {
        setMessage("");
        if (!form.username.trim() || !form.password) {
            setMessage("Username and password are required");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(form)
        });
        const data = await res.json();
        setMessage(data.message || data.code);
        if (res.ok && data.status === "SUCCESS") {
            login(data.token, form.username, data.role);
            navigate(resolveDashboardPath(data.role), { replace: true });
        }
    };

    return (
        <section className="page-card">
            {onboardingMode && <OnboardingSteps currentPath="/login" />}
            <h2>{onboardingMode ? "Step 4 — Sign in" : "Login"}</h2>
            <p className="page-lead">
                {onboardingMode
                    ? "Use the username and password you created during registration."
                    : "Sign in with your username and password."}
            </p>
            {onboardingMode && (
                <p className="form-hint">
                    Staff or existing user?{" "}
                    <button type="button" className="text-link link-button" onClick={exitRegistration}>
                        Exit registration and sign in separately
                    </button>
                </p>
            )}

            <div className="form-grid">
                <input
                    placeholder="Username"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                />
            </div>

            <div className="form-actions">
                <button type="button" onClick={submit}>Sign in</button>
            </div>
            <p className="form-hint">
                New customer? <Link to="/onboarding">Start registration</Link>
            </p>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
