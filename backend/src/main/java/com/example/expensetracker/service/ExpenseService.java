package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.PagedExpenseResponse;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense(
                request.getAmount(),
                request.getDate(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                user
        );

        Expense saved = expenseRepository.save(expense);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedExpenseResponse getExpenses(Long userId,
                                            String category,
                                            String search,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            int page,
                                            int size,
                                            String sortBy,
                                            String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = "amount".equalsIgnoreCase(sortBy) ? "amount" : "date";
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(direction, sortProperty));

        Page<Expense> expensePage;
        boolean hasFilters = (category != null && !category.isBlank()) ||
                             (search != null && !search.isBlank()) ||
                             startDate != null ||
                             endDate != null;

        if (hasFilters) {
            expensePage = expenseRepository.findWithFilters(userId, category, search, startDate, endDate, pageable);
        } else {
            expensePage = expenseRepository.findAllByUserId(userId, pageable);
        }

        List<ExpenseResponse> content = expensePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedExpenseResponse(
                content,
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportExpensesToCsv(Long userId,
                                      String category,
                                      String search,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String sortBy,
                                      String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = "amount".equalsIgnoreCase(sortBy) ? "amount" : "date";
        Sort sort = Sort.by(direction, property).and(Sort.by(Sort.Direction.DESC, "id"));

        List<Expense> expenses = expenseRepository.findWithFiltersList(
                userId,
                (category != null && !category.trim().isEmpty()) ? category.trim() : null,
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                startDate,
                endDate,
                sort
        );

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Date,Category,Description,Amount\n");

        for (Expense exp : expenses) {
            sb.append(exp.getId()).append(",")
              .append(exp.getDate()).append(",")
              .append(escapeCsv(exp.getCategory())).append(",")
              .append(escapeCsv(exp.getDescription())).append(",")
              .append(exp.getAmount()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return "\"" + value + "\"";
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getRecentExpenses(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit), Sort.by(Sort.Direction.DESC, "date"));
        return expenseRepository.findAllByUserId(userId, pageable)
                .getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or access denied for ID: " + expenseId));

        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or access denied for ID: " + expenseId));

        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription().trim());
        expense.setCategory(request.getCategory().trim());

        Expense updated = expenseRepository.save(expense);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or access denied for ID: " + expenseId));

        expenseRepository.delete(expense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDate(),
                expense.getDescription(),
                expense.getCategory(),
                expense.getCreatedAt()
        );
    }
}
