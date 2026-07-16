import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import OnboardingSteps from "./OnboardingSteps";
import { getOnboardingEmail, getOtpRequestId } from "../utils/onboardingStorage";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function OtpForm() {
    const navigate = useNavigate();
    const email = getOnboardingEmail();
    const otpRequestId = getOtpRequestId();
    const [otp, setOtp] = useState("");
    const [message, setMessage] = useState("");

    if (!email || !otpRequestId) {
        return <Navigate to="/onboarding" replace />;
    }

    const verifyOtp = async () => {
        setMessage("");

        if (!/^\d{6}$/.test(otp)) {
            setMessage("Enter a 6-digit OTP");
            return;
        }

        const res = await fetch(`${BASE_URL}/api/v1/customers/verify-otp`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ email, otp, otpRequestId })
        });

        const data = await res.json();
        setMessage(data.message || data.code);

        if (res.ok && data.status === "SUCCESS") {
            navigate("/onboarding/password");
        }
    };

    return (
        <section className="page-card">
            <OnboardingSteps currentPath="/onboarding/otp" />
            <h2>Step 2 — Verify your email</h2>
            <p className="page-lead">We sent a 6-digit code to <strong>{email}</strong>. Enter it below to continue.</p>

            <div className="form-grid">
                <input
                    placeholder="6-digit OTP"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    maxLength={6}
                />
            </div>

            <div className="form-actions">
                <button type="button" onClick={verifyOtp}>Verify code</button>
            </div>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
