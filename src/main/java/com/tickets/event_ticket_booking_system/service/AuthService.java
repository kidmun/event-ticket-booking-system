package com.tickets.event_ticket_booking_system.service;


import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tickets.event_ticket_booking_system.domain.entity.User;
import com.tickets.event_ticket_booking_system.domain.enums.UserRole;
import com.tickets.event_ticket_booking_system.dto.request.LoginRequest;
import com.tickets.event_ticket_booking_system.dto.request.RefreshTokenRequest;
import com.tickets.event_ticket_booking_system.dto.request.RegisterRequest;
import com.tickets.event_ticket_booking_system.dto.response.AuthResponse;
import com.tickets.event_ticket_booking_system.repository.UserRepository;
import com.tickets.event_ticket_booking_system.security.JwtTokenProvider;
import com.tickets.event_ticket_booking_system.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final  PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional 
    public AuthResponse register(RegisterRequest request){

        if (userRepository.existsByEmail(request.getEmail())){
            throw new ApiException(
                HttpStatus.CONFLICT,
                "USER_ALREADY_EXISTS",
                "User with email " + request.getEmail() + " already exists"
            );
        }
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .role(UserRole.USER)
            .build();
        userRepository.save(user);
       log.info("User registered: {}", user.getEmail());
         return buildAuthResponse(user);

    }


    public AuthResponse login(LoginRequest request){ 

         User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Invalid email or password.");
        }
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }




    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", "Refresh token is invalid or expired.");
        }

        UUID userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "INVALID_TOKEN", "User not found for token."));

        return buildAuthResponse(user);
    }
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .build();
    }



    
}
