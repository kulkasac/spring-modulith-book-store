package com.bookstore.orders.web.dto;

import com.bookstore.orders.domain.Order;
import com.bookstore.orders.domain.OrderStatus;

import java.util.UUID;

public record OrderResponse(
        UUID    id,
        UUID    bookId,
        int     quantity,
        OrderStatus status
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getBookId(),
                order.getQuantity(),
                order.getStatus()
        );
    }
}
