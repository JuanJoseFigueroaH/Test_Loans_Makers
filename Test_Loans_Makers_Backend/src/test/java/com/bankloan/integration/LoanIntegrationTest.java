package com.bankloan.integration;

import com.bankloan.application.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("dev")
class LoanIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bankloan_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateLoanWithValidUserToken() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 15000000, \"termMonths\": 36}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.userId").isNotEmpty()
                .jsonPath("$.amount").isEqualTo(15000000)
                .jsonPath("$.termMonths").isEqualTo(36)
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.updatedAt").isNotEmpty()
                .jsonPath("$.reviewedAt").doesNotExist()
                .jsonPath("$.reviewedBy").doesNotExist()
                .jsonPath("$.rejectionReason").doesNotExist();
    }

    @Test
    void shouldReturn400ForZeroAmount() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 0, \"termMonths\": 12}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400ForNegativeAmount() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": -5000, \"termMonths\": 12}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400ForInvalidTermMonths() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 5000000, \"termMonths\": 0}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400ForNegativeTermMonths() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 5000000, \"termMonths\": -6}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn401ForUnauthenticatedUser() {
        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 5000000, \"termMonths\": 12}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn403ForAdminCreatingLoan() {
        String token = loginAndGetToken("admin@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 5000000, \"termMonths\": 12}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn400ForMissingFields() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnLoanWithPendingStatus() {
        String token = loginAndGetToken("usuario@test.com", "123");

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": 25000000.50, \"termMonths\": 48}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    private String loginAndGetToken(String email, String password) {
        String responseBody = webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
            return node.get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract token from response", e);
        }
    }
}
