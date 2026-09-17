package com.example.expensetracker;

import com.example.expensetracker.dto.AuthRequest;
import com.example.expensetracker.dto.AuthResponse;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtTokenProvider;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseTrackerApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void testUserRegistration() {
        RegisterRequest req = new RegisterRequest("johndoe", "john@example.com", "secret123", "John Doe");

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");

        User savedUser = new User("johndoe", "john@example.com", "encodedPassword", "John Doe");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken(any())).thenReturn("mock-jwt-token");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("johndoe", res.getUsername());
    }

    @Test
    void testMultiUserExpenseIsolation() {
        Long userAId = 1L;
        Long userBId = 2L;

        User userA = new User("alice", "alice@example.com", "pass", "Alice");
        userA.setId(userAId);

        Expense expenseA = new Expense(new BigDecimal("50.00"), LocalDate.now(), "Lunch", "Food", userA);
        expenseA.setId(100L);

        // Mock: user A fetches their expense -> found
        when(expenseRepository.findByIdAndUserId(100L, userAId)).thenReturn(Optional.of(expenseA));

        // Mock: user B attempts to fetch user A's expense -> not found (strictly isolated)
        when(expenseRepository.findByIdAndUserId(100L, userBId)).thenReturn(Optional.empty());

        // Alice accesses her expense -> SUCCESS
        var retrieved = expenseService.getExpenseById(userAId, 100L);
        assertEquals("Lunch", retrieved.getDescription());

        // Bob attempts to access Alice's expense -> throws ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpenseById(userBId, 100L);
        });

        // Bob attempts to delete Alice's expense -> throws ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.deleteExpense(userBId, 100L);
        });
    }
}
