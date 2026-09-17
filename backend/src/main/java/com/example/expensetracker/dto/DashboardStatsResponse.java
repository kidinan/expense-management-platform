package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsResponse {

    private BigDecimal totalExpenses;
    private BigDecimal monthlyExpenses;
    private Long expenseCount;
    private List<CategorySummaryDto> spendingByCategory;
    private List<MonthlySpendingDto> monthlyTrend;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(BigDecimal totalExpenses, BigDecimal monthlyExpenses, Long expenseCount,
                                  List<CategorySummaryDto> spendingByCategory, List<MonthlySpendingDto> monthlyTrend) {
        this.totalExpenses = totalExpenses;
        this.monthlyExpenses = monthlyExpenses;
        this.expenseCount = expenseCount;
        this.spendingByCategory = spendingByCategory;
        this.monthlyTrend = monthlyTrend;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public Long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(Long expenseCount) {
        this.expenseCount = expenseCount;
    }

    public List<CategorySummaryDto> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(List<CategorySummaryDto> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }

    public List<MonthlySpendingDto> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<MonthlySpendingDto> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }
}
