package com.tickets.event_ticket_booking_system.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tickets.event_ticket_booking_system.domain.entity.Event;
import com.tickets.event_ticket_booking_system.domain.enums.EventStatus;


public interface EventRepository  extends JpaRepository<Event, UUID>{
 Page<Event> findByStatus(EventStatus status, Pageable pageable);
 Page<Event> findByStatusAndCategory(EventStatus status, String category, Pageable pageable);   
}
