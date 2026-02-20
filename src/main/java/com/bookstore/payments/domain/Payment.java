package com.bookstore.payments.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Getter
    @Id
    private UUID    id;
    @Getter
    @Column(nullable = false)
    private UUID orderId;
    @Getter
    @Column(nullable = false)
    private UUID    bookId;
    @Column(nullable = false)
    private int quantity;
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    protected Payment(){ }

    public Payment(UUID id, UUID orderId, UUID bookId, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.status = PaymentStatus.PENDING;
    }

    public void complete(){
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail(){
        this.status = PaymentStatus.FAILED;
    }

}
