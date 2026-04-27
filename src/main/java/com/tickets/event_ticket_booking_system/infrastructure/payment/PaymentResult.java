package com.tickets.event_ticket_booking_system.infrastructure.payment;

public record PaymentResult(String transactionId, boolean success) {}