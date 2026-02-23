package com.bookstore.inventory.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @NotNull @Min(1)   Integer quantity
) {}
