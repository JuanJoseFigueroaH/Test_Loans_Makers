package com.bankloan.infrastructure.config;

import com.bankloan.domain.port.out.TokenProvider;
import com.bankloan.infrastructure.adapter.out.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigFilterTest {

    private TokenProvider tokenProvider;
    private ObjectMapper objectMapper;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        objectMapper = new ObjectMapper();
        securityConfig = new SecurityConfig(tokenProvider, objectMapper);
    }

    @Test
    void jwtFilter_shouldPassPublicPaths() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldPassOptionsRequests() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.options("/api/v1/loans").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldPassWithoutAuthHeader() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldPassWithNonBearerAuth() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldAuthenticateWithValidToken() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tokenProvider.validateToken("valid-token"))
                .thenReturn(new TokenProvider.TokenClaims(userId, "test@test.com", "ROLE_USER"));

        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
        verify(tokenProvider).validateToken("valid-token");
    }

    @Test
    void jwtFilter_shouldRejectInvalidToken() throws Exception {
        when(tokenProvider.validateToken("bad-token"))
                .thenThrow(new JwtTokenProvider.InvalidTokenException("Invalid token"));

        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void jwtFilter_shouldHandleUnexpectedException() throws Exception {
        when(tokenProvider.validateToken("crash-token"))
                .thenThrow(new RuntimeException("Unexpected failure"));

        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer crash-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void jwtFilter_shouldPassSwaggerPaths() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldPassApiDocsPaths() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api-docs/openapi.json").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void jwtFilter_shouldPassActuatorHealth() throws Exception {
        WebFilter jwtFilter = getJwtFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void writeErrorResponse_shouldReturnJsonBody() throws Exception {
        Method method = SecurityConfig.class.getDeclaredMethod("writeErrorResponse",
                org.springframework.web.server.ServerWebExchange.class,
                HttpStatus.class, String.class, String.class);
        method.setAccessible(true);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        @SuppressWarnings("unchecked")
        Mono<Void> result = (Mono<Void>) method.invoke(securityConfig, exchange,
                HttpStatus.FORBIDDEN, "Forbidden", "Insufficient permissions");

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private WebFilter getJwtFilter() throws Exception {
        Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationFilter");
        method.setAccessible(true);
        return (WebFilter) method.invoke(securityConfig);
    }
}
