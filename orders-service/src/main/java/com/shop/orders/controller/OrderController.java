package com.shop.orders.controller;

import com.shop.orders.dto.CreateOrderRequest;
import com.shop.orders.dto.OrderResponse;
import com.shop.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Orders", description = "API для управления заказами")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Создать заказ", description = "Создает новый заказ для пользователя")
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader("X-User-ID") Long userId,
            @Parameter(description = "Данные заказа", required = true)
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Получить список заказов", description = "Возвращает все заказы пользователя")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Получить заказ по ID", description = "Возвращает информацию о конкретном заказе")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "ID заказа", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        OrderResponse order = orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(order);
    }
} 