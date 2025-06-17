package com.shop.orders.service;

import com.shop.orders.config.KafkaConfig;
import com.shop.orders.dto.PaymentStatusEvent;
import com.shop.orders.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.PAYMENT_STATUS_TOPIC, groupId = "orders-group")
    public void handlePaymentStatus(String message) {
        try {
            PaymentStatusEvent event = objectMapper.readValue(message, PaymentStatusEvent.class);
            orderService.updateOrderStatus(event.getOrderId(), event.getStatus());
            System.out.println("Order " + event.getOrderId() + " status updated to " + event.getStatus() + ". Message: " + event.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to process payment status event: " + e.getMessage());
        }
    }
} 