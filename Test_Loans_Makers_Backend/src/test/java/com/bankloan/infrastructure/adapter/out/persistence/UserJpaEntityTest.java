package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserJpaEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        UserJpaEntity entity = new UserJpaEntity();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        entity.setId(id);
        entity.setEmail("test@test.com");
        entity.setPasswordHash("hash");
        entity.setRole(Role.ROLE_USER);
        entity.setEnabled(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getEmail()).isEqualTo("test@test.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
