package com.bankloan.application.command;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommandRecordTest {

    @Test
    void createLoanCommand_shouldHoldValues() {
        UUID userId = UUID.randomUUID();
        var cmd = new CreateLoanCommand(userId, BigDecimal.valueOf(5000), 12);
        assertThat(cmd.userId()).isEqualTo(userId);
        assertThat(cmd.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(cmd.termMonths()).isEqualTo(12);
    }

    @Test
    void approveLoanCommand_shouldHoldValues() {
        UUID loanId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        var cmd = new ApproveLoanCommand(loanId, adminId);
        assertThat(cmd.loanId()).isEqualTo(loanId);
        assertThat(cmd.adminId()).isEqualTo(adminId);
    }

    @Test
    void rejectLoanCommand_shouldHoldValues() {
        UUID loanId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        var cmd = new RejectLoanCommand(loanId, adminId, "reason");
        assertThat(cmd.loanId()).isEqualTo(loanId);
        assertThat(cmd.adminId()).isEqualTo(adminId);
        assertThat(cmd.reason()).isEqualTo("reason");
    }
}
