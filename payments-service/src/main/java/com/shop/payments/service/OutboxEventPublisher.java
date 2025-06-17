package com.shop.payments.service;

import com.shop.payments.config.RabbitMQConfig;
import com.shop.payments.model.OutboxEvent;
import com.shop.payments.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxEventPublisher {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                // Determine routing key based on event type if needed
                String routingKey = RabbitMQConfig.PAYMENT_STATUS_ROUTING_KEY; // Default for now
                
                rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENTS_EXCHANGE, routingKey, event.getEventData());
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                System.err.println("Failed to publish outbox event: " + event.getId() + ", Error: " + e.getMessage());
                // Optionally, handle retries or move to a dead-letter queue
            }
        }
    }
} 