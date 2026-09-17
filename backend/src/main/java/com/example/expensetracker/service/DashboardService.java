package com.example.expensetracker.service;

import com.example.expensetracker.dto.CategorySummaryDto;
import com.example.expensetracker.dto.DashboardStatsResponse;
import com.example.expensetracker.dto.MonthlySpendingDto;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;

    public DashboardService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long userId) {
        // 1. Total expenses
        BigDecimal totalExpenses = expenseRepository.sumTotalByUserId(userId);
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        // 2. Monthly expenses (current month)
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        BigDecimal monthlyExpenses = expenseRepository.sumByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
        if (monthlyExpenses == null) {
            monthlyExpenses = BigDecimal.ZERO;
        }

        // 3. Expense count
        Long expenseCount = expenseRepository.countByUserId(userId);

        // 4. Spending by category
        List<Object[]> rawCategoryData = expenseRepository.getCategorySpending(userId);
        List<CategorySummaryDto> categorySummaries = new ArrayList<>();

        for (Object[] row : rawCategoryData) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();

            Double percentage = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            categorySummaries.add(new CategorySummaryDto(category, amount, count, percentage));
        }

        // 5. Monthly trend (past 6 months)
        List<Expense> allUserExpenses = expenseRepository.findAllByUserId(userId, Sort.by(Sort.Direction.ASC, "date"));
        Map<YearMonth, BigDecimal> monthlyMap = new TreeMap<>();

        // Initialize last 6 months with 0
        YearMonth currentYearMonth = YearMonth.from(now);
        for (int i = 5; i >= 0; i--) {
            monthlyMap.put(currentYearMonth.minusMonths(i), BigDecimal.ZERO);
        }

        for (Expense expense : allUserExpenses) {
            YearMonth ym = YearMonth.from(expense.getDate());
            if (monthlyMap.containsKey(ym)) {
                monthlyMap.put(ym, monthlyMap.get(ym).add(expense.getAmount()));
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<MonthlySpendingDto> monthlyTrend = monthlyMap.entrySet().stream()
                .map(entry -> new MonthlySpendingDto(entry.getKey().format(formatter), entry.getValue()))
                .collect(Collectors.toList());

        return new DashboardStatsResponse(totalExpenses, monthlyExpenses, expenseCount, categorySummaries, monthlyTrend);
    }
}
