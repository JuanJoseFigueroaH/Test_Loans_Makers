package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoanJpaEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        LoanJpaEntity entity = new LoanJpaEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewedBy = UUID.randomUUID();
        Instant now = Instant.now();

        entity.setId(id);
        entity.setUserId(userId);
        entity.setAmount(BigDecimal.valueOf(5000000));
        entity.setTermMonths(12);
        entity.setStatus(LoanStatus.PENDING);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setReviewedAt(now);
        entity.setReviewedBy(reviewedBy);
        entity.setRejectionReason("Test reason");
        entity.setVersion(1L);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000000));
        assertThat(entity.getTermMonths()).isEqualTo(12);
        assertThat(entity.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getReviewedAt()).isEqualTo(now);
        assertThat(entity.getReviewedBy()).isEqualTo(reviewedBy);
        assertThat(entity.getRejectionReason()).isEqualTo("Test reason");
        assertThat(entity.getVersion()).isEqualTo(1L);
    }
}
