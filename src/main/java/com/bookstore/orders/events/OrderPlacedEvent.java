package com.bookstore.orders.events;

import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface("events")
public record OrderPlacedEvent(UUID orderId, UUID bookId, int quantity) {
}
