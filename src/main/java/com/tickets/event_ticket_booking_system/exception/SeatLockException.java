package com.tickets.event_ticket_booking_system.exception;

import org.springframework.http.HttpStatus;

public class SeatLockException extends ApiException {
    public SeatLockException() {
        super(HttpStatus.CONFLICT, "SEAT_BEING_PROCESSED",
                "Seat is currently being processed by another request. Please try again.");
    }
}
