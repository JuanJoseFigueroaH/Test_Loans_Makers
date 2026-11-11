package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMapperTest {

    private final LoanPersistenceMapper loanMapper = Mappers.getMapper(LoanPersistenceMapper.class);
    private final UserPersistenceMapper userMapper = Mappers.getMapper(UserPersistenceMapper.class);

    @Test
    void loanMapper_toJpaEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewedBy = UUID.randomUUID();
        Instant now = Instant.now();
        Loan loan = Loan.reconstitute(id, userId, BigDecimal.valueOf(5000), 12,
                LoanStatus.APPROVED, now, now, now, reviewedBy, "reason", 1L);

        LoanJpaEntity entity = loanMapper.toJpaEntity(loan);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(entity.getTermMonths()).isEqualTo(12);
        assertThat(entity.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(entity.getReviewedBy()).isEqualTo(reviewedBy);
        assertThat(entity.getRejectionReason()).isEqualTo("reason");
        assertThat(entity.getVersion()).isEqualTo(1L);
    }

    @Test
    void loanMapper_toDomain_shouldMapAllFields() {
        LoanJpaEntity entity = new LoanJpaEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setAmount(BigDecimal.valueOf(3000));
        entity.setTermMonths(6);
        entity.setStatus(LoanStatus.PENDING);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setVersion(0L);

        Loan loan = loanMapper.toDomain(entity);

        assertThat(loan.getId()).isEqualTo(id);
        assertThat(loan.getUserId()).isEqualTo(userId);
        assertThat(loan.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(loan.getTermMonths()).isEqualTo(6);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void userMapper_toJpaEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = User.reconstitute(id, "test@test.com", "hash", Role.ROLE_USER, true, now, now);

        UserJpaEntity entity = userMapper.toJpaEntity(user);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getEmail()).isEqualTo("test@test.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(entity.isEnabled()).isTrue();
    }

    @Test
    void userMapper_toDomain_shouldMapAllFields() {
        UserJpaEntity entity = new UserJpaEntity();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        entity.setId(id);
        entity.setEmail("admin@test.com");
        entity.setPasswordHash("hash2");
        entity.setRole(Role.ROLE_ADMIN);
        entity.setEnabled(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        User user = userMapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo("admin@test.com");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(user.isEnabled()).isFalse();
    }
}
