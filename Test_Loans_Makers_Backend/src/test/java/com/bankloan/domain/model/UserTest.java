package com.bankloan.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithCorrectDefaults() {
        User user = User.create("test@test.com", "hashedPassword", Role.ROLE_USER);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateAdminUser() {
        User user = User.create("admin@test.com", "hashedPassword", Role.ROLE_ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void shouldReconstituteUser() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user = User.reconstitute(id, "test@test.com", "hash", Role.ROLE_USER, true, now, now);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldReconstituteDisabledUser() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user = User.reconstitute(id, "test@test.com", "hash", Role.ROLE_USER, false, now, now);

        assertThat(user.isEnabled()).isFalse();
    }
}
