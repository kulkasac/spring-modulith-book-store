package com.bookstore.orders.domain;

import com.bookstore.orders.events.OrderPlacedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.domain.AfterDomainEventPublication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Getter
    @Id
    private UUID id;
    @Getter
    @Column(nullable = false)
    private UUID bookId;
    @Getter
    @Column(nullable = false)
    private int quantity;
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    //Add Modular Domain Events
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    protected Order() {
        // JPA requires a default constructor
    }

    public Order(UUID id, UUID bookId, int quantity) {
        this.id = id;
        this.bookId = bookId;
        this.quantity = quantity;
        this.status = OrderStatus.CREATED;

        domainEvents.add(new OrderPlacedEvent(id,bookId,quantity));
    }

    @DomainEvents
    public Collection<Object> domainEvents(){
        return domainEvents;
    }

    @AfterDomainEventPublication
    public void clearEvents(){
        domainEvents.clear();
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void fail() {
        this.status = OrderStatus.FAILED;
    }
}
