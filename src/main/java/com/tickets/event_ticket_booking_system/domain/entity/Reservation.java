package com.tickets.event_ticket_booking_system.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tickets.event_ticket_booking_system.domain.enums.ReservationStatus;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name="reservations", indexes = {
    @Index(name="idx_reservation_expires",columnList = "status, expires_at"),
    @Index(name="idx_reservation_expires",columnList = "status, expires_at") 
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="event_id", nullable = false)
    private Event event;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name= "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name= "idempotency_key", unique = true, nullable = false)
    private UUID idempotencyKey;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name= "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isExpired(){
        return Instant.now().isAfter(expiresAt);
    }





    
}
