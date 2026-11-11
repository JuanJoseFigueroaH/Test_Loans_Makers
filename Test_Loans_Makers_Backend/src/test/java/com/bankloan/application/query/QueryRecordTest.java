package com.bankloan.application.query;

import com.bankloan.domain.model.LoanStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRecordTest {

    @Test
    void getLoanByIdQuery_shouldHoldValues() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var query = new GetLoanByIdQuery(loanId, userId);
        assertThat(query.loanId()).isEqualTo(loanId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    void getMyLoansQuery_shouldHoldValues() {
        UUID userId = UUID.randomUUID();
        var query = new GetMyLoansQuery(userId, LoanStatus.PENDING, 0, 10);
        assertThat(query.userId()).isEqualTo(userId);
        assertThat(query.status()).isEqualTo(LoanStatus.PENDING);
        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(10);
    }
}
