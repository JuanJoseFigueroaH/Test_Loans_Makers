package com.bankloan.infrastructure.config;

import com.bankloan.domain.port.out.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Test
    void corsConfigurationSource_shouldBeConfigured() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SecurityConfig config = new SecurityConfig(tokenProvider, objectMapper);

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertThat(source).isNotNull();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        CorsConfiguration corsConfig = source.getCorsConfiguration(exchange);
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("http://localhost:4200");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }
}
