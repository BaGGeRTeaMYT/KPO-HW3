package com.shop.orders.service;

import com.shop.orders.dto.CreateOrderRequest;
import com.shop.orders.dto.OrderResponse;
import com.shop.orders.dto.PaymentRequestEvent;
import com.shop.orders.model.Order;
import com.shop.orders.model.OrderStatus;
import com.shop.orders.model.OutboxEvent;
import com.shop.orders.repository.OrderRepository;
import com.shop.orders.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OutboxRepository outboxRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Order order = new Order(userId, request.getAmount());
        order = orderRepository.save(order);
        
        try {
            String eventData = objectMapper.writeValueAsString(new PaymentRequestEvent(order.getId(), userId, request.getAmount()));
            OutboxEvent outboxEvent = new OutboxEvent(
                order.getId().toString(),
                "Order",
                "ORDER_CREATED",
                eventData
            );
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create outbox event", e);
        }
        
        return convertToResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToResponse(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse convertToResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getUserId(),
            order.getAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
} 