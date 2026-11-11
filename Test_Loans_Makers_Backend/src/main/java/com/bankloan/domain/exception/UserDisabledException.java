package com.bankloan.domain.exception;

public final class UserDisabledException extends DomainException {

    public UserDisabledException() {
        super("USER_DISABLED", "User account is disabled");
    }
}
