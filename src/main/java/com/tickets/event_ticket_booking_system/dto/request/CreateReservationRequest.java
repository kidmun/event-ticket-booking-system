package com.tickets.event_ticket_booking_system.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateReservationRequest {

    @NotNull private UUID seatId;
    @NotNull private UUID eventId;
    
}
