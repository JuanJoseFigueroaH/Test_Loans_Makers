package com.bankloan.domain.exception;

public final class InvalidLoanException extends DomainException {

    public InvalidLoanException(String message) {
        super("INVALID_LOAN", message);
    }
}
