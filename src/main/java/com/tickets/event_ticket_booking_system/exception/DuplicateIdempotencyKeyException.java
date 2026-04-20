package com.tickets.event_ticket_booking_system.exception;


import org.springframework.http.HttpStatus;

public class DuplicateIdempotencyKeyException extends ApiException {
    public DuplicateIdempotencyKeyException() {
        super(HttpStatus.CONFLICT, "DUPLICATE_IDEMPOTENCY_KEY",
                "A request with this idempotency key has already been processed.");
    }
}
