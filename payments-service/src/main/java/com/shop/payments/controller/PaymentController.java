package com.shop.payments.controller;

import com.shop.payments.dto.AccountResponse;
import com.shop.payments.dto.BalanceResponse;
import com.shop.payments.dto.DepositRequest;
import com.shop.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Payments", description = "API для управления платежами и счетами")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/accounts")
    @Operation(summary = "Создать счет", description = "Создает новый счет для пользователя")
    public ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        AccountResponse account = paymentService.createAccount(userId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/accounts/deposit")
    @Operation(summary = "Пополнить счет", description = "Пополняет счет пользователя на указанную сумму")
    public ResponseEntity<BalanceResponse> depositMoney(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Данные пополнения", required = true)
            @Valid @RequestBody DepositRequest request) {
        
        BalanceResponse balance = paymentService.depositMoney(userId, request.getAmount());
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/accounts/balance")
    @Operation(summary = "Получить баланс", description = "Возвращает текущий баланс счета пользователя")
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        
        BalanceResponse balance = paymentService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }
} 