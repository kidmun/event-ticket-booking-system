package com.tickets.event_ticket_booking_system.exception;

import org.springframework.http.HttpStatus;

public class ReservationExpiredException extends ApiException {
    public ReservationExpiredException() {
        super(HttpStatus.GONE, "RESERVATION_EXPIRED",
                "Reservation has expired. Please create a new reservation.");
    }
}
