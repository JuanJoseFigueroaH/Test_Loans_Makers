package com.bankloan.domain.exception;

public final class UserNotFoundException extends DomainException {

    public UserNotFoundException(String identifier) {
        super("USER_NOT_FOUND", "User not found: " + identifier);
    }
}
