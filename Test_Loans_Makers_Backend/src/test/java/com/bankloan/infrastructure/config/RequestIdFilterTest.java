package com.bankloan.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void shouldGenerateRequestIdWhenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String requestId = exchange.getResponse().getHeaders().getFirst("X-Request-Id");
        assertThat(requestId).isNotNull().hasSize(8);
    }

    @Test
    void shouldUseProvidedRequestId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Request-Id", "custom-id").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String requestId = exchange.getResponse().getHeaders().getFirst("X-Request-Id");
        assertThat(requestId).isEqualTo("custom-id");
    }

    @Test
    void shouldGenerateIdWhenBlank() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Request-Id", "  ").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String requestId = exchange.getResponse().getHeaders().getFirst("X-Request-Id");
        assertThat(requestId).isNotNull().hasSize(8);
    }
}
