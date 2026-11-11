package com.bankloan.domain.port.in;

import com.bankloan.domain.model.User;

public interface AuthUseCase {

    AuthResult login(String email, String rawPassword);

    User getCurrentUser(String userId);

    record AuthResult(String token, User user) {}
}
