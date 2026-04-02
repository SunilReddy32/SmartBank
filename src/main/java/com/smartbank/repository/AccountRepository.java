package com.smartbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ✅ NEW: Get all accounts belonging to a user
    List<Account> findByUser(User user);

    // ✅ NEW: Check if an auto-generated account number already exists (collision check)
    boolean existsByAccountNumber(String accountNumber);
}