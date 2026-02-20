package com.bookstore.payments.events;

import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentId, UUID orderId) {
}
