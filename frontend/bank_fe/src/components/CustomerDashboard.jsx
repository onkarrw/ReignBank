import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authHeaders, parseApiResponse } from "../utils/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function CustomerDashboard() {
    const navigate = useNavigate();
    const { authToken, username, logout } = useAuth();
    const [summary, setSummary] = useState(null);
    const [message, setMessage] = useState("");

    const loadSummary = async () => {
        const res = await fetch(`${BASE_URL}/api/v1/accounts/me`, {
            method: "GET",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        if (res.status === 401) {
            logout();
            navigate("/login", { replace: true, state: { message: "Session expired. Please sign in again." } });
            return;
        }
        const data = await parseApiResponse(res);
        if (res.ok) {
            setSummary(data);
            setMessage("");
        } else {
            setMessage(data.message || data.code || data.error);
        }
    };

    useEffect(() => {
        loadSummary();
    }, []);

    const hasActiveAccount = summary?.status === "ACTIVE";

    return (
        <section className="page-card">
            <h2>Customer dashboard</h2>
            <p className="page-lead">Welcome back, <strong>{username}</strong>.</p>

            {hasActiveAccount && (
                <div className="balance-banner">
                    <p className="balance-label">Available balance</p>
                    <p className="balance-value">{summary.currency} {summary.balance}</p>
                    <p className="form-hint">Account {summary.accountNumber} · {summary.accountType}{summary.ifscCode ? ` · IFSC ${summary.ifscCode}` : ""}</p>
                </div>
            )}

            {summary?.status === "PENDING_APPROVAL" && (
                <div className="info-box">
                    <p>{summary.message}</p>
                </div>
            )}

            <div className="action-grid">
                {summary?.status === "NO_ACCOUNT" && (
                    <Link to="/account" className="action-card">
                        <h3>Open account</h3>
                        <p>Verify your email and submit a new account request</p>
                    </Link>
                )}
                {hasActiveAccount && (
                    <>
                        <Link to="/balance" className="action-card">
                            <h3>See balance</h3>
                            <p>View account number, type, and current balance</p>
                        </Link>
                        <Link to="/send-money" className="action-card">
                            <h3>Send money</h3>
                            <p>Transfer funds to another account</p>
                        </Link>
                    </>
                )}
            </div>

            <div className="form-actions">
                <button type="button" onClick={loadSummary}>Refresh</button>
            </div>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
