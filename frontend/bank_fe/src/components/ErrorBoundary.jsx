import { Component } from "react";

export default class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { error: null };
    }

    static getDerivedStateFromError(error) {
        return { error };
    }

    render() {
        if (this.state.error) {
            return (
                <section className="page-card">
                    <h2>Something went wrong</h2>
                    <p className="form-message">{this.state.error.message}</p>
                    <button type="button" onClick={() => window.location.assign("/")}>Reload app</button>
                </section>
            );
        }
        return this.props.children;
    }
}
