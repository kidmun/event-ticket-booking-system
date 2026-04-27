package com.tickets.event_ticket_booking_system.infrastructure.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult charge(BigDecimal amount, String currency, String idempotencyKey);
    PaymentResult refund(String transactionId, BigDecimal amount);
}
