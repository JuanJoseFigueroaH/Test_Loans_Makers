package com.bankloan.infrastructure.config;

import com.bankloan.domain.exception.*;
import com.bankloan.infrastructure.adapter.out.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleInvalidCredentials_shouldReturn401() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(problem.getTitle()).isEqualTo("Invalid Credentials");
                    assertThat(problem.getDetail()).isEqualTo("Invalid email or password");
                    assertThat(problem.getProperties()).containsKey("code");
                    assertThat(problem.getProperties().get("code")).isEqualTo("AUTH_INVALID_CREDENTIALS");
                    assertThat(problem.getProperties()).containsKey("timestamp");
                    assertThat(problem.getType().toString()).contains("authentication/invalid-credentials");
                })
                .verifyComplete();
    }

    @Test
    void handleUserDisabled_shouldReturn403() {
        UserDisabledException ex = new UserDisabledException();

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
                    assertThat(problem.getTitle()).isEqualTo("Account Disabled");
                    assertThat(problem.getProperties().get("code")).isEqualTo("USER_DISABLED");
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidToken_shouldReturn401() {
        JwtTokenProvider.InvalidTokenException ex =
                new JwtTokenProvider.InvalidTokenException("Token expired");

        Mono<ProblemDetail> result = handler.handleInvalidToken(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(problem.getTitle()).isEqualTo("Invalid Token");
                    assertThat(problem.getProperties().get("code")).isEqualTo("INVALID_TOKEN");
                })
                .verifyComplete();
    }

    @Test
    void handleUserNotFound_shouldReturn404() {
        UserNotFoundException ex = new UserNotFoundException("user@test.com");

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(problem.getTitle()).isEqualTo("User Not Found");
                    assertThat(problem.getProperties().get("code")).isEqualTo("USER_NOT_FOUND");
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidLoan_shouldReturn400() {
        InvalidLoanException ex = new InvalidLoanException("Amount must be greater than zero");

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(problem.getTitle()).isEqualTo("Invalid Loan Request");
                    assertThat(problem.getDetail()).isEqualTo("Amount must be greater than zero");
                    assertThat(problem.getProperties().get("code")).isEqualTo("INVALID_LOAN");
                })
                .verifyComplete();
    }

    @Test
    void handleLoanNotFound_shouldReturn404() {
        LoanNotFoundException ex = new LoanNotFoundException("some-id");

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(problem.getTitle()).isEqualTo("Loan Not Found");
                    assertThat(problem.getProperties().get("code")).isEqualTo("LOAN_NOT_FOUND");
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidLoanTransition_shouldReturn409() {
        InvalidLoanTransitionException ex =
                new InvalidLoanTransitionException("Loan is already APPROVED");

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(problem.getTitle()).isEqualTo("Invalid Loan Transition");
                    assertThat(problem.getProperties().get("code")).isEqualTo("INVALID_LOAN_TRANSITION");
                })
                .verifyComplete();
    }

    @Test
    void handleLoanConcurrency_shouldReturn409() {
        LoanConcurrencyException ex =
                new LoanConcurrencyException("modified by another administrator");

        Mono<ProblemDetail> result = handler.handleDomainException(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(problem.getTitle()).isEqualTo("Concurrency Conflict");
                    assertThat(problem.getProperties().get("code")).isEqualTo("LOAN_CONCURRENCY_CONFLICT");
                })
                .verifyComplete();
    }

    @Test
    void handleBadInput_shouldReturn400() {
        org.springframework.web.server.ServerWebInputException ex =
                new org.springframework.web.server.ServerWebInputException("bad body");

        Mono<ProblemDetail> result = handler.handleBadInput(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(problem.getTitle()).isEqualTo("Bad Request");
                    assertThat(problem.getProperties().get("code")).isEqualTo("BAD_INPUT");
                })
                .verifyComplete();
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid param");

        Mono<ProblemDetail> result = handler.handleIllegalArgument(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(problem.getProperties().get("code")).isEqualTo("ILLEGAL_ARGUMENT");
                    assertThat(problem.getDetail()).isEqualTo("Invalid param");
                })
                .verifyComplete();
    }

    @Test
    void handleDataAccess_shouldReturn503() {
        DataAccessException ex = new DataAccessException("Connection refused") {};

        Mono<ProblemDetail> result = handler.handleDataAccess(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
                    assertThat(problem.getTitle()).isEqualTo("Service Temporarily Unavailable");
                    assertThat(problem.getProperties().get("code")).isEqualTo("DATABASE_ERROR");
                })
                .verifyComplete();
    }

    @Test
    void handleGeneric_shouldReturn500() {
        Exception ex = new RuntimeException("Unexpected failure");

        Mono<ProblemDetail> result = handler.handleGeneric(ex);

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
                    assertThat(problem.getProperties().get("code")).isEqualTo("INTERNAL_ERROR");
                })
                .verifyComplete();
    }

    @Test
    void allResponses_shouldContainTimestamp() {
        Mono<ProblemDetail> result = handler.handleGeneric(new RuntimeException("test"));

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getProperties()).containsKey("timestamp");
                    assertThat(problem.getProperties().get("timestamp").toString()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void allResponses_shouldContainTypeUri() {
        Mono<ProblemDetail> result = handler.handleDomainException(new InvalidLoanException("test"));

        StepVerifier.create(result)
                .assertNext(problem -> {
                    assertThat(problem.getType().toString())
                            .startsWith("https://api.bankloan.com/errors/");
                })
                .verifyComplete();
    }
}
