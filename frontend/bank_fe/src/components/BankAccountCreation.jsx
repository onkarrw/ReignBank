import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import OnboardingSteps from "./OnboardingSteps";
import { useAuth } from "../context/AuthContext";
import { authHeaders, parseApiResponse } from "../utils/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function BankAccountCreation() {
    const navigate = useNavigate();
    const { authToken, logout } = useAuth();
    const [form, setForm] = useState({
        initialDeposit: "0"
    });
    const [message, setMessage] = useState("");
    const [accountOtp, setAccountOtp] = useState("");
    const [accountOtpRequestId, setAccountOtpRequestId] = useState("");
    const [accountOtpMessage, setAccountOtpMessage] = useState("");
    const [isAccountOtpVerified, setIsAccountOtpVerified] = useState(false);

    const handleUnauthorized = () => {
        logout();
        navigate("/login", { replace: true, state: { message: "Session expired. Please sign in again." } });
    };

    useEffect(() => {
        const checkAccount = async () => {
            const res = await fetch(`${BASE_URL}/api/v1/accounts/me`, {
                method: "GET",
                headers: authHeaders(authToken),
                credentials: "include"
            });
            if (res.status === 401) {
                handleUnauthorized();
                return;
            }
            const data = await parseApiResponse(res);
            if (res.ok && data.status === "ACTIVE") {
                navigate("/dashboard", { replace: true });
            }
        };
        checkAccount();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const submit = async () => {
        if (form.initialDeposit === "" || Number(form.initialDeposit) < 0) {
            setMessage("Initial deposit cannot be negative");
            return;
        }
        if (!isAccountOtpVerified) {
            setMessage("Verify email OTP before creating account");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/accounts/create`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include",
            body: JSON.stringify({
                initialDeposit: Number(form.initialDeposit)
            })
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        setMessage(data.message || data.code);
        if (res.ok && data.status === "SUCCESS") {
            setIsAccountOtpVerified(false);
            setAccountOtp("");
            setAccountOtpRequestId("");
        }
    };

    const requestAccountCreationOtp = async () => {
        setAccountOtpMessage("");
        setIsAccountOtpVerified(false);
        const res = await fetch(`${BASE_URL}/api/v1/accounts/create-otp/request`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        setAccountOtpMessage(data.message || data.code || data.error);
        if (res.ok && data.status === "OTP_SENT") {
            setAccountOtpRequestId(data.otpRequestId);
        }
    };

    const verifyAccountCreationOtp = async () => {
        setAccountOtpMessage("");
        if (!/^\d{6}$/.test(accountOtp)) {
            setAccountOtpMessage("Enter a valid 6-digit OTP");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/accounts/create-otp/verify`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include",
            body: JSON.stringify({ otp: accountOtp, otpRequestId: accountOtpRequestId })
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        setAccountOtpMessage(data.message || data.code || data.error);
        if (res.ok && data.status === "SUCCESS") {
            setIsAccountOtpVerified(true);
        }
    };

    return (
        <section className="page-card">
            <OnboardingSteps currentPath="/account" />
            <h2>Step 5 — Open your account</h2>
            <p className="page-lead">Confirm your email again, then submit your account request.</p>

            <div className="form-section">
                <h3>1. Initial deposit</h3>
                <input
                    name="initialDeposit"
                    placeholder="Initial deposit amount"
                    value={form.initialDeposit}
                    onChange={handleChange}
                />
            </div>

            <div className="form-section">
                <h3>2. Email verification</h3>
                <button type="button" onClick={requestAccountCreationOtp}>Send OTP to my email</button>
                <div className="inline-form">
                    <input
                        placeholder="6-digit OTP"
                        value={accountOtp}
                        onChange={(e) => setAccountOtp(e.target.value)}
                        maxLength={6}
                    />
                    <button type="button" onClick={verifyAccountCreationOtp}>Verify</button>
                </div>
                {accountOtpMessage && <p className="form-message">{accountOtpMessage}</p>}
                {isAccountOtpVerified && <p className="form-success">Email verified — you can create your account.</p>}
            </div>

            <div className="form-actions">
                <button type="button" onClick={submit} disabled={!isAccountOtpVerified}>Create account</button>
                <Link to="/dashboard" className="text-link">Back to dashboard</Link>
            </div>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
