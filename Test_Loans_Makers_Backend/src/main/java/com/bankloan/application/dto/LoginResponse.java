package com.bankloan.application.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {}
