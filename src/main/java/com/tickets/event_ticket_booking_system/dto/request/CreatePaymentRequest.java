package com.tickets.event_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentRequest {
    @NotNull private UUID reservationId;
    @NotBlank private String paymentProvider;
}
