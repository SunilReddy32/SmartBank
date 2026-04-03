package com.smartbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String name) {
        send(toEmail, "Welcome to SmartBank!",
            "Hi " + name + ",\n\nWelcome to SmartBank! Your account has been created successfully.\n\nRegards,\nSmartBank Team");
    }

    public void sendLoginNotification(String toEmail, String name) {
        send(toEmail, "SmartBank Login Alert",
            "Hi " + name + ",\n\nYou just logged into your SmartBank account.\nIf this wasn't you, please contact support immediately.\n\nRegards,\nSmartBank Team");
    }

    public void sendDepositEmail(String toEmail, String name,
                                  String accountNumber, double amount, double newBalance) {
        send(toEmail, "SmartBank — Deposit Successful",
            "Hi " + name + ",\n\nA deposit of ₹" + amount + " has been credited.\n\n" +
            "Account Number : " + accountNumber + "\nAmount Deposited: ₹" + amount +
            "\nCurrent Balance : ₹" + newBalance + "\n\nRegards,\nSmartBank Team");
    }

    public void sendWithdrawalEmail(String toEmail, String name,
                                     String accountNumber, double amount, double newBalance) {
        send(toEmail, "SmartBank — Withdrawal Alert",
            "Hi " + name + ",\n\n₹" + amount + " has been debited from your account.\n\n" +
            "Account Number : " + accountNumber + "\nAmount Withdrawn: ₹" + amount +
            "\nCurrent Balance : ₹" + newBalance + "\n\nRegards,\nSmartBank Team");
    }

    public void sendTransferSentEmail(String toEmail, String name,
                                       String fromAccount, String toAccount,
                                       double amount, double newBalance) {
        send(toEmail, "SmartBank — Transfer Sent",
            "Hi " + name + ",\n\n₹" + amount + " transferred from your account.\n\n" +
            "From Account : " + fromAccount + "\nTo Account   : " + toAccount +
            "\nAmount       : ₹" + amount + "\nBalance Left : ₹" + newBalance +
            "\n\nRegards,\nSmartBank Team");
    }

    public void sendTransferReceivedEmail(String toEmail, String name,
                                           String fromAccount, String toAccount,
                                           double amount, double newBalance) {
        send(toEmail, "SmartBank — Money Received",
            "Hi " + name + ",\n\n₹" + amount + " credited to your account.\n\n" +
            "From Account : " + fromAccount + "\nTo Account   : " + toAccount +
            "\nAmount       : ₹" + amount + "\nNew Balance  : ₹" + newBalance +
            "\n\nRegards,\nSmartBank Team");
    }

    public void sendInterestCreditEmail(String toEmail, String name,
                                         String accountNumber, double interestAmount, double newBalance) {
        send(toEmail, "SmartBank — Monthly Interest Credited",
            "Hi " + name + ",\n\nYour monthly savings interest has been credited!\n\n" +
            "Account Number  : " + accountNumber + "\nInterest Earned : ₹" + interestAmount +
            "\nNew Balance     : ₹" + newBalance + "\n\nRegards,\nSmartBank Team");
    }

    // ✅ LOAN: Application received
    public void sendLoanApplicationEmail(String toEmail, String name,
                                          double loanAmount, int tenureMonths) {
        send(toEmail, "SmartBank — Loan Application Received",
            "Hi " + name + ",\n\nYour loan application has been received and is under review.\n\n" +
            "Loan Amount  : ₹" + loanAmount + "\n" +
            "Tenure       : " + tenureMonths + " months\n\n" +
            "We will notify you once a decision is made.\n\nRegards,\nSmartBank Team");
    }

    // ✅ LOAN: Approved — includes EMI details
    public void sendLoanApprovedEmail(String toEmail, String name,
                                       double loanAmount, int tenureMonths,
                                       double emiAmount, double totalPayable,
                                       String accountNumber) {
        send(toEmail, "SmartBank — Loan Approved! 🎉",
            "Hi " + name + ",\n\nCongratulations! Your loan has been approved and disbursed.\n\n" +
            "Loan Amount    : ₹" + loanAmount + "\n" +
            "Tenure         : " + tenureMonths + " months\n" +
            "Monthly EMI    : ₹" + String.format("%.2f", emiAmount) + "\n" +
            "Total Payable  : ₹" + String.format("%.2f", totalPayable) + "\n" +
            "Disbursed To   : Account " + accountNumber + "\n\n" +
            "Please pay your EMI on time to avoid penalties.\n\nRegards,\nSmartBank Team");
    }

    // ✅ LOAN: Rejected
    public void sendLoanRejectedEmail(String toEmail, String name,
                                       double loanAmount, String reason) {
        send(toEmail, "SmartBank — Loan Application Update",
            "Hi " + name + ",\n\nWe regret to inform you that your loan application has been rejected.\n\n" +
            "Loan Amount  : ₹" + loanAmount + "\n" +
            "Reason       : " + reason + "\n\n" +
            "You may re-apply after addressing the above reason.\n\nRegards,\nSmartBank Team");
    }

    // ✅ LOAN: EMI payment confirmation
    public void sendEmiPaymentEmail(String toEmail, String name,
                                     Long loanId, int emiNumber, int totalEmis,
                                     double emiAmount, double accountBalance) {
        String footer = (emiNumber == totalEmis)
                ? "\n🎉 Congratulations! You have paid off your loan completely!"
                : "\n" + (totalEmis - emiNumber) + " EMIs remaining.";

        send(toEmail, "SmartBank — EMI Payment Successful",
            "Hi " + name + ",\n\nYour EMI payment has been processed successfully.\n\n" +
            "Loan ID       : " + loanId + "\n" +
            "EMI Number    : " + emiNumber + " of " + totalEmis + "\n" +
            "Amount Paid   : ₹" + String.format("%.2f", emiAmount) + "\n" +
            "Account Balance: ₹" + String.format("%.2f", accountBalance) +
            footer + "\n\nRegards,\nSmartBank Team");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("📧 Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}