import { Link } from "react-router-dom";

const STEPS = [
    { path: "/onboarding", label: "Your details" },
    { path: "/onboarding/otp", label: "Verify email" },
    { path: "/onboarding/password", label: "Set password" },
    { path: "/login", label: "Sign in" },
    { path: "/account", label: "Open account" }
];

export default function OnboardingSteps({ currentPath }) {
    const currentIndex = STEPS.findIndex((step) => step.path === currentPath);

    return (
        <nav className="onboarding-steps" aria-label="Onboarding progress">
            {STEPS.map((step, index) => {
                const done = index < currentIndex;
                const active = index === currentIndex;
                const reachable = index <= currentIndex;
                return (
                    <div
                        key={step.path}
                        className={`onboarding-step ${done ? "done" : ""} ${active ? "active" : ""}`}
                    >
                        <span className="step-number">{index + 1}</span>
                        {reachable && !active ? (
                            <Link to={step.path} className="step-label">{step.label}</Link>
                        ) : (
                            <span className="step-label">{step.label}</span>
                        )}
                    </div>
                );
            })}
        </nav>
    );
}
