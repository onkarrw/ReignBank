import { useState } from "react";
import { useNavigate } from "react-router-dom";
import OnboardingSteps from "./OnboardingSteps";
import { setOnboardingEmail, setOtpRequestId } from "../utils/onboardingStorage";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function CustomerForm() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
        phone: ""
    });
    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const validate = () => {
        if (!form.firstName.trim() || !form.lastName.trim()) {
            return "First and last name are required";
        }
        if (!form.email.includes("@")) {
            return "Enter a valid email";
        }
        if (!/^\d{10,15}$/.test(form.phone)) {
            return "Phone must be 10-15 digits";
        }
        return null;
    };

    const submit = async () => {
        setMessage("");
        const error = validate();
        if (error) {
            setMessage(error);
            return;
        }

        const res = await fetch(`${BASE_URL}/api/v1/customers/onboard`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(form)
        });

        const data = await res.json();
        setMessage(data.message || data.code);

        if (res.ok && data.status === "OTP_SENT") {
            setOnboardingEmail(form.email);
            setOtpRequestId(data.otpRequestId);
            navigate("/onboarding/otp");
        }
        if (res.ok && data.status === "ACCOUNT_CREATION_PENDING") {
            setOnboardingEmail(form.email);
            navigate("/login");
        }
    };

    return (
        <section className="page-card">
            <OnboardingSteps currentPath="/onboarding" />
            <h2>Step 1 — Your details</h2>
            <p className="page-lead">Tell us who you are. We will send a verification code to your email.</p>

            <div className="form-grid">
                <input name="firstName" placeholder="First name" onChange={handleChange} />
                <input name="lastName" placeholder="Last name" onChange={handleChange} />
                <input name="email" type="email" placeholder="Email" onChange={handleChange} />
                <input name="phone" placeholder="Phone (10-15 digits)" onChange={handleChange} />
            </div>

            <div className="form-actions">
                <button type="button" onClick={submit}>Continue</button>
            </div>
            {message && <p className="form-message">{message}</p>}
        </section>
    );
}
