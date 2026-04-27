package com.tickets.event_ticket_booking_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tickets.event_ticket_booking_system.domain.entity.OutboxEvent;
import com.tickets.event_ticket_booking_system.domain.enums.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID>{
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAt(OutboxStatus status);
}
