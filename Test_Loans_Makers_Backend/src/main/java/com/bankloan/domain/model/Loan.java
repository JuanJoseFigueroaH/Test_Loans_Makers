package com.bankloan.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private BigDecimal amount;
    private Integer termMonths;
    private LoanStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant reviewedAt;
    private UUID reviewedBy;
    private String rejectionReason;
    private Long version;

    private Loan() {}

    public static Loan create(UUID userId, BigDecimal amount, Integer termMonths) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (termMonths == null || termMonths <= 0) {
            throw new IllegalArgumentException("Term months must be greater than zero");
        }

        Loan loan = new Loan();
        loan.id = UUID.randomUUID();
        loan.userId = userId;
        loan.amount = amount;
        loan.termMonths = termMonths;
        loan.status = LoanStatus.PENDING;
        loan.createdAt = Instant.now();
        loan.updatedAt = Instant.now();
        loan.version = 0L;
        return loan;
    }

    @JsonCreator
    public static Loan reconstitute(
            @JsonProperty("id") UUID id,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("termMonths") Integer termMonths,
            @JsonProperty("status") LoanStatus status,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt,
            @JsonProperty("reviewedAt") Instant reviewedAt,
            @JsonProperty("reviewedBy") UUID reviewedBy,
            @JsonProperty("rejectionReason") String rejectionReason,
            @JsonProperty("version") Long version) {
        Loan loan = new Loan();
        loan.id = id;
        loan.userId = userId;
        loan.amount = amount;
        loan.termMonths = termMonths;
        loan.status = status;
        loan.createdAt = createdAt;
        loan.updatedAt = updatedAt;
        loan.reviewedAt = reviewedAt;
        loan.reviewedBy = reviewedBy;
        loan.rejectionReason = rejectionReason;
        loan.version = version;
        return loan;
    }

    public void approve(UUID adminId) {
        assertPending();
        this.status = LoanStatus.APPROVED;
        this.reviewedBy = adminId;
        this.reviewedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void reject(UUID adminId, String reason) {
        assertPending();
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.status = LoanStatus.REJECTED;
        this.reviewedBy = adminId;
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason;
        this.updatedAt = Instant.now();
    }

    public boolean isPending() {
        return this.status == LoanStatus.PENDING;
    }

    private void assertPending() {
        if (this.status != LoanStatus.PENDING) {
            throw new IllegalStateException(
                    "Loan " + this.id + " is already " + this.status + " and cannot be modified");
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public Integer getTermMonths() { return termMonths; }
    public LoanStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public UUID getReviewedBy() { return reviewedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public Long getVersion() { return version; }
}
