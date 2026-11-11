package com.bankloan.domain.port.out;

import java.util.UUID;

public interface TokenProvider {

    String generateToken(UUID userId, String email, String role);

    TokenClaims validateToken(String token);

    record TokenClaims(UUID userId, String email, String role) {}
}
