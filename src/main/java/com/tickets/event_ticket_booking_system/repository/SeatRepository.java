package com.tickets.event_ticket_booking_system.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.tickets.event_ticket_booking_system.domain.entity.Seat;
import com.tickets.event_ticket_booking_system.domain.enums.SeatStatus;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByEventId(UUID eventId);
    List<Seat> findByEventIdAndStatus(UUID eventId, SeatStatus status);

    @Query("SELECT s FROM Seat s where s.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Seat> findByIdForUpdate(@Param("id") UUID id);

    long countByEventIdAndStatus(UUID eventId, SeatStatus status);

    
}
