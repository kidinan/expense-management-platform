package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class CategorySummaryDto {

    private String category;
    private BigDecimal totalAmount;
    private Long count;
    private Double percentage;

    public CategorySummaryDto() {
    }

    public CategorySummaryDto(String category, BigDecimal totalAmount, Long count) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    public CategorySummaryDto(String category, BigDecimal totalAmount, Long count, Double percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
