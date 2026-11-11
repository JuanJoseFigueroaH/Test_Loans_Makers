package com.bankloan.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, RateEntry> attempts = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (!LOGIN_PATH.equals(path) || !"POST".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange);
        RateEntry entry = attempts.compute(clientIp, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RateEntry();
            }
            existing.increment();
            return existing;
        });

        if (entry.getCount() > MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return writeRateLimitResponse(exchange);
        }

        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> writeRateLimitResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "type", "https://api.bankloan.com/errors/authentication/rate-limited",
                "title", "Too Many Requests",
                "status", 429,
                "detail", "Too many login attempts. Please wait before trying again.",
                "code", "RATE_LIMITED"
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] fallback = """
                    {"status":429,"detail":"Too many login attempts"}""".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(fallback);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    private static class RateEntry {
        private final AtomicInteger count = new AtomicInteger(1);
        private final Instant windowStart = Instant.now();

        void increment() {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() - windowStart.getEpochSecond() > WINDOW_SECONDS;
        }
    }
}
