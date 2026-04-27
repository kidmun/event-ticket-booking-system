package com.tickets.event_ticket_booking_system.infrastructure.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {
 
    
    @Override
    public PaymentResult charge(BigDecimal amount, String currency, String idempotencyKey){
     if (Math.random() < 0.05) {
            throw new RuntimeException("Simulated payment provider error");
        }
        return new PaymentResult("mock-txn-" + UUID.randomUUID(), true);
    }

    @Override
    public PaymentResult refund(String transactionId, BigDecimal amount) {
        log.info("Mock refund: {} for txn {}", amount, transactionId);
        return new PaymentResult("mock-refund-" + UUID.randomUUID(), true);
    }
}