package com.tickets.event_ticket_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID reservationId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentProvider;
    private String providerTransactionId;
    private Instant createdAt;
}
