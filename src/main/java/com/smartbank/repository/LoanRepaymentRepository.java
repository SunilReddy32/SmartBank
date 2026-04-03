package com.smartbank.repository;

import com.smartbank.entity.Loan;
import com.smartbank.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    // All repayments for a loan, in order
    List<LoanRepayment> findByLoanOrderByEmiNumberAsc(Loan loan);

    // Count how many EMIs have been paid for a loan
    int countByLoan(Loan loan);
}