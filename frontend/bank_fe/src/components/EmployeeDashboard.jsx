import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { authHeaders, parseApiResponse } from "../utils/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function EmployeeDashboard() {
    const { authToken, role } = useAuth();
    const [requests, setRequests] = useState([]);
    const [message, setMessage] = useState("");
    const [customerQuery, setCustomerQuery] = useState("");
    const [customers, setCustomers] = useState([]);
    const [customerMessage, setCustomerMessage] = useState("");
    const [cashAmounts, setCashAmounts] = useState({});
    const [cashNotes, setCashNotes] = useState({});
    const [audits, setAudits] = useState({});
    const [expandedCustomerId, setExpandedCustomerId] = useState(null);

    const loadRequests = async () => {
        try {
            const res = await fetch(`${BASE_URL}/api/v1/accounts/cash-requests/pending`, {
                method: "GET",
                headers: authHeaders(authToken),
                credentials: "include"
            });
            const data = await parseApiResponse(res);
            if (Array.isArray(data)) {
                setRequests(data);
                setMessage("");
            } else {
                setMessage(data.message || data.code);
            }
        } catch {
            setMessage("Could not load requests. Is the backend running?");
        }
    };

    const actionRequest = async (requestId, action) => {
        const res = await fetch(`${BASE_URL}/api/v1/accounts/cash-requests/${requestId}/${action}`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        const data = await parseApiResponse(res);
        setMessage(data.message || data.code);
        await loadRequests();
    };

    const searchCustomers = async () => {
        setCustomerMessage("");
        if (!customerQuery.trim()) {
            setCustomerMessage("Enter an id, email, phone, or name to search");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/admin/customers/search?q=${encodeURIComponent(customerQuery.trim())}`, {
            method: "GET",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        const data = await parseApiResponse(res);
        if (Array.isArray(data)) {
            setCustomers(data);
            if (data.length === 0) {
                setCustomerMessage("No customers found");
            }
        } else {
            setCustomers([]);
            setCustomerMessage(data.message || data.code || data.error);
        }
    };

    const loadAudit = async (customerId) => {
        const res = await fetch(`${BASE_URL}/api/v1/admin/customers/${customerId}/audit`, {
            method: "GET",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        const data = await parseApiResponse(res);
        if (Array.isArray(data)) {
            setAudits((current) => ({ ...current, [customerId]: data }));
        }
    };

    const toggleAudit = async (customerId) => {
        if (expandedCustomerId === customerId) {
            setExpandedCustomerId(null);
            return;
        }
        setExpandedCustomerId(customerId);
        if (!audits[customerId]) {
            await loadAudit(customerId);
        }
    };

    const updateCustomerStatus = async (customerId, action) => {
        setCustomerMessage("");
        const res = await fetch(`${BASE_URL}/api/v1/admin/customers/${customerId}/${action}`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include"
        });
        const data = await parseApiResponse(res);
        if (res.ok) {
            setCustomers((current) => current.map((item) => (item.id === data.id ? data : item)));
            setCustomerMessage(`Customer ${data.id} is now ${data.status}`);
            await loadAudit(customerId);
        } else {
            setCustomerMessage(data.message || data.code || data.error);
        }
    };

    const adjustCash = async (customerId, action) => {
        setCustomerMessage("");
        const amount = Number(cashAmounts[customerId] || "");
        if (!amount || amount <= 0) {
            setCustomerMessage("Enter a valid amount");
            return;
        }
        const res = await fetch(`${BASE_URL}/api/v1/admin/customers/${customerId}/cash/${action}`, {
            method: "POST",
            headers: authHeaders(authToken),
            credentials: "include",
            body: JSON.stringify({ amount, note: cashNotes[customerId] || "" })
        });
        const data = await parseApiResponse(res);
        if (res.ok) {
            setCustomerMessage(`${data.message} New balance: INR ${data.newBalance}`);
            setCashAmounts((current) => ({ ...current, [customerId]: "" }));
            await loadAudit(customerId);
        } else {
            setCustomerMessage(data.message || data.code || data.error);
        }
    };

    useEffect(() => {
        loadRequests();
    }, []);

    return (
        <section className="page-card">
            <h2>{role === "ADMIN" ? "Admin dashboard" : "Employee dashboard"}</h2>
            <p className="page-lead">Review and action pending cash account requests.</p>

            <div className="form-actions">
                <button type="button" onClick={loadRequests}>Refresh</button>
            </div>

            {message && <p className="form-message">{message}</p>}
            {requests.length === 0 && <p className="form-hint">No pending cash requests.</p>}

            <div className="request-list">
                {requests.map((item) => (
                    <article key={item.requestId} className="request-card">
                        <p><strong>Request #{item.requestId}</strong></p>
                        <p>Customer {item.customerId} · {item.accountType} · Deposit {item.initialDeposit}</p>
                        <div className="form-actions">
                            <button type="button" onClick={() => actionRequest(item.requestId, "approve")}>Approve</button>
                            <button type="button" className="secondary" onClick={() => actionRequest(item.requestId, "deny")}>Deny</button>
                        </div>
                    </article>
                ))}
            </div>

            {role === "ADMIN" && (
                <div className="form-section admin-customer-search">
                    <h3>Customer search</h3>
                    <p className="form-hint">Search by customer id, email, phone, or name. Add/remove cash and view admin audit trail.</p>
                    <div className="inline-form">
                        <input
                            placeholder="Search customers"
                            value={customerQuery}
                            onChange={(e) => setCustomerQuery(e.target.value)}
                        />
                        <button type="button" onClick={searchCustomers}>Search</button>
                    </div>
                    {customerMessage && <p className="form-message">{customerMessage}</p>}
                    <div className="request-list">
                        {customers.map((customer) => (
                            <article key={customer.id} className="request-card">
                                <p><strong>{customer.firstName} {customer.lastName || ""}</strong> · #{customer.id}</p>
                                <p>{customer.email} · {customer.phone || "No phone"}</p>
                                <p>Status: <strong>{customer.status}</strong></p>
                                <div className="form-actions">
                                    {customer.status !== "ACTIVE" && (
                                        <button type="button" onClick={() => updateCustomerStatus(customer.id, "activate")}>Activate</button>
                                    )}
                                    {customer.status === "ACTIVE" && (
                                        <button type="button" className="secondary" onClick={() => updateCustomerStatus(customer.id, "deactivate")}>Deactivate</button>
                                    )}
                                    <button type="button" onClick={() => toggleAudit(customer.id)}>
                                        {expandedCustomerId === customer.id ? "Hide audit" : "View audit"}
                                    </button>
                                </div>
                                <div className="inline-form">
                                    <input
                                        placeholder="Amount"
                                        value={cashAmounts[customer.id] || ""}
                                        onChange={(e) => setCashAmounts((current) => ({ ...current, [customer.id]: e.target.value }))}
                                    />
                                    <input
                                        placeholder="Note (optional)"
                                        value={cashNotes[customer.id] || ""}
                                        onChange={(e) => setCashNotes((current) => ({ ...current, [customer.id]: e.target.value }))}
                                    />
                                    <button type="button" onClick={() => adjustCash(customer.id, "add")}>Add cash</button>
                                    <button type="button" className="secondary" onClick={() => adjustCash(customer.id, "remove")}>Remove cash</button>
                                </div>
                                {expandedCustomerId === customer.id && audits[customer.id] && (
                                    <ul className="audit-list">
                                        {audits[customer.id].length === 0 && <li>No admin actions yet</li>}
                                        {audits[customer.id].map((entry) => (
                                            <li key={entry.id}>
                                                {entry.createdAt} · {entry.adminUsername} · {entry.actionType}
                                                {entry.amount != null && ` · INR ${entry.amount}`}
                                                {entry.balanceAfter != null && ` · balance ${entry.balanceAfter}`}
                                                {entry.note && ` · ${entry.note}`}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </article>
                        ))}
                    </div>
                </div>
            )}
        </section>
    );
}
