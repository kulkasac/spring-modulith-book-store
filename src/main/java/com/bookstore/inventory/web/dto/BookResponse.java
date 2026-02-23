package com.bookstore.inventory.web.dto;

import java.util.UUID;

public record BookResponse(
        UUID id,
        String isbn,
        String title,
        int stock
) {
    public static BookResponse from(com.bookstore.inventory.domain.Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getStock()
        );
    }
}
