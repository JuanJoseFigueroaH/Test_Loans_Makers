package com.bankloan.application.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheNamesTest {

    @Test
    void shouldDefineAllCacheNames() {
        assertThat(CacheNames.LOAN_BY_ID).isEqualTo("loan_by_id");
        assertThat(CacheNames.LOAN_SUMMARY).isEqualTo("loan_summary");
        assertThat(CacheNames.USER_LOANS).isEqualTo("user_loans");
        assertThat(CacheNames.ADMIN_LOANS).isEqualTo("admin_loans");
    }
}
