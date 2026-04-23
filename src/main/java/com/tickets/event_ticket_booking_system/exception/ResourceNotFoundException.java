package com.tickets.event_ticket_booking_system.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                resource + " not found with id: " + id);
    }
}
