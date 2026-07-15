import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import OnboardingSteps from "./OnboardingSteps";
import { getOnboardingEmail } from "../utils/onboardingStorage";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function SetPasswordForm() {
    const navigate = useNavigate();
    const email = getOnboardingEmail();
    const [form, setForm] = useState({
        username: "",
        password: ""
    });
    const [message, setMessage] = useState("");

    if (!email) {
        return <Navigate to="/onboarding" replace />;
    }

    const submit = async () => {
        setMessage("");
        if (!form.username.trim()) {
            setMessage("Username is required");
            return;
        }
        if (form.password.length < 8) {
            setMessage("Password must be at least 8 characters");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/auth/set-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, username: form.username, password: form.password })
        });
        const data = await res.json();
        setMessage(data.message || data.code);
        if (res.ok && data.status === "SUCCESS") {
            sessionStorage.setItem("username", form.username);
            navigate("/login");
        }
    };

    return (
        <section className="page-card">
            <OnboardingSteps currentPath="/onboarding/password" />
            <h2>Step 3 — Create your login</h2>
            <p className="page-lead">Email verified for <strong>{email}</strong>. Pick a username and password to sign in later.</p>

            <div className="form-grid">
                <input
                    placeholder="Username"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                />
                <input
                    type="password"
                    placeholder="Password (min 8 characters)"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                />
            </div>

            <div className="form-actions">
                <button type="button" onClick={submit}>Save and continue</button>
            </div>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
