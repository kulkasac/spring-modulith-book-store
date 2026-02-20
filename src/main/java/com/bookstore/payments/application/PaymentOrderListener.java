package com.bookstore.payments.application;

import com.bookstore.orders.events.OrderPlacedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentOrderListener {
    private final PaymentService paymentService;

    public PaymentOrderListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(OrderPlacedEvent event){
        paymentService.processPayment(event.orderId(),event.bookId(),event.quantity());
    }
}
