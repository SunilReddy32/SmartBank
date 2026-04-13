package com.smartbank.repository;

import com.smartbank.entity.Account;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount(Account account, Pageable pageable);

    Page<Transaction> findByAccountAndType(Account account, String type, Pageable pageable);

    // For dashboard — recent txns across all accounts
    List<Transaction> findByAccountInOrderByCreatedAtDesc(List<Account> accounts, Pageable pageable);

    // ✅ FEATURE: Daily limit check
    // Sum of all WITHDRAW amounts for an account today
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account = :account AND t.type = :type " +
           "AND t.createdAt >= :startOfDay")
    double sumAmountByAccountAndTypeAndCreatedAtAfter(
            @Param("account") Account account,
            @Param("type") TransactionType type,
            @Param("startOfDay") LocalDateTime startOfDay
    );

    // ✅ FEATURE: Account statement — all txns for an account in a date range
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.account = :account " +
           "AND t.createdAt BETWEEN :from AND :to " +
           "ORDER BY t.createdAt ASC")
    List<Transaction> findByAccountAndDateRange(
            @Param("account") Account account,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // ✅ FEATURE: Spending analytics — sum by type for an account in a date range
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
           "WHERE t.account IN :accounts " +
           "AND t.createdAt BETWEEN :from AND :to " +
           "GROUP BY t.type")
    List<Object[]> sumByTypeForAccountsInRange(
            @Param("accounts") List<Account> accounts,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // ✅ FEATURE: Monthly breakdown — total per month for analytics chart
    @Query("SELECT MONTH(t.createdAt), YEAR(t.createdAt), t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.account IN :accounts " +
           "AND t.createdAt >= :from " +
           "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt), t.type " +
           "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> monthlyBreakdownForAccounts(
            @Param("accounts") List<Account> accounts,
            @Param("from") LocalDateTime from
    );
}