package com.smartbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbank.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
