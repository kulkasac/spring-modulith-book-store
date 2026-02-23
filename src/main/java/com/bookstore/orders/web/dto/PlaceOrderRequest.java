package com.bookstore.orders.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID bookId,
        @NotNull @Min(1) Integer quantity
){}
