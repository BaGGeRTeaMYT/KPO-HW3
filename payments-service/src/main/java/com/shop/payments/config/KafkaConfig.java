package com.shop.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String PAYMENT_REQUEST_TOPIC = "payment.request";
    public static final String PAYMENT_STATUS_TOPIC = "payment.status";

    @Bean
    public NewTopic paymentRequestTopic() {
        return TopicBuilder.name(PAYMENT_REQUEST_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentStatusTopic() {
        return TopicBuilder.name(PAYMENT_STATUS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
} 