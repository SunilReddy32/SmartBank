package com.smartbank.repository;

import com.smartbank.entity.Loan;
import com.smartbank.entity.LoanStatus;
import com.smartbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // All loans for a specific user
    List<Loan> findByUser(User user);

    // All loans with a specific status — used by admin to see PENDING applications
    List<Loan> findByStatus(LoanStatus status);

    // All loans for a user with a specific status
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
}