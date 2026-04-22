package com.tickets.event_ticket_booking_system.controller;

import org.springframework.web.bind.annotation.*;

import com.tickets.event_ticket_booking_system.dto.request.LoginRequest;
import com.tickets.event_ticket_booking_system.dto.request.RefreshTokenRequest;
import com.tickets.event_ticket_booking_system.dto.request.RegisterRequest;
import com.tickets.event_ticket_booking_system.dto.response.AuthResponse;
import com.tickets.event_ticket_booking_system.service.AuthService;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user account")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT tokens")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    



    
}
