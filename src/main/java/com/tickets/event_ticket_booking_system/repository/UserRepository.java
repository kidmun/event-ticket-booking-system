package com.tickets.event_ticket_booking_system.repository;


import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.Apiversion.Use;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tickets.event_ticket_booking_system.domain.entity.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User, UUID>{

    Optional<User>  findByEmail(String email);
    boolean existsByEmail(String email);
}