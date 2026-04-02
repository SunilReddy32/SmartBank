package com.smartbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(User user);

    boolean existsByAccountNumber(String accountNumber);

    // ✅ NEW: look up account by its account number — used for transfer by account number
    Optional<Account> findByAccountNumber(String accountNumber);
}