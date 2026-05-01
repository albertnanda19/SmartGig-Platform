package com.smartgig.gateway.config;

import com.smartgig.common.constants.AppConstants;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = Optional.ofNullable(remoteAddress)
                    .map(InetSocketAddress::getAddress)
                    .map(a -> a.getHostAddress())
                    .orElse("unknown");
            return Mono.just(ip);
        };
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst(AppConstants.USER_ID_HEADER);
            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = Optional.ofNullable(remoteAddress)
                    .map(InetSocketAddress::getAddress)
                    .map(a -> a.getHostAddress())
                    .orElse("unknown");
            return Mono.just(ip);
        };
    }
}

