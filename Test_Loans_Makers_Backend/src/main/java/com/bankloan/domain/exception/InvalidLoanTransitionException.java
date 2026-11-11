package com.bankloan.domain.exception;

public final class InvalidLoanTransitionException extends DomainException {

    public InvalidLoanTransitionException(String message) {
        super("INVALID_LOAN_TRANSITION", message);
    }
}
