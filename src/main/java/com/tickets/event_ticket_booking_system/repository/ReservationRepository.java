package com.tickets.event_ticket_booking_system.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;


import com.tickets.event_ticket_booking_system.domain.entity.Reservation;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;


public interface ReservationRepository extends JpaRepository<Reservation,  UUID> {

    Optional<Reservation> findByIdempotencyKey(UUID idempotencyKey);

    @Query("SELECT r FROM Reservation r where r.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT r FROM Reservation r where r.status = 'PENDING' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") Instant now);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(UUID userId);

} 