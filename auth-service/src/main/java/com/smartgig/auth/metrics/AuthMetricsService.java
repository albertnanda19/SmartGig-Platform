package com.smartgig.auth.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetricsService {
    private final MeterRegistry meterRegistry;

    public AuthMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSuccessfulLogin() {
        meterRegistry.counter("smartgig.auth.login.success").increment();
    }

    public void recordFailedLogin(String reason) {
        meterRegistry.counter("smartgig.auth.login.failed", "reason", reason == null ? "unknown" : reason).increment();
    }

    public void recordAccountLocked() {
        meterRegistry.counter("smartgig.auth.account.locked").increment();
    }
}

