package com.bookstore.orders.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Getter
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID bookId;
    @Column(nullable = false)
    private int quantity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected Order() {
        // JPA requires a default constructor
    }

    public Order(UUID id, UUID bookId, int quantity) {
        this.id = id;
        this.bookId = bookId;
        this.quantity = quantity;
        this.status = OrderStatus.CREATED;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void fail() {
        this.status = OrderStatus.FAILED;
    }
}
