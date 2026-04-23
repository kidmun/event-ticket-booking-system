package com.tickets.event_ticket_booking_system.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder @AllArgsConstructor
public class ReservationResponse {
    private UUID id;
    private UUID seatId;
    private UUID eventId;
    private String status;
    private Instant expiresAt;
    private long secondsRemaining;
    private Instant createdAt;
}
