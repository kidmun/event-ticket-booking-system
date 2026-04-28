package com.tickets.event_ticket_booking_system.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.tickets.event_ticket_booking_system.dto.request.CreateReservationRequest;
import com.tickets.event_ticket_booking_system.dto.response.ReservationResponse;
import com.tickets.event_ticket_booking_system.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name="Reservation")
public class ReservationController {
    private final ReservationService reservationService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve a seat (requires Idempotency-Key header)")
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request, @AuthenticationPrincipal UUID userId, @RequestHeader("Idempotency-Key") UUID idempotencyKey) {
        return reservationService.createReservation(request, userId, idempotencyKey);
        
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation status and expiry countdown")
    public ReservationResponse getReservation(@PathVariable UUID id, @AuthenticationPrincipal UUID userId){

        return reservationService.getReservation(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel pending reservation and release seat")
    public void cancelReservation(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {
        reservationService.cancelReservation(id, userId);
    }



}
