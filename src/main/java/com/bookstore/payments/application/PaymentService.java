package com.bookstore.payments.application;

import com.bookstore.payments.domain.Payment;
import com.bookstore.payments.domain.PaymentRepository;
import com.bookstore.payments.domain.PaymentStatus;
import com.bookstore.payments.events.PaymentCompletedEvent;
import com.bookstore.payments.events.PaymentFailedEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository, ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    public void processPayment(UUID orderId , UUID bookId, int quantity) {
        Payment payment = new Payment(UUID.randomUUID(), orderId, bookId, quantity);
        try{
            simulatePaymentGateWay(orderId);
            payment.complete();
        } catch (Exception e) {
            payment.fail();
            eventPublisher.publishEvent(new PaymentFailedEvent(payment.getId(), orderId, e.getMessage()));
        }
        paymentRepository.save(payment);
        if(payment.getStatus().equals(PaymentStatus.COMPLETED)){
            eventPublisher.publishEvent(new PaymentCompletedEvent(payment.getId(), orderId));
        }
    }

    private void simulatePaymentGateWay(UUID orderId) {
        if(orderId.toString().startsWith("0")) {
            throw new RuntimeException("Payment Gateway rejected payment");
        }
    }
}
