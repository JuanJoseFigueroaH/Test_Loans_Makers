package com.bankloan.application.mapper;

import com.bankloan.application.dto.LoanResponse;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoanMapperTest {

    private final LoanMapper mapper = Mappers.getMapper(LoanMapper.class);

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewedBy = UUID.randomUUID();
        Instant now = Instant.now();
        Loan loan = Loan.reconstitute(id, userId, BigDecimal.valueOf(5000), 12,
                LoanStatus.APPROVED, now, now, now, reviewedBy, null, 1L);

        LoanResponse response = mapper.toResponse(loan);

        assertThat(response.id()).isEqualTo(id.toString());
        assertThat(response.userId()).isEqualTo(userId.toString());
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.termMonths()).isEqualTo(12);
        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.reviewedBy()).isEqualTo(reviewedBy.toString());
    }

    @Test
    void toResponse_shouldHandleNullReviewedBy() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Loan loan = Loan.reconstitute(id, userId, BigDecimal.valueOf(1000), 6,
                LoanStatus.PENDING, now, now, null, null, null, 0L);

        LoanResponse response = mapper.toResponse(loan);

        assertThat(response.reviewedBy()).isNull();
        assertThat(response.reviewedAt()).isNull();
        assertThat(response.rejectionReason()).isNull();
    }
}
