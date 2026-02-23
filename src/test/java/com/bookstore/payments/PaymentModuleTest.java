package com.bookstore.payments;

import com.bookstore.inventory.application.InventoryService;
import com.bookstore.orders.application.OrderService;
import com.bookstore.payments.application.PaymentService;
import com.bookstore.payments.domain.PaymentRepository;
import com.bookstore.payments.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class PaymentModuleTest {
    @Autowired
    InventoryService inventoryService;
    @Autowired
    OrderService orderService;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    PaymentService paymentService;

    @Test
    void successfulOrderCreatesCompletedPayment(){
        var bookId = inventoryService.addBook("123-4567-890", "Test Book", 10);
        var orderId = orderService.placeOrder(bookId, 2);

        var payments = paymentRepository.findAll();

        assertThat(payments)
                .anyMatch(p-> p.getOrderId().equals(orderId)
                && p.getStatus().equals(PaymentStatus.COMPLETED));
    }

    @Test
    void failedPaymentGatewayCreatesFailedPayment(){
        // Use a UUID that ends in '0' — deterministically triggers gateway rejection
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        UUID bookId = UUID.randomUUID();

        paymentService.processPayment(orderId, bookId, 1);

        assertThat(paymentRepository.findAll())
        .anyMatch(p-> p.getOrderId().equals(orderId)
                && p.getStatus().equals(PaymentStatus.FAILED));
    }
}
