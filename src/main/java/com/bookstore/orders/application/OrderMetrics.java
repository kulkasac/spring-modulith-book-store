package com.bookstore.orders.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {
    private final Counter orderPlacedCounter;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.orderPlacedCounter = Counter.builder("bookstore.orders.placed")
                .description("Number of orders placed")
                .register(meterRegistry);
    }

    public void recordOrderPlaced(){
        orderPlacedCounter.increment();
    }
}
