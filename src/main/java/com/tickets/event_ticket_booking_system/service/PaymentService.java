package com.tickets.event_ticket_booking_system.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.event_ticket_booking_system.domain.entity.Booking;
import com.tickets.event_ticket_booking_system.domain.entity.Event;
import com.tickets.event_ticket_booking_system.domain.entity.OutboxEvent;
import com.tickets.event_ticket_booking_system.domain.entity.Payment;
import com.tickets.event_ticket_booking_system.domain.entity.Reservation;
import com.tickets.event_ticket_booking_system.domain.entity.Seat;
import com.tickets.event_ticket_booking_system.domain.entity.User;
import com.tickets.event_ticket_booking_system.domain.enums.PaymentStatus;
import com.tickets.event_ticket_booking_system.domain.enums.ReservationStatus;
import com.tickets.event_ticket_booking_system.domain.enums.SeatStatus;
import com.tickets.event_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.tickets.event_ticket_booking_system.dto.response.PaymentResponse;
import com.tickets.event_ticket_booking_system.exception.ApiException;
import com.tickets.event_ticket_booking_system.exception.ReservationExpiredException;
import com.tickets.event_ticket_booking_system.exception.ResourceNotFoundException;
import com.tickets.event_ticket_booking_system.infrastructure.payment.PaymentGateway;
import com.tickets.event_ticket_booking_system.infrastructure.payment.PaymentResult;
import com.tickets.event_ticket_booking_system.repository.BookingRepository;
import com.tickets.event_ticket_booking_system.repository.OutboxEventRepository;
import com.tickets.event_ticket_booking_system.repository.PaymentRepository;
import com.tickets.event_ticket_booking_system.repository.ReservationRepository;
import com.tickets.event_ticket_booking_system.repository.SeatRepository;
import com.tickets.event_ticket_booking_system.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentGateway paymentGateway;
    private final ObjectMapper objectMapper;

    @Value("${payment.max-retries:3}")
    private int maxRetries;

    @Value("${payment.base-delay-ms:1000}")
    private long baseDelayMs;

    @Transactional
    public PaymentResponse initiatePayment(CreatePaymentRequest request, UUID userId, UUID idempotencyKey){

        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()){
            return toResponse(existing.get());
        }

        Reservation reservation = reservationRepository.findByIdForUpdate(request.getReservationId()).orElseThrow(
            () -> new ResourceNotFoundException("Reservation", request.getReservationId())
        );
        if (!reservation.getUser().getId().equals(userId)){
            throw new ResourceNotFoundException("Reservation", request.getReservationId());
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE",
                    "Reservation is not in PENDING state.");
        }
        if (reservation.isExpired()){
            throw new ReservationExpiredException();
        }
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("User", userId)
        );

        Payment payment = Payment.builder()
                .reservation(reservation)
                .user(user)
                .amount(reservation.getSeat().getPrice())
                .paymentProvider(request.getPaymentProvider())
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.INITIATED)
                .build();
                payment = paymentRepository.save(payment);
            
        payment = paymentRepository.save(payment);
        payment = processWithRetries(payment);
        
        if (payment.getStatus() == PaymentStatus.SUCCESS){
           confirmBooking(reservation, payment, user);
        }
        else{
            handlePaymentFailer(reservation);
        }


        return toResponse(payment);



        

        

        



    }

    private Payment processWithRetries(Payment payment){

        for (int attempt =0; attempt < maxRetries; attempt++){
            try{
                payment.setStatus(PaymentStatus.PROCESSING);
                payment = paymentRepository.save(payment);
                PaymentResult result = paymentGateway.charge(payment.getAmount(), payment.getCurrency(), payment.getIdempotencyKey().toString());
                payment.setProviderTransactionId(result.transactionId());
                payment.setStatus(PaymentStatus.SUCCESS);
                return paymentRepository.save(payment);

            }
            catch(RuntimeException e){
                log.warn("Payment attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt == maxRetries-1){
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason(e.getMessage());
                    return paymentRepository.save(payment);   
                }
                payment.incrementRetryCount();
                paymentRepository.save(payment);

                long delay = baseDelayMs * (long) Math.pow(2, attempt);
                long jitter = ThreadLocalRandom.current().nextLong(delay/2);
                try {
                    Thread.sleep(delay + jitter);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Interrupted during retry");
                    return paymentRepository.save(payment);
                }

            }
        }
        return payment;
        
    }

    @Transactional
    protected void confirmBooking(Reservation reservation, Payment payment, User user){
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.BOOKED);

        Booking booking = Booking.builder()
                .reservation(reservation)
                .user(user)
                .event(reservation.getEvent())
                .seat(seat)
                .bookingReference(generateBookingReference())
                .totalAmount(payment.getAmount())
                .build();
        bookingRepository.save(booking);
        try {
    String payload = objectMapper.writeValueAsString(new BookingConfirmedPayload(
            booking.getId(),
            booking.getBookingReference(),
            user.getEmail(),
            reservation.getEvent().getTitle(),
            seat.getLabel(),
            payment.getAmount()
    ));

    OutboxEvent outbox = OutboxEvent.builder()
            .aggregateType("Booking")
            .aggregateId(booking.getId())
            .eventType("booking.confirmed")
            .payload(payload)
            .build();

    outboxEventRepository.save(outbox);

} catch (JsonProcessingException e) {
    log.error("Failed to serialize booking confirmed outbox event", e);
    throw new RuntimeException("Failed to create booking confirmed outbox event", e);
}
log.info("Booking confirmed: {} reference: {}", booking.getId(), booking.getBookingReference());
    

                
    }
    public PaymentResponse getPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        if (!payment.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Payment", paymentId);
        }
        return toResponse(payment);
    }


    private void handlePaymentFailer(Reservation reservation){
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);
        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        Event event = reservation.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);

    }

    private String generateBookingReference(){
         String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("EVT-");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }


     private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .reservationId(p.getReservation().getId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .paymentProvider(p.getPaymentProvider())
                .providerTransactionId(p.getProviderTransactionId())
                .createdAt(p.getCreatedAt())
                .build();
    }




  
public record BookingConfirmedPayload(
            UUID bookingId, String bookingReference,
            String userEmail, String eventTitle,
            String seatLabel, java.math.BigDecimal amount) {}
    
} 



