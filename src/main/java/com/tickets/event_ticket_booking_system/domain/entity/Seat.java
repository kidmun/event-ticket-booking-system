package com.tickets.event_ticket_booking_system.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tickets.event_ticket_booking_system.domain.enums.PriceTier;
import com.tickets.event_ticket_booking_system.domain.enums.SeatStatus;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(
    name="uk_seat_event_section_row_number",
    columnNames = {"event_id", "section", "row_number", "seat_number"}
), indexes = @Index(name = "idx_seats_event_status", columnList = "event_id, status")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 50)
    private String section;

    @Column(name = "row_number", nullable = false, length = 10)
    private String rowNumber;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="price_tier", nullable = false, length = 50)
    private PriceTier priceTier;

    @Column(nullable = false, precision = 10, scale =2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
    @Version
    private Integer version;
    @CreationTimestamp
    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name= "updated_at", nullable = false)
    private Instant updatedAt;


    public String getLabel(){
        return section + "-" + rowNumber + "-" + seatNumber;
    }



    




    
}
