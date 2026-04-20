package com.tickets.event_ticket_booking_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tickets.event_ticket_booking_system.domain.entity.Event;
import com.tickets.event_ticket_booking_system.domain.entity.Reservation;
import com.tickets.event_ticket_booking_system.domain.entity.Seat;
import com.tickets.event_ticket_booking_system.domain.entity.User;
import com.tickets.event_ticket_booking_system.domain.enums.ReservationStatus;
import com.tickets.event_ticket_booking_system.domain.enums.SeatStatus;
import com.tickets.event_ticket_booking_system.dto.request.CreateReservationRequest;
import com.tickets.event_ticket_booking_system.dto.response.ReservationResponse;
import com.tickets.event_ticket_booking_system.exception.ApiException;
import com.tickets.event_ticket_booking_system.exception.ResourceNotFoundException;
import com.tickets.event_ticket_booking_system.exception.SeatLockException;
import com.tickets.event_ticket_booking_system.exception.SeatUnavailableException;
import com.tickets.event_ticket_booking_system.repository.EventRepository;
import com.tickets.event_ticket_booking_system.repository.ReservationRepository;
import com.tickets.event_ticket_booking_system.repository.SeatRepository;
import com.tickets.event_ticket_booking_system.repository.UserRepository;

import jodd.net.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {


    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    @Value("${app.reservation.expiry.minutes}")
    private int expiryMinutes;

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, UUID userId, UUID idempotencyKey){

        Optional<Reservation> existing = reservationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()){
            return toResponse(existing.get());
        }
        UUID seatId = request.getSeatId();
        String lockKey = "seat-lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);

            if (!acquired){
                throw new SeatLockException();
            }
            Seat seat = seatRepository.findByIdForUpdate(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
            );

            if (seat.getStatus() != SeatStatus.AVAILABLE){
                throw new SeatUnavailableException(seat.getLabel());
            }
            User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", userId)
            );
            Event event = eventRepository.findById(request.getEventId()).orElseThrow(
                () -> new ResourceNotFoundException("Event", request.getEventId())
            );
            seat.setStatus(SeatStatus.RESERVED);
            seatRepository.save(seat);

            Reservation reservation = Reservation.builder()
                .seat(seat)
                .user(user)
                .event(event)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .idempotencyKey(idempotencyKey)
                .build();

            reservation = reservationRepository.save(reservation);
            event.setAvailableSeats(event.getAvailableSeats() -1);
            eventRepository.save(event);

            log.info("Reservation created: {} for seat {} by user {}",
                    reservation.getId(), seatId, userId);
                     return toResponse(reservation);


        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new SeatLockException();
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    public ReservationResponse getReservation(UUID reservationId, UUID userId){
       Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        if (!reservation.getUser().getId().equals(userId)){
            throw new ResourceNotFoundException("Reservation", reservationId);
        }

        return toResponse(reservation);
    }

    @Transactional
    public void cancelReservation(UUID reservationId, UUID userId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        if (!reservation.getUser().getId().equals(userId)){
            throw new ResourceNotFoundException("Reservation", reservationId);
        }
        if (reservation.getStatus() != ReservationStatus.PENDING){
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "INVALID_STATE", "Only PENDING reservations can be cancelled.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        Event event = reservation.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);
        
        log.info("Reservation cancelled: {}", reservationId);

    }

    private ReservationResponse toResponse(Reservation reservation) {
     
        long secondsRemaining = Math.max(0, Duration.between(Instant.now(), reservation.getExpiresAt()).getSeconds());
        return ReservationResponse.builder()
            .id(reservation.getId())
            .seatId(reservation.getSeat().getId())
            .eventId(reservation.getEvent().getId())
            .status(reservation.getStatus().name())
            .expiresAt(reservation.getExpiresAt())
            .secondsRemaining(secondsRemaining)
            .createdAt(reservation.getCreatedAt())
         
            .build();
    }
    
}
