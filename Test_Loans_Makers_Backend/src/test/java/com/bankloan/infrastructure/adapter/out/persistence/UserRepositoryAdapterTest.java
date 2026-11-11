package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock private UserJpaRepository jpaRepository;
    @Mock private UserPersistenceMapper mapper;

    @InjectMocks private UserRepositoryAdapter adapter;

    private final UUID userId = UUID.randomUUID();
    private UserJpaEntity entity;
    private User domainUser;

    @BeforeEach
    void setUp() {
        entity = new UserJpaEntity();
        entity.setId(userId);
        entity.setEmail("test@test.com");
        entity.setPasswordHash("hash");
        entity.setRole(Role.ROLE_USER);
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        domainUser = User.reconstitute(userId, "test@test.com", "hash", Role.ROLE_USER, true, Instant.now(), Instant.now());
    }

    @Test
    void findByEmail_shouldReturnUser() {
        when(jpaRepository.findByEmail("test@test.com")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        Optional<User> result = adapter.findByEmail("test@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void findByEmail_shouldReturnEmpty() {
        when(jpaRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail("none@test.com");
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnUser() {
        when(jpaRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        Optional<User> result = adapter.findById(userId);
        assertThat(result).isPresent();
    }

    @Test
    void save_shouldMapAndPersist() {
        when(mapper.toJpaEntity(domainUser)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        User result = adapter.save(domainUser);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(jpaRepository).save(entity);
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        when(jpaRepository.existsByEmail("test@test.com")).thenReturn(true);
        assertThat(adapter.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse() {
        when(jpaRepository.existsByEmail("none@test.com")).thenReturn(false);
        assertThat(adapter.existsByEmail("none@test.com")).isFalse();
    }
}
