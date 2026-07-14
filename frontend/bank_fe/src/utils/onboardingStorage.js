const EMAIL_KEY = "onboardingEmail";
const OTP_REQUEST_KEY = "otpRequestId";

export function getOnboardingEmail() {
    return sessionStorage.getItem(EMAIL_KEY) || "";
}

export function setOnboardingEmail(email) {
    sessionStorage.setItem(EMAIL_KEY, email);
}

export function getOtpRequestId() {
    return sessionStorage.getItem(OTP_REQUEST_KEY) || "";
}

export function setOtpRequestId(id) {
    sessionStorage.setItem(OTP_REQUEST_KEY, id);
}

export function clearOnboardingData() {
    sessionStorage.removeItem(EMAIL_KEY);
    sessionStorage.removeItem(OTP_REQUEST_KEY);
}
