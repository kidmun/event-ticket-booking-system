package com.tickets.event_ticket_booking_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.tickets.event_ticket_booking_system.domain.entity.Payment;



public interface PaymentRepository extends JpaRepository<Payment, UUID>{

    Optional<Payment>  findByIdempotencyKey(UUID idempotencyKey);
    Optional<Payment>  findByReservationId(UUID reservationId);


} 
