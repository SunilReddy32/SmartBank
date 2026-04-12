package com.smartbank.service;

import com.smartbank.dto.MonthlyBreakdownDTO;
import com.smartbank.dto.SpendingAnalyticsResponseDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    // ✅ GET SPENDING ANALYTICS
    // months param: how many months back to include (default 6, max 12)
    public SpendingAnalyticsResponseDTO getAnalytics(Long userId, int months) {

        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own analytics");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Account> accounts = accountRepository.findByUser(user);

        if (accounts.isEmpty()) {
            SpendingAnalyticsResponseDTO empty = new SpendingAnalyticsResponseDTO();
            empty.setUserId(userId);
            empty.setUserName(user.getName());
            empty.setPeriod("Last " + months + " months");
            empty.setBreakdownByType(new HashMap<>());
            empty.setMonthlyBreakdown(new ArrayList<>());
            return empty;
        }

        LocalDateTime from = LocalDateTime.now().minusMonths(months);
        LocalDateTime to = LocalDateTime.now();

        // ── Summary totals ────────────────────────────────────────────────────
        List<Object[]> typeSums = transactionRepository
                .sumByTypeForAccountsInRange(accounts, from, to);

        Map<String, Double> breakdownByType = new LinkedHashMap<>();
        double totalDeposited = 0, totalWithdrawn = 0, totalTransferred = 0;
        double totalInterest = 0, totalEmi = 0;

        for (Object[] row : typeSums) {
            String type = row[0].toString();
            double sum = ((Number) row[1]).doubleValue();
            sum = Math.round(sum * 100.0) / 100.0;
            breakdownByType.put(type, sum);

            switch (type) {
                case "DEPOSIT"          -> totalDeposited = sum;
                case "WITHDRAW"         -> totalWithdrawn = sum;
                case "TRANSFER"         -> totalTransferred = sum;
                case "INTEREST_CREDIT"  -> totalInterest = sum;
                case "EMI_PAYMENT"      -> totalEmi = sum;
            }
        }

        double netFlow = Math.round((totalDeposited - totalWithdrawn - totalTransferred) * 100.0) / 100.0;

        // ── Monthly breakdown ─────────────────────────────────────────────────
        List<Object[]> monthlyRows = transactionRepository
                .monthlyBreakdownForAccounts(accounts, from);

        // Group by year+month
        Map<String, MonthlyBreakdownDTO> monthMap = new LinkedHashMap<>();

        for (Object[] row : monthlyRows) {
            int month = ((Number) row[0]).intValue();
            int year  = ((Number) row[1]).intValue();
            String type = row[2].toString();
            double sum  = Math.round(((Number) row[3]).doubleValue() * 100.0) / 100.0;

            String key = year + "-" + String.format("%02d", month);

            monthMap.computeIfAbsent(key, k -> {
                MonthlyBreakdownDTO dto = new MonthlyBreakdownDTO();
                dto.setYear(year);
                dto.setMonth(month);
                dto.setMonthLabel(Month.of(month)
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year);
                dto.setByType(new LinkedHashMap<>());
                return dto;
            });

            monthMap.get(key).getByType().put(type, sum);
        }

        // ── Assemble response ─────────────────────────────────────────────────
        SpendingAnalyticsResponseDTO response = new SpendingAnalyticsResponseDTO();
        response.setUserId(userId);
        response.setUserName(user.getName());
        response.setPeriod("Last " + months + " months");
        response.setTotalDeposited(totalDeposited);
        response.setTotalWithdrawn(totalWithdrawn);
        response.setTotalTransferred(totalTransferred);
        response.setTotalInterestEarned(totalInterest);
        response.setTotalEmiPaid(totalEmi);
        response.setNetFlow(netFlow);
        response.setBreakdownByType(breakdownByType);
        response.setMonthlyBreakdown(new ArrayList<>(monthMap.values()));

        return response;
    }
}