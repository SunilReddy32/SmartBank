package com.smartbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount(Account account, Pageable pageable);

    Page<Transaction> findByAccountAndType(Account account, String type, Pageable pageable);

    // ✅ NEW: get recent transactions across multiple accounts — used by dashboard
    // Spring Data derives the full query from the method name automatically
    List<Transaction> findByAccountInOrderByCreatedAtDesc(List<Account> accounts, Pageable pageable);
}