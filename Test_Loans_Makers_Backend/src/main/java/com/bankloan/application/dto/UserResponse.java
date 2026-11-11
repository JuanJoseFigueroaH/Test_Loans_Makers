package com.bankloan.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String role,
        boolean enabled,
        Instant createdAt
) {}
