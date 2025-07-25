package com.shop.payments.service;

import com.shop.payments.config.KafkaConfig;
import com.shop.payments.dto.PaymentRequestEvent;
import com.shop.payments.dto.PaymentStatusEvent;
import com.shop.payments.model.Account;
import com.shop.payments.model.OrderStatus;
import com.shop.payments.model.OutboxEvent;
import com.shop.payments.repository.AccountRepository;
import com.shop.payments.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class PaymentRequestListener {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.PAYMENT_REQUEST_TOPIC, groupId = "payments-group")
    @Transactional
    public void handlePaymentRequest(String message) {
        PaymentStatusEvent paymentStatusEvent = null;
        try {
            PaymentRequestEvent request = objectMapper.readValue(message, PaymentRequestEvent.class);

            Optional<Account> accountOptional = accountRepository.findByUserId(request.getUserId());

            if (accountOptional.isEmpty()) {
                paymentStatusEvent = new PaymentStatusEvent(request.getOrderId(), OrderStatus.CANCELLED, "Account not found.");
            } else {
                Account account = accountOptional.get();
                if (account.getBalance().compareTo(request.getAmount()) < 0) {
                    paymentStatusEvent = new PaymentStatusEvent(request.getOrderId(), OrderStatus.CANCELLED, "Insufficient funds.");
                } else {
                    account.setBalance(account.getBalance().subtract(request.getAmount()));
                    accountRepository.save(account);
                    paymentStatusEvent = new PaymentStatusEvent(request.getOrderId(), OrderStatus.FINISHED, "Payment successful.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process payment request: " + e.getMessage());
            if (paymentStatusEvent == null) {
                paymentStatusEvent = new PaymentStatusEvent(null, OrderStatus.CANCELLED, "Internal payment processing error.");
            }
        } finally {
            if (paymentStatusEvent != null) {
                try {
                    String eventData = objectMapper.writeValueAsString(paymentStatusEvent);
                    OutboxEvent outboxEvent = new OutboxEvent(
                            String.valueOf(paymentStatusEvent.getOrderId()),
                            "Payment",
                            "PAYMENT_STATUS_UPDATE",
                            eventData
                    );
                    outboxRepository.save(outboxEvent);
                } catch (Exception e) {
                    System.err.println("Failed to create outbox event for payment status: " + e.getMessage());
                }
            }
        }
    }
} 