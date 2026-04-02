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

    // ✅ Registration welcome email
    public void sendWelcomeEmail(String toEmail, String name) {
        send(toEmail,
            "Welcome to SmartBank!",
            "Hi " + name + ",\n\n" +
            "Welcome to SmartBank! Your account has been created successfully.\n\n" +
            "You can now log in and start banking.\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Login notification
    public void sendLoginNotification(String toEmail, String name) {
        send(toEmail,
            "SmartBank Login Alert",
            "Hi " + name + ",\n\n" +
            "You just logged into your SmartBank account.\n" +
            "If this wasn't you, please contact support immediately.\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Deposit confirmation
    public void sendDepositEmail(String toEmail, String name,
                                  String accountNumber, double amount, double newBalance) {
        send(toEmail,
            "SmartBank — Deposit Successful",
            "Hi " + name + ",\n\n" +
            "A deposit of ₹" + amount + " has been credited to your account.\n\n" +
            "Account Number : " + accountNumber + "\n" +
            "Amount Deposited: ₹" + amount + "\n" +
            "Current Balance : ₹" + newBalance + "\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Withdrawal confirmation
    public void sendWithdrawalEmail(String toEmail, String name,
                                     String accountNumber, double amount, double newBalance) {
        send(toEmail,
            "SmartBank — Withdrawal Alert",
            "Hi " + name + ",\n\n" +
            "₹" + amount + " has been debited from your account.\n\n" +
            "Account Number : " + accountNumber + "\n" +
            "Amount Withdrawn: ₹" + amount + "\n" +
            "Current Balance : ₹" + newBalance + "\n\n" +
            "If this wasn't you, contact support immediately.\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Transfer sent (sender)
    public void sendTransferSentEmail(String toEmail, String name,
                                       String fromAccountNumber, String toAccountNumber,
                                       double amount, double newBalance) {
        send(toEmail,
            "SmartBank — Transfer Sent",
            "Hi " + name + ",\n\n" +
            "₹" + amount + " has been transferred from your account.\n\n" +
            "From Account : " + fromAccountNumber + "\n" +
            "To Account   : " + toAccountNumber + "\n" +
            "Amount       : ₹" + amount + "\n" +
            "Balance Left : ₹" + newBalance + "\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Transfer received (receiver)
    public void sendTransferReceivedEmail(String toEmail, String name,
                                           String fromAccountNumber, String toAccountNumber,
                                           double amount, double newBalance) {
        send(toEmail,
            "SmartBank — Money Received",
            "Hi " + name + ",\n\n" +
            "₹" + amount + " has been credited to your account.\n\n" +
            "From Account : " + fromAccountNumber + "\n" +
            "To Account   : " + toAccountNumber + "\n" +
            "Amount       : ₹" + amount + "\n" +
            "New Balance  : ₹" + newBalance + "\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // ✅ Monthly interest credit
    public void sendInterestCreditEmail(String toEmail, String name,
                                         String accountNumber, double interestAmount, double newBalance) {
        send(toEmail,
            "SmartBank — Monthly Interest Credited",
            "Hi " + name + ",\n\n" +
            "Your monthly savings interest has been credited!\n\n" +
            "Account Number  : " + accountNumber + "\n" +
            "Interest Earned : ₹" + interestAmount + "\n" +
            "New Balance     : ₹" + newBalance + "\n\n" +
            "Regards,\nSmartBank Team"
        );
    }

    // 🔧 Internal send helper — catches exceptions so email failure never breaks transactions
    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("📧 Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            // Log but don't rethrow — a failed email should never roll back a transaction
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}