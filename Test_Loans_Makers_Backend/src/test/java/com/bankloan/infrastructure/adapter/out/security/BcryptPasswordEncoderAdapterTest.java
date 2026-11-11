package com.bankloan.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordEncoderAdapterTest {

    private final BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

    @Test
    void encode_shouldReturnHashedPassword() {
        String hash = adapter.encode("password123");
        assertThat(hash).isNotNull().isNotEmpty().isNotEqualTo("password123");
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String hash = adapter.encode("password123");
        assertThat(adapter.matches("password123", hash)).isTrue();
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String hash = adapter.encode("password123");
        assertThat(adapter.matches("wrongpassword", hash)).isFalse();
    }
}
