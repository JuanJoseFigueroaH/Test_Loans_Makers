package com.bankloan.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(new ObjectMapper());
    }

    @Test
    void shouldPassThroughNonLoginRequests() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldPassThroughGetLoginRequests() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldAllowLoginWithinLimit() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldUseXForwardedForHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldHandleNullRemoteAddress() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldBlockAfterExceedingRateLimit() {
        WebFilterChain chain = ex -> Mono.empty();

        for (int i = 0; i < 11; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                    .header("X-Forwarded-For", "10.99.99.99").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        }

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .header("X-Forwarded-For", "10.99.99.99").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
