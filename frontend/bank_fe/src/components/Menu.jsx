import { Link } from "react-router-dom";

export default function Menu() {
    return (
        <section className="page-card">
            <h1>Welcome to Reign Bank</h1>
            <p className="page-lead">Manage accounts, onboard new customers, or review pending requests.</p>
            <div className="action-grid">
                <Link to="/login" className="action-card">
                    <h3>Login</h3>
                    <p>Existing customers and staff</p>
                </Link>
                <Link to="/onboarding" className="action-card">
                    <h3>Register</h3>
                    <p>New customer onboarding — takes about 5 minutes</p>
                </Link>
            </div>
            <div className="info-box">
                <h4>New here?</h4>
                <ol>
                    <li>Fill in your personal details</li>
                    <li>Verify your email with a one-time code</li>
                    <li>Choose a username and password</li>
                    <li>Sign in and open your account</li>
                </ol>
            </div>
        </section>
    );
}
