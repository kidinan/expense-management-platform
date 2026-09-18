package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Strict multi-tenant check: find expense only if it belongs to this user
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findAllByUserId(Long userId, Sort sort);

    Page<Expense> findAllByUserId(Long userId, Pageable pageable);

    @Query(value = "SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND (:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (CAST(:startDate AS java.time.LocalDate) IS NULL OR e.date >= :startDate) " +
           "AND (CAST(:endDate AS java.time.LocalDate) IS NULL OR e.date <= :endDate)",
           countQuery = "SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
           "AND (:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (CAST(:startDate AS java.time.LocalDate) IS NULL OR e.date >= :startDate) " +
           "AND (CAST(:endDate AS java.time.LocalDate) IS NULL OR e.date <= :endDate)")
    Page<Expense> findWithFilters(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal sumTotalByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date >= :startDate AND e.date <= :endDate")
    BigDecimal sumByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Long countByUserId(Long userId);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategorySpending(@Param("userId") Long userId);
}
