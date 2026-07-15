import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authHeaders, parseApiResponse } from "../utils/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function SendMoney() {
    const navigate = useNavigate();
    const { authToken, logout } = useAuth();
    const [form, setForm] = useState({
        toAccountNumber: "",
        amount: "",
        description: ""
    });
    const [message, setMessage] = useState("");
    const [success, setSuccess] = useState("");
    const [transferOtp, setTransferOtp] = useState("");
    const [transferOtpRequestId, setTransferOtpRequestId] = useState("");
    const [transferOtpMessage, setTransferOtpMessage] = useState("");
    const [isTransferOtpVerified, setIsTransferOtpVerified] = useState(false);

    const handleUnauthorized = () => {
        logout();
        navigate("/login", { replace: true, state: { message: "Session expired. Please sign in again." } });
    };

    const resetTransferOtp = () => {
        setIsTransferOtpVerified(false);
        setTransferOtp("");
        setTransferOtpRequestId("");
        setTransferOtpMessage("");
    };

    const updateForm = (patch) => {
        setForm((current) => ({ ...current, ...patch }));
        resetTransferOtp();
    };

    const requestTransferOtp = async () => {
        setTransferOtpMessage("");
        resetTransferOtp();
        const res = await fetch(`${BASE_URL}/api/v1/accounts/transfer-otp/request`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        setTransferOtpMessage(data.message || data.code || data.error);
        if (res.ok && data.status === "OTP_SENT") {
            setTransferOtpRequestId(data.otpRequestId);
        }
    };

    const verifyTransferOtp = async () => {
        setTransferOtpMessage("");
        if (!/^\d{6}$/.test(transferOtp)) {
            setTransferOtpMessage("Enter a valid 6-digit OTP");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/accounts/transfer-otp/verify`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include",
            body: JSON.stringify({ otp: transferOtp, otpRequestId: transferOtpRequestId })
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        setTransferOtpMessage(data.message || data.code || data.error);
        if (res.ok && data.status === "SUCCESS") {
            setIsTransferOtpVerified(true);
        }
    };

    const submit = async () => {
        setMessage("");
        setSuccess("");
        if (!form.toAccountNumber.trim()) {
            setMessage("Recipient account number is required");
            return;
        }
        if (form.amount === "" || Number(form.amount) <= 0) {
            setMessage("Enter a valid amount greater than zero");
            return;
        }
        if (!isTransferOtpVerified) {
            setMessage("Verify email OTP before sending money");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/accounts/transfer`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include",
            body: JSON.stringify({
                toAccountNumber: form.toAccountNumber.trim(),
                amount: Number(form.amount),
                description: form.description.trim()
            })
        });
        if (res.status === 401) {
            handleUnauthorized();
            return;
        }
        const data = await parseApiResponse(res);
        if (res.ok && data.status === "SUCCESS") {
            setSuccess(`Transfer successful. Reference: ${data.referenceId}. New balance: INR ${data.newBalance}`);
            setForm({ toAccountNumber: "", amount: "", description: "" });
            resetTransferOtp();
        } else {
            setMessage(data.message || data.code || data.error);
            if (data.code === "SEND_MONEY_OTP_NOT_VERIFIED") {
                resetTransferOtp();
            }
        }
    };

    return (
        <section className="page-card">
            <h2>Send money</h2>
            <p className="page-lead">Transfer funds to another bank account.</p>

            <div className="form-grid">
                <input
                    placeholder="Recipient account number"
                    value={form.toAccountNumber}
                    onChange={(e) => updateForm({ toAccountNumber: e.target.value })}
                />
                <input
                    placeholder="Amount"
                    value={form.amount}
                    onChange={(e) => updateForm({ amount: e.target.value })}
                />
                <input
                    placeholder="Description (optional)"
                    value={form.description}
                    onChange={(e) => updateForm({ description: e.target.value })}
                />
            </div>

            <div className="form-section">
                <h3>Confirm with email OTP</h3>
                <button type="button" onClick={requestTransferOtp}>Send OTP to my email</button>
                <div className="inline-form">
                    <input
                        placeholder="6-digit OTP"
                        value={transferOtp}
                        onChange={(e) => {
                            setTransferOtp(e.target.value);
                            setIsTransferOtpVerified(false);
                        }}
                        maxLength={6}
                    />
                    <button type="button" onClick={verifyTransferOtp}>Verify</button>
                </div>
                {transferOtpMessage && <p className="form-message">{transferOtpMessage}</p>}
                {isTransferOtpVerified && <p className="form-success">Email verified — you can send money.</p>}
            </div>

            <div className="form-actions">
                <button type="button" onClick={submit} disabled={!isTransferOtpVerified}>Send</button>
                <Link to="/dashboard" className="text-link">Back to dashboard</Link>
            </div>
            {success && <p className="form-success">{success}</p>}
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
