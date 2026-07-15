import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authHeaders, parseApiResponse } from "../utils/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function AccountBalance() {
    const navigate = useNavigate();
    const { authToken, logout } = useAuth();
    const [summary, setSummary] = useState(null);
    const [message, setMessage] = useState("");

    useEffect(() => {
        const load = async () => {
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
            if (res.ok && data.status === "ACTIVE") {
                setSummary(data);
            } else if (res.ok && data.status !== "ACTIVE") {
                navigate("/dashboard", { replace: true });
            } else {
                setMessage(data.message || data.code || data.error);
            }
        };
        load();
    }, []);

    if (!summary) {
        return (
            <section className="page-card">
                <h2>Account balance</h2>
                {message && <p className="form-message">{message}</p>}
            </section>
        );
    }

    return (
        <section className="page-card">
            <h2>Account balance</h2>
            <div className="balance-banner">
                <p className="balance-label">Available balance</p>
                <p className="balance-value">{summary.currency} {summary.balance}</p>
            </div>
            <div className="detail-list">
                <p><strong>Account number</strong> {summary.accountNumber}</p>
                <p><strong>Account type</strong> {summary.accountType}</p>
                <p><strong>Status</strong> {summary.status}</p>
            </div>
            <div className="form-actions">
                <Link to="/send-money" className="text-link">Send money</Link>
                <Link to="/dashboard" className="text-link">Back to dashboard</Link>
            </div>
        </section>
    );
}
