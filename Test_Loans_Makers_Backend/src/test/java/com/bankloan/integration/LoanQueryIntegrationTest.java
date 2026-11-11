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
@AutoConfigureWebTestClient(timeout = "30000")
@Testcontainers
@ActiveProfiles("dev")
class LoanQueryIntegrationTest {

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
    void shouldReturnOwnLoans() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        createLoan(userToken, 5000000, 12);
        createLoan(userToken, 10000000, 24);

        webTestClient.get()
                .uri("/api/v1/loans")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").value(val -> {
                    org.assertj.core.api.Assertions.assertThat((int) val).isGreaterThanOrEqualTo(2);
                })
                .jsonPath("$.totalElements").value(val -> {
                    org.assertj.core.api.Assertions.assertThat(((Number) val).longValue()).isGreaterThanOrEqualTo(2);
                })
                .jsonPath("$.page").isEqualTo(0);
    }

    @Test
    void shouldFilterByStatus() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        String loanId = createLoanAndGetId(userToken, 3000000, 12);

        String adminToken = loginAndGetToken("admin@test.com", "123");
        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/loans?status=APPROVED")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].status").isEqualTo("APPROVED");
    }

    @Test
    void shouldPaginateResults() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        for (int i = 0; i < 3; i++) {
            createLoan(userToken, 1000000 + i * 100000, 12);
        }

        webTestClient.get()
                .uri("/api/v1/loans?page=0&size=2")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.first").isEqualTo(true);
    }

    @Test
    void shouldReturnLoanById() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        String loanId = createLoanAndGetId(userToken, 7000000, 36);

        webTestClient.get()
                .uri("/api/v1/loans/" + loanId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(loanId)
                .jsonPath("$.amount").isEqualTo(7000000)
                .jsonPath("$.termMonths").isEqualTo(36)
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.createdAt").isNotEmpty();
    }

    @Test
    void shouldReturn404ForNonExistentLoan() {
        String userToken = loginAndGetToken("usuario@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/loans/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn403WhenAdminAccessesUserLoans() {
        String adminToken = loginAndGetToken("admin@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/loans")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn401WithoutToken() {
        webTestClient.get()
                .uri("/api/v1/loans")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldShowRejectionReasonAfterReject() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        String loanId = createLoanAndGetId(userToken, 4000000, 12);

        String adminToken = loginAndGetToken("admin@test.com", "123");
        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue("{\"reason\": \"Insufficient income\"}")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/loans/" + loanId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("REJECTED")
                .jsonPath("$.rejectionReason").isEqualTo("Insufficient income")
                .jsonPath("$.reviewedAt").isNotEmpty();
    }

    @Test
    void shouldShowApprovedStatusAfterApproval() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        String loanId = createLoanAndGetId(userToken, 6000000, 24);

        String adminToken = loginAndGetToken("admin@test.com", "123");
        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/loans/" + loanId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("APPROVED")
                .jsonPath("$.reviewedAt").isNotEmpty()
                .jsonPath("$.reviewedBy").isNotEmpty();
    }

    @Test
    void shouldReturnEmptyPageWhenNoLoansMatchFilter() {
        String userToken = loginAndGetToken("usuario@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/loans?status=REJECTED&page=999&size=10")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    private void createLoan(String token, int amount, int termMonths) {
        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": " + amount + ", \"termMonths\": " + termMonths + "}")
                .exchange()
                .expectStatus().isCreated();
    }

    private String createLoanAndGetId(String token, int amount, int termMonths) {
        String body = webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"amount\": " + amount + ", \"termMonths\": " + termMonths + "}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract loan id", e);
        }
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
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody).get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract token", e);
        }
    }
}
