package com.tickets.event_ticket_booking_system.exception;

import org.springframework.http.HttpStatus;

public class SeatUnavailableException extends ApiException {
    public SeatUnavailableException(String seatLabel) {
        super(HttpStatus.CONFLICT, "SEAT_ALREADY_RESERVED",
                "Seat " + seatLabel + " is already reserved by another user.");
    }
}
