package com.bankloan.application.mapper;

import com.bankloan.application.dto.UserResponse;
import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = User.reconstitute(id, "test@test.com", "hash", Role.ROLE_USER, true, now, now);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.enabled()).isTrue();
    }

    @Test
    void toResponse_shouldMapAdminRole() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = User.reconstitute(id, "admin@test.com", "hash", Role.ROLE_ADMIN, true, now, now);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.role()).isEqualTo("ROLE_ADMIN");
    }
}
