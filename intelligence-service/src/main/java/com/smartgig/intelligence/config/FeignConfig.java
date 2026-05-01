package com.smartgig.intelligence.config;

import com.smartgig.common.constants.AppConstants;
import feign.Logger;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String traceId = MDC.get("traceId");
            if (traceId != null && !traceId.isBlank()) {
                requestTemplate.header(AppConstants.TRACE_ID_HEADER, traceId);
            }
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}

