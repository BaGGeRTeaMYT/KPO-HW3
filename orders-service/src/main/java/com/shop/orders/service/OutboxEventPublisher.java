package com.shop.orders.service;

import com.shop.orders.config.KafkaConfig;
import com.shop.orders.model.OutboxEvent;
import com.shop.orders.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxEventPublisher {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                // Determine routing key based on event type if needed
                String topic = KafkaConfig.PAYMENT_REQUEST_TOPIC; // Default for now
                
                kafkaTemplate.send(topic, event.getEventData());
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                System.err.println("Failed to publish outbox event: " + event.getId() + ", Error: " + e.getMessage());
                // Optionally, handle retries or move to a dead-letter queue
            }
        }
    }
} 