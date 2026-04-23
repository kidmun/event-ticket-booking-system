package com.tickets.event_ticket_booking_system.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tickets.event_ticket_booking_system.domain.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash",  nullable = false)
    private String passwordHash;

    @Column(name="first_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name= "updated_at", nullable = false)
    private Instant updatedAt;





    
  
}
