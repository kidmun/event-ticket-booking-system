package com.tickets.event_ticket_booking_system.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class RegisterRequest {

    @NotNull @Email
    private String email;

    @NotNull @Size(min=8, max=100)
    private String password;

    @NotNull @Size(max=100)
    private String fullName;
    
}
