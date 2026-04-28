package com.tickets.event_ticket_booking_system.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tickets.event_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.tickets.event_ticket_booking_system.dto.response.PaymentResponse;
import com.tickets.event_ticket_booking_system.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name="Payments")
public class PaymentController {

    private final PaymentService paymentService;
    

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate payment for reservation (Idempotency-Key required)")
    public PaymentResponse initiatePayment(@Valid @RequestBody  CreatePaymentRequest request,  @AuthenticationPrincipal UUID userId, @RequestHeader("Idempotency-Key") UUID idempotencyKey){
        
        return paymentService.initiatePayment(request, userId, idempotencyKey);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment status")
    public PaymentResponse getPayment(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {

        return paymentService.getPayment(id, userId);
    }

    
}
