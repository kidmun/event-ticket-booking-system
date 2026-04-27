package com.tickets.event_ticket_booking_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tickets.event_ticket_booking_system.domain.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, UUID>{

    Page<Booking>  findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Booking> findByBookingReference(String bookingReference);
    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);

} 