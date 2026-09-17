package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<ExpenseResponse> getExpenses(Long userId,
                                            String category,
                                            String search,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            String sortBy,
                                            String sortDirection) {
        // Sort direction and field
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = "amount".equalsIgnoreCase(sortBy) ? "amount" : "date";
        Sort sort = Sort.by(direction, sortProperty);

        List<Expense> expenses;
        boolean hasFilters = (category != null && !category.isBlank()) ||
                             (search != null && !search.isBlank()) ||
                             startDate != null ||
                             endDate != null;

        if (hasFilters) {
            expenses = expenseRepository.findWithFilters(userId, category, search, startDate, endDate, sort);
        } else {
            expenses = expenseRepository.findAllByUserId(userId, sort);
        }

        return expenses.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {
        // Strict server-side authorization: user can only access their own expense
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or access denied for ID: " + expenseId));

        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        // Strict server-side authorization: user can only edit their own expense
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
        // Strict server-side authorization: user can only delete their own expense
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
