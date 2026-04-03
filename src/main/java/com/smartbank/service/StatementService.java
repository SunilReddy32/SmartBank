package com.smartbank.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartbank.entity.Account;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.TransactionType;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Brand colours
    private static final DeviceRgb BRAND_BLUE   = new DeviceRgb(0, 84, 166);
    private static final DeviceRgb BRAND_LIGHT   = new DeviceRgb(235, 243, 255);
    private static final DeviceRgb CREDIT_GREEN  = new DeviceRgb(0, 128, 0);
    private static final DeviceRgb DEBIT_RED     = new DeviceRgb(200, 0, 0);
    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    /**
     * Generates a PDF bank statement for the given account and date range.
     * Returns raw bytes — the controller streams them as application/pdf.
     *
     * @param accountId  account to generate statement for
     * @param fromYear   start year
     * @param fromMonth  start month (1–12)
     * @param toYear     end year
     * @param toMonth    end month (1–12)
     */
    public byte[] generateStatement(Long accountId,
                                     int fromYear, int fromMonth,
                                     int toYear, int toMonth) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Ownership check
        User loggedIn = getLoggedInUser();
        if (!account.getUser().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("Unauthorized: This account does not belong to you");
        }

        LocalDateTime from = LocalDateTime.of(fromYear, fromMonth, 1, 0, 0);
        LocalDateTime to   = LocalDateTime.of(toYear, toMonth,
                java.time.YearMonth.of(toYear, toMonth).lengthOfMonth(), 23, 59, 59);

        List<Transaction> transactions = transactionRepository
                .findByAccountAndDateRange(account, from, to);

        // ── Build PDF in memory ───────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);
            doc.setMargins(36, 36, 36, 36);

            // ── Header ────────────────────────────────────────────────────────
            Paragraph bankName = new Paragraph("SmartBank")
                    .setFontSize(24).setBold()
                    .setFontColor(BRAND_BLUE)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(bankName);

            Paragraph subtitle = new Paragraph("Account Statement")
                    .setFontSize(13)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(subtitle);

            doc.add(new Paragraph("\n"));

            // ── Account info table ────────────────────────────────────────────
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(infoCell("Account Holder", account.getUser().getName()));
            infoTable.addCell(infoCell("Account Number", account.getAccountNumber()));
            infoTable.addCell(infoCell("Account Type", account.getAccountType().name()));
            infoTable.addCell(infoCell("Current Balance", "Rs. " + String.format("%.2f", account.getBalance())));
            infoTable.addCell(infoCell("Statement Period",
                    from.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " - " +
                    to.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
            infoTable.addCell(infoCell("Generated On",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))));

            doc.add(infoTable);
            doc.add(new Paragraph("\n"));

            // ── Transactions table header ─────────────────────────────────────
            Table txTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Date & Time", "Type", "Amount (Rs.)", "Balance"};
            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(BRAND_BLUE)
                        .setPadding(6);
                txTable.addHeaderCell(headerCell);
            }

            if (transactions.isEmpty()) {
                Cell noTx = new Cell(1, 4)
                        .add(new Paragraph("No transactions found for this period.")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(ColorConstants.GRAY))
                        .setPadding(12);
                txTable.addCell(noTx);
            } else {
                // Calculate running balance by going through all transactions
                // (simplified: we track credits/debits from the list)
                boolean alternateRow = false;
                double runningBalance = account.getBalance();

                // Reverse to compute running balance from current backwards is complex —
                // we'll show current balance and work backwards from the last transaction
                // For simplicity and accuracy, show transaction amount + final balance note
                for (int i = transactions.size() - 1; i >= 0; i--) {
                    Transaction tx = transactions.get(i);
                    boolean isCredit = isCredit(tx.getType());
                    if (isCredit) {
                        runningBalance -= tx.getAmount();
                    } else {
                        runningBalance += tx.getAmount();
                    }
                }

                for (Transaction tx : transactions) {
                    boolean isCredit = isCredit(tx.getType());
                    if (isCredit) {
                        runningBalance += tx.getAmount();
                    } else {
                        runningBalance -= tx.getAmount();
                    }

                    DeviceRgb rowBg = alternateRow ? BRAND_LIGHT : null;
                    alternateRow = !alternateRow;

                    Cell dateCell = new Cell()
                            .add(new Paragraph(tx.getCreatedAt().format(FMT)).setFontSize(9))
                            .setPadding(5);
                    Cell typeCell = new Cell()
                            .add(new Paragraph(formatType(tx.getType())).setFontSize(9))
                            .setPadding(5);
                    Cell amountCell = new Cell()
                            .add(new Paragraph(
                                (isCredit ? "+ " : "- ") + String.format("%.2f", tx.getAmount()))
                                .setFontSize(9)
                                .setFontColor(isCredit ? CREDIT_GREEN : DEBIT_RED)
                                .setBold())
                            .setPadding(5);
                    Cell balanceCell = new Cell()
                            .add(new Paragraph(String.format("%.2f", runningBalance)).setFontSize(9))
                            .setPadding(5);

                    if (rowBg != null) {
                        dateCell.setBackgroundColor(rowBg);
                        typeCell.setBackgroundColor(rowBg);
                        amountCell.setBackgroundColor(rowBg);
                        balanceCell.setBackgroundColor(rowBg);
                    }

                    txTable.addCell(dateCell);
                    txTable.addCell(typeCell);
                    txTable.addCell(amountCell);
                    txTable.addCell(balanceCell);
                }
            }

            doc.add(txTable);

            // ── Summary footer ────────────────────────────────────────────────
            doc.add(new Paragraph("\n"));

            double totalCredits = transactions.stream()
                    .filter(tx -> isCredit(tx.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double totalDebits = transactions.stream()
                    .filter(tx -> !isCredit(tx.getType()))
                    .mapToDouble(Transaction::getAmount).sum();

            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(50))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

            summaryTable.addCell(summaryCell("Total Credits", String.format("+ Rs. %.2f", totalCredits), CREDIT_GREEN));
            summaryTable.addCell(summaryCell("Total Debits", String.format("- Rs. %.2f", totalDebits), DEBIT_RED));
            summaryTable.addCell(summaryCell("Total Transactions", String.valueOf(transactions.size()), BRAND_BLUE));

            doc.add(summaryTable);

            // ── Footer ────────────────────────────────────────────────────────
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("This is a system-generated statement and does not require a signature.")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF statement: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isCredit(TransactionType type) {
        return type == TransactionType.DEPOSIT
                || type == TransactionType.INTEREST_CREDIT
                || type == TransactionType.LOAN_DISBURSEMENT;
    }

    private String formatType(TransactionType type) {
        return switch (type) {
            case DEPOSIT          -> "Deposit";
            case WITHDRAW         -> "Withdrawal";
            case TRANSFER         -> "Transfer";
            case INTEREST_CREDIT  -> "Interest";
            case LOAN_DISBURSEMENT -> "Loan Disbursed";
            case EMI_PAYMENT      -> "EMI Payment";
        };
    }

    private Cell infoCell(String label, String value) {
        return new Cell()
                .add(new Paragraph()
                        .add(new Text(label + ": ").setBold())
                        .add(new Text(value)))
                .setPadding(5)
                .setBorder(null);
    }

    private Cell summaryCell(String label, String value, DeviceRgb color) {
        return new Cell()
                .add(new Paragraph()
                        .add(new Text(label + ": ").setBold())
                        .add(new Text(value).setFontColor(color)))
                .setPadding(5);
    }
}