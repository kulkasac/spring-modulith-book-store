package com.bookstore.inventory.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {
    @Getter
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String isbn;
    @Column(nullable = false)
    private String title;
    @Getter
    @Column(nullable = false)
    private int stock;

    protected Book() {
        // JPA requires a default constructor
    }

    public Book(UUID uuid, String isbn, String title, int initialStock) {
        this.id = uuid;
        this.isbn = isbn;
        this.title = title;
        this.stock = initialStock;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException("Not enough stock to decrease");
        }
        this.stock -= quantity;
    }

}
