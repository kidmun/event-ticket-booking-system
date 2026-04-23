package com.tickets.event_ticket_booking_system.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}
