package com.shop.orders.dto;

import com.shop.orders.model.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {
    private Long orderId;
    private OrderStatus status;
    private String message;
} 