package com.bankloan.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class LoanTest {

    @Test
    void create_withValidData_shouldCreatePendingLoan() {
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("15000000.00");
        Integer termMonths = 36;

        Loan loan = Loan.create(userId, amount, termMonths);

        assertThat(loan.getId()).isNotNull();
        assertThat(loan.getUserId()).isEqualTo(userId);
        assertThat(loan.getAmount()).isEqualByComparingTo(amount);
        assertThat(loan.getTermMonths()).isEqualTo(termMonths);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(loan.getCreatedAt()).isNotNull();
        assertThat(loan.getUpdatedAt()).isNotNull();
        assertThat(loan.getReviewedAt()).isNull();
        assertThat(loan.getReviewedBy()).isNull();
        assertThat(loan.getRejectionReason()).isNull();
        assertThat(loan.getVersion()).isEqualTo(0L);
    }

    @Test
    void create_withZeroAmount_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), BigDecimal.ZERO, 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void create_withNegativeAmount_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), new BigDecimal("-1000"), 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void create_withNullAmount_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), null, 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void create_withZeroTermMonths_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), new BigDecimal("5000"), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Term months must be greater than zero");
    }

    @Test
    void create_withNegativeTermMonths_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), new BigDecimal("5000"), -6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Term months must be greater than zero");
    }

    @Test
    void create_withNullTermMonths_shouldThrowException() {
        assertThatThrownBy(() -> Loan.create(UUID.randomUUID(), new BigDecimal("5000"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Term months must be greater than zero");
    }

    @Test
    void reconstitute_shouldRestoreAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25000000.50");
        Integer termMonths = 48;
        Instant now = Instant.now();
        UUID reviewedBy = UUID.randomUUID();

        Loan loan = Loan.reconstitute(id, userId, amount, termMonths, LoanStatus.APPROVED,
                now, now, now, reviewedBy, null, 1L);

        assertThat(loan.getId()).isEqualTo(id);
        assertThat(loan.getUserId()).isEqualTo(userId);
        assertThat(loan.getAmount()).isEqualByComparingTo(amount);
        assertThat(loan.getTermMonths()).isEqualTo(termMonths);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(loan.getReviewedBy()).isEqualTo(reviewedBy);
        assertThat(loan.getVersion()).isEqualTo(1L);
    }

    @Test
    void create_shouldAlwaysGenerateUniqueIds() {
        Loan loan1 = Loan.create(UUID.randomUUID(), new BigDecimal("1000"), 12);
        Loan loan2 = Loan.create(UUID.randomUUID(), new BigDecimal("2000"), 24);

        assertThat(loan1.getId()).isNotEqualTo(loan2.getId());
    }

    @Test
    void approve_pendingLoan_shouldTransitionToApproved() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        UUID adminId = UUID.randomUUID();

        loan.approve(adminId);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(loan.getReviewedBy()).isEqualTo(adminId);
        assertThat(loan.getReviewedAt()).isNotNull();
        assertThat(loan.getRejectionReason()).isNull();
    }

    @Test
    void reject_pendingLoan_shouldTransitionToRejected() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        UUID adminId = UUID.randomUUID();
        String reason = "Does not meet criteria";

        loan.reject(adminId, reason);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.REJECTED);
        assertThat(loan.getReviewedBy()).isEqualTo(adminId);
        assertThat(loan.getReviewedAt()).isNotNull();
        assertThat(loan.getRejectionReason()).isEqualTo(reason);
    }

    @Test
    void approve_approvedLoan_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.approve(UUID.randomUUID());

        assertThatThrownBy(() -> loan.approve(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void reject_approvedLoan_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.approve(UUID.randomUUID());

        assertThatThrownBy(() -> loan.reject(UUID.randomUUID(), "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void approve_rejectedLoan_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.reject(UUID.randomUUID(), "reason");

        assertThatThrownBy(() -> loan.approve(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REJECTED");
    }

    @Test
    void reject_rejectedLoan_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.reject(UUID.randomUUID(), "first reason");

        assertThatThrownBy(() -> loan.reject(UUID.randomUUID(), "second reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REJECTED");
    }

    @Test
    void reject_withNullReason_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);

        assertThatThrownBy(() -> loan.reject(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rejection reason is required");
    }

    @Test
    void reject_withEmptyReason_shouldThrowException() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);

        assertThatThrownBy(() -> loan.reject(UUID.randomUUID(), "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rejection reason is required");
    }

    @Test
    void isPending_shouldReturnTrueForNewLoan() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        assertThat(loan.isPending()).isTrue();
    }

    @Test
    void isPending_shouldReturnFalseAfterApproval() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.approve(UUID.randomUUID());
        assertThat(loan.isPending()).isFalse();
    }
}
