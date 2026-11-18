package com.loyalty.transaction.config;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import com.loyalty.transaction.entity.CustomerTransaction;
import com.loyalty.transaction.entity.TransactionItem;
import com.loyalty.transaction.repository.CustomerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CustomerTransactionRepository transactionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (transactionRepository.count() == 0) {
            log.info("Seeding initial customer transactions...");

            createTxn("TXN-20261101-1001", 1L, "CUST-10001", new BigDecimal("125.50"), "Whole Foods Market",
                    "Downtown SF", TransactionCategory.GROCERY, PaymentMethod.CREDIT_CARD, true, 1882L,
                    LocalDateTime.now().minusDays(14), "Weekly family grocery haul");

            createTxn("TXN-20261105-1002", 1L, "CUST-10001", new BigDecimal("499.00"), "Best Buy Electronics",
                    "Market St Store", TransactionCategory.ELECTRONICS, PaymentMethod.CREDIT_CARD, true, 7485L,
                    LocalDateTime.now().minusDays(10), "4K Monitor purchase");

            createTxn("TXN-20261110-1003", 2L, "CUST-10002", new BigDecimal("1250.00"), "Delta Air Lines",
                    "Online Booking", TransactionCategory.TRAVEL, PaymentMethod.CREDIT_CARD, true, 25000L,
                    LocalDateTime.now().minusDays(6), "Business flight roundtrip");

            createTxn("TXN-20261112-1004", 3L, "CUST-10003", new BigDecimal("85.20"), "Nordstrom Department",
                    "Pine Mall", TransactionCategory.RETAIL, PaymentMethod.DEBIT_CARD, true, 1065L,
                    LocalDateTime.now().minusDays(4), "Clothing and accessories");

            createTxn("TXN-20261114-1005", 4L, "CUST-10004", new BigDecimal("45.00"), "Barnes & Noble Books",
                    "City Center", TransactionCategory.RETAIL, PaymentMethod.CASH, true, 450L,
                    LocalDateTime.now().minusDays(2), "Technical books");

            log.info("Initial customer transactions seeded successfully.");
        }
    }

    private void createTxn(String ref, Long customerId, String custNumber, BigDecimal amount, String merchant,
                           String store, TransactionCategory cat, PaymentMethod payment, boolean processed, Long points,
                           LocalDateTime date, String note) {
        CustomerTransaction tx = CustomerTransaction.builder()
                .transactionReference(ref)
                .customerId(customerId)
                .customerNumber(custNumber)
                .amount(amount)
                .currency("USD")
                .merchantName(merchant)
                .storeLocation(store)
                .category(cat)
                .paymentMethod(payment)
                .status("COMPLETED")
                .processedForPoints(processed)
                .pointsAwarded(points)
                .transactionDate(date)
                .notes(note)
                .build();

        TransactionItem item = TransactionItem.builder()
                .transaction(tx)
                .itemName(merchant + " Purchase Item")
                .quantity(1)
                .unitPrice(amount)
                .totalPrice(amount)
                .build();
        tx.setItems(List.of(item));

        transactionRepository.save(tx);
    }
}
