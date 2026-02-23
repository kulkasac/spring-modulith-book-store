package com.bookstore.inventory.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBookRequest(
        @NotBlank String isbn,
        @NotBlank String title,
        @NotNull @Min(1) Integer initialStock
) {}
