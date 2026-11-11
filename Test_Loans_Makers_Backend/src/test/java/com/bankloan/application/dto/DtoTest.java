package com.bankloan.application.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void createLoanRequest_shouldHoldValues() {
        var dto = new CreateLoanRequest(BigDecimal.valueOf(5000), 12);
        assertThat(dto.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(dto.termMonths()).isEqualTo(12);
    }

    @Test
    void loginRequest_shouldHoldValues() {
        var dto = new LoginRequest("test@test.com", "pass");
        assertThat(dto.email()).isEqualTo("test@test.com");
        assertThat(dto.password()).isEqualTo("pass");
    }

    @Test
    void loginResponse_shouldHoldValues() {
        UUID uid = UUID.randomUUID();
        var user = new UserResponse(uid, "test@test.com", "ROLE_USER", true, Instant.now());
        var dto = new LoginResponse("token", user);
        assertThat(dto.token()).isEqualTo("token");
        assertThat(dto.user().email()).isEqualTo("test@test.com");
    }

    @Test
    void loanResponse_shouldHoldValues() {
        Instant now = Instant.now();
        var dto = new LoanResponse("id", "userId", BigDecimal.TEN, 12, "PENDING", now, now, null, null, null);
        assertThat(dto.id()).isEqualTo("id");
        assertThat(dto.status()).isEqualTo("PENDING");
        assertThat(dto.reviewedAt()).isNull();
        assertThat(dto.reviewedBy()).isNull();
        assertThat(dto.rejectionReason()).isNull();
    }

    @Test
    void loanSummaryResponse_shouldHoldValues() {
        var dto = new LoanSummaryResponse(10, 5, 3, 2);
        assertThat(dto.total()).isEqualTo(10);
        assertThat(dto.pending()).isEqualTo(5);
        assertThat(dto.approved()).isEqualTo(3);
        assertThat(dto.rejected()).isEqualTo(2);
    }

    @Test
    void pagedResponse_shouldHoldValues() {
        var dto = new PagedResponse<>(List.of("a", "b"), 0, 10, 2, 1, true, true);
        assertThat(dto.content()).hasSize(2);
        assertThat(dto.page()).isZero();
        assertThat(dto.size()).isEqualTo(10);
        assertThat(dto.totalElements()).isEqualTo(2);
        assertThat(dto.totalPages()).isEqualTo(1);
        assertThat(dto.first()).isTrue();
        assertThat(dto.last()).isTrue();
    }

    @Test
    void rejectLoanRequest_shouldHoldValues() {
        var dto = new RejectLoanRequest("reason");
        assertThat(dto.reason()).isEqualTo("reason");
    }

    @Test
    void userResponse_shouldHoldValues() {
        Instant now = Instant.now();
        UUID uid2 = UUID.randomUUID();
        var dto = new UserResponse(uid2, "test@test.com", "ROLE_ADMIN", true, now);
        assertThat(dto.id()).isEqualTo(uid2);
        assertThat(dto.email()).isEqualTo("test@test.com");
        assertThat(dto.role()).isEqualTo("ROLE_ADMIN");
        assertThat(dto.enabled()).isTrue();
    }
}
