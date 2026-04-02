package com.smartbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbank.entity.Account;
import com.smartbank.entity.AccountType;
import com.smartbank.entity.User;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(User user);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    // ✅ NEW: used by InterestSchedulerService to find all SAVINGS accounts
    List<Account> findByAccountType(AccountType accountType);
}