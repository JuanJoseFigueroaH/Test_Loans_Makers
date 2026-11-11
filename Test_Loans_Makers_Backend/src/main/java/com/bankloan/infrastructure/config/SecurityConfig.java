package com.bankloan.infrastructure.config;

import com.bankloan.infrastructure.adapter.out.security.JwtTokenProvider;
import com.bankloan.domain.port.out.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/actuator/health"
    };

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    public SecurityConfig(TokenProvider tokenProvider, ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) ->
                                writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required"))
                        .accessDeniedHandler((exchange, denied) ->
                                writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "Forbidden", "Insufficient permissions"))
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/api/v1/loans/**").hasAuthority("ROLE_USER")
                        .pathMatchers("/api/v1/loans").hasAuthority("ROLE_USER")
                        .pathMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private WebFilter jwtAuthenticationFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String path = exchange.getRequest().getPath().value();

            for (String publicPath : PUBLIC_PATHS) {
                String pattern = publicPath.replace("/**", "");
                if (path.equals(pattern) || path.startsWith(pattern + "/") || path.equals(publicPath)) {
                    return chain.filter(exchange);
                }
            }

            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = authHeader.substring(7);

            return Mono.fromCallable(() -> tokenProvider.validateToken(token))
                    .flatMap(claims -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        claims.userId().toString(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(claims.role()))
                                );

                        MDC.put("userId", claims.userId().toString());

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                                .doFinally(signal -> MDC.remove("userId"));
                    })
                    .onErrorResume(JwtTokenProvider.InvalidTokenException.class, e ->
                            writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid Token", "Invalid or expired JWT token"))
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Unexpected error during JWT authentication: {}", ex.getMessage());
                        return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication Error", "An error occurred during authentication");
                    });
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:80", "http://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "type", "about:blank",
                "title", error,
                "status", status.value(),
                "detail", message
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] fallback = ("{\"status\":" + status.value() + ",\"detail\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(fallback);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}
