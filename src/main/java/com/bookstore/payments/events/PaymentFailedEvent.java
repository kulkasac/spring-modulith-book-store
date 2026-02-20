package com.bookstore.payments.events;

import java.util.UUID;

public record PaymentFailedEvent(UUID paymentId, UUID orderId,String reason) {
}
