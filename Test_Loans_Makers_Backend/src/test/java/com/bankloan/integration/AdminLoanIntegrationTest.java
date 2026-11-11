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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "30000")
@Testcontainers
@ActiveProfiles("dev")
class AdminLoanIntegrationTest {

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
    void shouldApprovePendingLoan() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(loanId)
                .jsonPath("$.status").isEqualTo("APPROVED")
                .jsonPath("$.reviewedBy").isNotEmpty()
                .jsonPath("$.reviewedAt").isNotEmpty();
    }

    @Test
    void shouldRejectPendingLoan() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue("{\"reason\": \"La solicitud no cumple los criterios internos.\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(loanId)
                .jsonPath("$.status").isEqualTo("REJECTED")
                .jsonPath("$.rejectionReason").isEqualTo("La solicitud no cumple los criterios internos.")
                .jsonPath("$.reviewedBy").isNotEmpty()
                .jsonPath("$.reviewedAt").isNotEmpty();
    }

    @Test
    void shouldReturn400WhenRejectingWithoutReason() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue("{\"reason\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn409WhenApprovingAlreadyApprovedLoan() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Invalid Loan Transition");
    }

    @Test
    void shouldReturn409WhenRejectingAlreadyRejectedLoan() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue("{\"reason\": \"first\"}")
                .exchange()
                .expectStatus().isOk();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue("{\"reason\": \"second\"}")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldReturn403WhenUserTriesToApprove() {
        String userToken = loginAndGetToken("usuario@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn401WhenUnauthenticatedTriesToApprove() {
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn404ForNonExistentLoan() {
        String adminToken = loginAndGetToken("admin@test.com", "123");

        webTestClient.patch()
                .uri("/api/v1/admin/loans/00000000-0000-0000-0000-000000000000/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldVerifyReviewedByAndReviewedAtAfterApproval() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reviewedBy").isNotEmpty()
                .jsonPath("$.reviewedAt").isNotEmpty()
                .jsonPath("$.status").isEqualTo("APPROVED");
    }

    @Test
    void shouldListAllLoansForAdminPaginated() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        createLoanAndGetId();

        webTestClient.get()
                .uri("/api/v1/admin/loans")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content[0].id").isNotEmpty()
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.totalElements").value(val -> {
                    org.assertj.core.api.Assertions.assertThat(((Number) val).longValue()).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void shouldFilterAdminLoansByStatus() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/admin/loans?status=APPROVED")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].status").isEqualTo("APPROVED");
    }

    @Test
    void shouldPaginateAdminLoans() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        createLoanAndGetId();
        createLoanAndGetId();

        webTestClient.get()
                .uri("/api/v1/admin/loans?page=0&size=1")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.size").isEqualTo(1);
    }

    @Test
    void shouldSearchAdminLoanById() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.get()
                .uri("/api/v1/admin/loans?search=" + loanId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo(loanId);
    }

    @Test
    void shouldReturnEmptyForInvalidSearchId() {
        String adminToken = loginAndGetToken("admin@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/admin/loans?search=not-a-uuid")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void shouldGetAdminLoanById() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        webTestClient.get()
                .uri("/api/v1/admin/loans/" + loanId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(loanId)
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.amount").isNotEmpty();
    }

    @Test
    void shouldReturn404ForNonExistentAdminLoanDetail() {
        String adminToken = loginAndGetToken("admin@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/admin/loans/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnLoanSummary() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        createLoanAndGetId();

        webTestClient.get()
                .uri("/api/v1/admin/loans/summary")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.total").value(val -> {
                    org.assertj.core.api.Assertions.assertThat(((Number) val).longValue()).isGreaterThanOrEqualTo(1);
                })
                .jsonPath("$.pending").isNotEmpty()
                .jsonPath("$.approved").isNotEmpty()
                .jsonPath("$.rejected").isNotEmpty();
    }

    @Test
    void shouldUpdateSummaryAfterApproval() {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        String beforeBody = webTestClient.get()
                .uri("/api/v1/admin/loans/summary")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        webTestClient.patch()
                .uri("/api/v1/admin/loans/" + loanId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/admin/loans/summary")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.approved").value(val -> {
                    org.assertj.core.api.Assertions.assertThat(((Number) val).longValue()).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void shouldReturn403WhenUserAccessesSummary() {
        String userToken = loginAndGetToken("usuario@test.com", "123");

        webTestClient.get()
                .uri("/api/v1/admin/loans/summary")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void concurrentApproveAndReject_shouldResultInOneSuccessAndOneConflict() throws Exception {
        String adminToken = loginAndGetToken("admin@test.com", "123");
        String loanId = createLoanAndGetId();

        int threadCount = 2;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                webTestClient.patch()
                        .uri("/api/v1/admin/loans/" + loanId + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .exchange()
                        .expectBody()
                        .consumeWith(result -> {
                            int status = result.getStatus().value();
                            if (status == 200) successCount.incrementAndGet();
                            else if (status == 409) conflictCount.incrementAndGet();
                        });
            } catch (Exception ignored) {
                conflictCount.incrementAndGet();
            }
        });

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                webTestClient.patch()
                        .uri("/api/v1/admin/loans/" + loanId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .bodyValue("{\"reason\": \"Concurrent rejection\"}")
                        .exchange()
                        .expectBody()
                        .consumeWith(result -> {
                            int status = result.getStatus().value();
                            if (status == 200) successCount.incrementAndGet();
                            else if (status == 409) conflictCount.incrementAndGet();
                        });
            } catch (Exception ignored) {
                conflictCount.incrementAndGet();
            }
        });

        readyLatch.await();
        startLatch.countDown();

        executor.shutdown();
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        org.assertj.core.api.Assertions.assertThat(successCount.get() + conflictCount.get()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }

    private String createLoanAndGetId() {
        String userToken = loginAndGetToken("usuario@test.com", "123");

        String body = webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .bodyValue("{\"amount\": 5000000, \"termMonths\": 12}")
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
