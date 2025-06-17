package com.shop.payments.service;

import com.shop.payments.dto.AccountResponse;
import com.shop.payments.dto.BalanceResponse;
import com.shop.payments.model.Account;
import com.shop.payments.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {
    
    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(Long userId) {
        // Проверяем, существует ли уже счет для пользователя
        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Account already exists for user: " + userId);
        }
        
        Account account = new Account(userId);
        account = accountRepository.save(account);
        
        return convertToResponse(account);
    }

    @Transactional
    public BalanceResponse depositMoney(Long userId, BigDecimal amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for user: " + userId));
        
        // Атомарное обновление баланса с оптимистичной блокировкой
        account.setBalance(account.getBalance().add(amount));
        account = accountRepository.save(account);
        
        return new BalanceResponse(userId, account.getBalance());
    }

    public BalanceResponse getBalance(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for user: " + userId));
        
        return new BalanceResponse(userId, account.getBalance());
    }

    private AccountResponse convertToResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getUserId(),
            account.getBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
} 