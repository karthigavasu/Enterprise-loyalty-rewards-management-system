package com.loyalty.transaction.service;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import com.loyalty.common.events.TransactionCreatedEvent;
import com.loyalty.transaction.dto.TransactionAnalyticsDto;
import com.loyalty.transaction.dto.TransactionCreateRequest;
import com.loyalty.transaction.dto.TransactionItemDto;
import com.loyalty.transaction.dto.TransactionResponse;
import com.loyalty.transaction.entity.CustomerTransaction;
import com.loyalty.transaction.entity.TransactionItem;
import com.loyalty.transaction.repository.CustomerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerTransactionService {

    private final CustomerTransactionRepository transactionRepository;
    private final TransactionKafkaProducerService producerService;

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        String reference = (request.getTransactionReference() != null && !request.getTransactionReference().isBlank())
                ? request.getTransactionReference().trim().toUpperCase()
                : generateTransactionReference();

        if (transactionRepository.existsByTransactionReference(reference)) {
            throw new IllegalArgumentException("Transaction with reference " + reference + " already exists (duplicate protection)");
        }

        LocalDateTime txnDate = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now();

        CustomerTransaction transaction = CustomerTransaction.builder()
                .transactionReference(reference)
                .customerId(request.getCustomerId())
                .customerNumber(request.getCustomerNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .merchantName(request.getMerchantName())
                .storeLocation(request.getStoreLocation() != null ? request.getStoreLocation() : "Online / Main Store")
                .category(request.getCategory() != null ? request.getCategory() : TransactionCategory.RETAIL)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CREDIT_CARD)
                .status("COMPLETED")
                .processedForPoints(false)
                .pointsAwarded(0L)
                .notes(request.getNotes())
                .transactionDate(txnDate)
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<TransactionItem> items = new ArrayList<>();
            for (TransactionItemDto dto : request.getItems()) {
                TransactionItem item = TransactionItem.builder()
                        .transaction(transaction)
                        .itemName(dto.getItemName())
                        .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                        .unitPrice(dto.getUnitPrice())
                        .totalPrice(dto.getTotalPrice() != null ? dto.getTotalPrice() : dto.getUnitPrice().multiply(java.math.BigDecimal.valueOf(dto.getQuantity() != null ? dto.getQuantity() : 1)))
                        .build();
                items.add(item);
            }
            transaction.setItems(items);
        }

        CustomerTransaction saved = transactionRepository.save(transaction);
        log.info("Saved transaction reference: {}, customerId: {}, amount: {}", reference, request.getCustomerId(), request.getAmount());

        // Publish Kafka event
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .correlationId(UUID.randomUUID().toString())
                .transactionId(saved.getId())
                .transactionReference(saved.getTransactionReference())
                .customerId(saved.getCustomerId())
                .customerNumber(saved.getCustomerNumber())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .merchantName(saved.getMerchantName())
                .storeLocation(saved.getStoreLocation())
                .category(saved.getCategory())
                .paymentMethod(saved.getPaymentMethod())
                .build();

        producerService.publishTransactionCreatedEvent(event);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        CustomerTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getByReference(String reference) {
        CustomerTransaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with reference: " + reference));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getCustomerTransactions(Long customerId, Pageable pageable) {
        return transactionRepository.findByCustomerIdOrderByTransactionDateDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchTransactions(
            Long customerId,
            String merchant,
            TransactionCategory category,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return transactionRepository.searchTransactions(customerId, merchant, category, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TransactionAnalyticsDto getAnalytics() {
        return TransactionAnalyticsDto.builder()
                .totalTransactions(transactionRepository.count())
                .totalVolume(transactionRepository.sumTotalCompletedVolume())
                .averageTransactionAmount(transactionRepository.avgCompletedTransactionAmount())
                .build();
    }

    @Transactional
    public void markPointsAwarded(Long transactionId, Long points) {
        transactionRepository.findById(transactionId).ifPresent(tx -> {
            tx.setProcessedForPoints(true);
            tx.setPointsAwarded(points);
            transactionRepository.save(tx);
            log.info("Updated transaction id {} with {} points awarded", transactionId, points);
        });
    }

    private String generateTransactionReference() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TXN-" + dateStr + "-" + rand;
    }

    public TransactionResponse mapToResponse(CustomerTransaction tx) {
        List<TransactionItemDto> itemDtos = null;
        if (tx.getItems() != null) {
            itemDtos = tx.getItems().stream()
                    .map(item -> TransactionItemDto.builder()
                            .id(item.getId())
                            .itemName(item.getItemName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return TransactionResponse.builder()
                .id(tx.getId())
                .transactionReference(tx.getTransactionReference())
                .customerId(tx.getCustomerId())
                .customerNumber(tx.getCustomerNumber())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .merchantName(tx.getMerchantName())
                .storeLocation(tx.getStoreLocation())
                .category(tx.getCategory())
                .paymentMethod(tx.getPaymentMethod())
                .status(tx.getStatus())
                .processedForPoints(tx.getProcessedForPoints())
                .pointsAwarded(tx.getPointsAwarded())
                .notes(tx.getNotes())
                .items(itemDtos)
                .transactionDate(tx.getTransactionDate())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
