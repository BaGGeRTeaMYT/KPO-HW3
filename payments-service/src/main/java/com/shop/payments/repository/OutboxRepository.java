package com.shop.payments.repository;

import com.shop.payments.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.processed = true WHERE e.id = ?1")
    void markAsProcessed(Long id);
} 