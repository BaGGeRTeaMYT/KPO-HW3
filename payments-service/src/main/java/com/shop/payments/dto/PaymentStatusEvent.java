package com.shop.payments.dto;

import com.shop.payments.model.OrderStatus;
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