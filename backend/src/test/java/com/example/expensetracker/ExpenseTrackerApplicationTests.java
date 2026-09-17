package com.example.expensetracker;

import com.example.expensetracker.dto.AuthRequest;
import com.example.expensetracker.dto.AuthResponse;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExpenseTrackerApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private ExpenseService expenseService;

    @Test
    void testUserRegistrationAndLogin() {
        RegisterRequest registerReq = new RegisterRequest(
                "john_doe", "john@example.com", "secret123", "John Doe");
        AuthResponse registerRes = authService.register(registerReq);

        assertNotNull(registerRes.getToken());
        assertEquals("john_doe", registerRes.getUsername());

        AuthResponse loginRes = authService.login(new AuthRequest("john_doe", "secret123"));
        assertNotNull(loginRes.getToken());
        assertEquals(registerRes.getId(), loginRes.getId());
    }

    @Test
    void testMultiUserExpenseIsolation() {
        // Create User A
        RegisterRequest userAReq = new RegisterRequest(
                "alice", "alice@example.com", "password123", "Alice Smith");
        AuthResponse userA = authService.register(userAReq);

        // Create User B
        RegisterRequest userBReq = new RegisterRequest(
                "bob", "bob@example.com", "password123", "Bob Jones");
        AuthResponse userB = authService.register(userBReq);

        // User A creates an expense
        ExpenseRequest expenseReq = new ExpenseRequest(
                new BigDecimal("45.50"), LocalDate.now(), "Grocery Shopping", "Food");
        var expenseA = expenseService.createExpense(userA.getId(), expenseReq);

        // User A can see their own expense
        var aliceExpenses = expenseService.getExpenses(userA.getId(), null, null, null, null, "date", "desc");
        assertEquals(1, aliceExpenses.size());
        assertEquals("Grocery Shopping", aliceExpenses.get(0).getDescription());

        // User B CANNOT see User A's expense
        var bobExpenses = expenseService.getExpenses(userB.getId(), null, null, null, null, "date", "desc");
        assertEquals(0, bobExpenses.size());

        // User B attempts to access User A's expense directly -> must throw exception
        assertThrows(RuntimeException.class, () -> {
            expenseService.getExpenseById(userB.getId(), expenseA.getId());
        });

        // User B attempts to delete User A's expense directly -> must throw exception
        assertThrows(RuntimeException.class, () -> {
            expenseService.deleteExpense(userB.getId(), expenseA.getId());
        });
    }
}
