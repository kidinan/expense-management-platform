package com.example.expensetracker;

import com.example.expensetracker.controller.ExpenseController;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.PagedExpenseResponse;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.GlobalExceptionHandler;
import com.example.expensetracker.security.UserPrincipal;
import com.example.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    private ObjectMapper objectMapper;
    private UserPrincipal testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User("testuser", "test@example.com", "pass", "Test User");
        user.setId(1L);
        testUser = UserPrincipal.create(user);

        // Custom argument resolver to inject the mock UserPrincipal for @AuthenticationPrincipal
        HandlerMethodArgumentResolver userPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(expenseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(userPrincipalResolver)
                .build();
    }

    @Test
    void testCreateExpenseValidationFailure() throws Exception {
        // Invalid expense: missing description and negative amount
        ExpenseRequest invalidRequest = new ExpenseRequest(
                new BigDecimal("-15.00"),
                LocalDate.now(),
                "",
                "Food"
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testCreateExpenseSuccess() throws Exception {
        ExpenseRequest validRequest = new ExpenseRequest(
                new BigDecimal("45.00"),
                LocalDate.now(),
                "Team Lunch",
                "Food"
        );

        ExpenseResponse mockResponse = new ExpenseResponse(
                10L,
                new BigDecimal("45.00"),
                LocalDate.now(),
                "Team Lunch",
                "Food",
                java.time.LocalDateTime.now()
        );

        when(expenseService.createExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.description").value("Team Lunch"));
    }

    @Test
    void testExportExpensesToCsv() throws Exception {
        byte[] csvOutput = "ID,Date,Category,Description,Amount\n1,2026-09-18,Food,Lunch,12.50\n".getBytes();
        when(expenseService.exportExpensesToCsv(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenReturn(csvOutput);

        mockMvc.perform(get("/api/expenses/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"expenses.csv\""))
                .andExpect(content().bytes(csvOutput));
    }

    @Test
    void testGetRecentExpenses() throws Exception {
        when(expenseService.getRecentExpenses(eq(1L), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/expenses/recent?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
