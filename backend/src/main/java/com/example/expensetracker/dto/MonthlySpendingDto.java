package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class MonthlySpendingDto {

    private String month; // e.g. "2026-09" or "Sep 2026"
    private BigDecimal amount;

    public MonthlySpendingDto() {
    }

    public MonthlySpendingDto(String month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
