package com.bankloan.domain.exception;

public final class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("AUTH_INVALID_CREDENTIALS", "Invalid email or password");
    }
}
