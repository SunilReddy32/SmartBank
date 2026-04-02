package com.smartbank.service;

import com.smartbank.entity.Account;
import com.smartbank.entity.AccountType;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.TransactionType;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestSchedulerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    // Annual interest rate loaded from application.properties
    // Default: 4.0% per year → 0.333% per month
    @Value("${smartbank.interest.annual-rate:4.0}")
    private double annualInterestRate;

    // ✅ Runs at midnight on the 1st of every month
    // Cron: second minute hour day-of-month month day-of-week
    // To test immediately, change to: @Scheduled(fixedDelay = 30000) — runs every 30 seconds
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void creditMonthlyInterest() {

        log.info("⏰ Interest scheduler triggered — processing all SAVINGS accounts...");

        double monthlyRate = annualInterestRate / 100.0 / 12.0;

        // Only SAVINGS accounts earn interest
        List<Account> savingsAccounts = accountRepository.findByAccountType(AccountType.SAVINGS);

        int credited = 0;

        for (Account account : savingsAccounts) {

            if (account.getBalance() <= 0) continue; // skip zero-balance accounts

            double interestAmount = account.getBalance() * monthlyRate;
            interestAmount = Math.round(interestAmount * 100.0) / 100.0; // round to 2 decimal places

            // Credit interest to balance
            account.setBalance(account.getBalance() + interestAmount);
            accountRepository.save(account);

            // Record it as a transaction so it shows in history
            Transaction interestTx = new Transaction();
            interestTx.setType(TransactionType.INTEREST_CREDIT);
            interestTx.setAmount(interestAmount);
            interestTx.setAccount(account);
            transactionRepository.save(interestTx);

            // Notify the user via email
            emailService.sendInterestCreditEmail(
                    account.getUser().getEmail(),
                    account.getUser().getName(),
                    account.getAccountNumber(),
                    interestAmount,
                    account.getBalance()
            );

            credited++;
            log.info("✅ Credited ₹{} interest to account {} (new balance: {})",
                    interestAmount, account.getAccountNumber(), account.getBalance());
        }

        log.info("✅ Interest scheduler done — credited to {} accounts.", credited);
    }
}