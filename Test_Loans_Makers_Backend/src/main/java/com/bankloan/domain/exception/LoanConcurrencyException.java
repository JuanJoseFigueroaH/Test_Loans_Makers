package com.bankloan.domain.exception;

public final class LoanConcurrencyException extends DomainException {

    public LoanConcurrencyException(String message) {
        super("LOAN_CONCURRENCY_CONFLICT", message);
    }
}
