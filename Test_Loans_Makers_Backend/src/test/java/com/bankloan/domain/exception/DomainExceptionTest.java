package com.bankloan.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void invalidCredentialsException_shouldHaveCorrectCodeAndMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        assertThat(ex.getCode()).isEqualTo("AUTH_INVALID_CREDENTIALS");
        assertThat(ex.getMessage()).isEqualTo("Invalid email or password");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void userDisabledException_shouldHaveCorrectCodeAndMessage() {
        UserDisabledException ex = new UserDisabledException();

        assertThat(ex.getCode()).isEqualTo("USER_DISABLED");
        assertThat(ex.getMessage()).isEqualTo("User account is disabled");
    }

    @Test
    void userNotFoundException_shouldIncludeIdentifier() {
        UserNotFoundException ex = new UserNotFoundException("user@test.com");

        assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("User not found: user@test.com");
    }

    @Test
    void invalidLoanException_shouldPreserveMessage() {
        InvalidLoanException ex = new InvalidLoanException("Amount must be greater than zero");

        assertThat(ex.getCode()).isEqualTo("INVALID_LOAN");
        assertThat(ex.getMessage()).isEqualTo("Amount must be greater than zero");
    }

    @Test
    void invalidLoanTransitionException_shouldPreserveMessage() {
        InvalidLoanTransitionException ex = new InvalidLoanTransitionException("Loan is already APPROVED");

        assertThat(ex.getCode()).isEqualTo("INVALID_LOAN_TRANSITION");
        assertThat(ex.getMessage()).isEqualTo("Loan is already APPROVED");
    }

    @Test
    void loanNotFoundException_shouldFormatLoanId() {
        String loanId = "550e8400-e29b-41d4-a716-446655440000";
        LoanNotFoundException ex = new LoanNotFoundException(loanId);

        assertThat(ex.getCode()).isEqualTo("LOAN_NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("Loan not found with id: " + loanId);
    }

    @Test
    void loanConcurrencyException_shouldPreserveMessage() {
        LoanConcurrencyException ex = new LoanConcurrencyException("modified by another administrator");

        assertThat(ex.getCode()).isEqualTo("LOAN_CONCURRENCY_CONFLICT");
        assertThat(ex.getMessage()).contains("modified by another administrator");
    }

    @Test
    void allExceptions_shouldExtendDomainException() {
        assertThat(new InvalidCredentialsException()).isInstanceOf(DomainException.class);
        assertThat(new UserDisabledException()).isInstanceOf(DomainException.class);
        assertThat(new UserNotFoundException("x")).isInstanceOf(DomainException.class);
        assertThat(new InvalidLoanException("x")).isInstanceOf(DomainException.class);
        assertThat(new InvalidLoanTransitionException("x")).isInstanceOf(DomainException.class);
        assertThat(new LoanNotFoundException("x")).isInstanceOf(DomainException.class);
        assertThat(new LoanConcurrencyException("x")).isInstanceOf(DomainException.class);
    }
}
