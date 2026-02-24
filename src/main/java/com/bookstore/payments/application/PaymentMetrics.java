package com.bookstore.payments.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {
    private final Counter paymentCompletedCounter;
    private final Counter paymentFailedCounter;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.paymentCompletedCounter = Counter.builder("bookstore.payments.completed")
                .description("Total number of successful payments")
                .register(meterRegistry);

        this.paymentFailedCounter = Counter.builder("bookstore.payments.failed")
                .description("Total number of failed payments")
                .register(meterRegistry);
    }

    public void recordPaymentCompleted(){
        paymentCompletedCounter.increment();
    }

    public void recordPaymentFailed(){
        paymentFailedCounter.increment();
    }
}
