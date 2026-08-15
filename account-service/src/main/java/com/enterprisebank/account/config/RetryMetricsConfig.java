package com.enterprisebank.account.config;

import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryMetricsConfig {

    public RetryMetricsConfig(
            RetryRegistry retryRegistry,
            MeterRegistry meterRegistry
    ) {

        TaggedRetryMetrics
                .ofRetryRegistry(retryRegistry)
                .bindTo(meterRegistry);
    }
}