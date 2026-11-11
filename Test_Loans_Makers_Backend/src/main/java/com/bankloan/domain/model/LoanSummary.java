package com.bankloan.domain.model;

import java.io.Serializable;

public record LoanSummary(long total, long pending, long approved, long rejected) implements Serializable {}
