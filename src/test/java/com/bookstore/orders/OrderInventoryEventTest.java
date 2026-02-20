package com.bookstore.orders;

import com.bookstore.inventory.application.InventoryService;
import com.bookstore.inventory.domain.BookRepository;
import com.bookstore.orders.application.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class OrderInventoryEventTest {

    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final BookRepository bookRepository;

    @Autowired
    public OrderInventoryEventTest(InventoryService inventoryService, OrderService orderService, BookRepository bookRepository) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.bookRepository = bookRepository;
    }

    @Test
    void placeOrderAndCheckInventory() {
        // Given
        var bookId = inventoryService.addBook("1234567890", "Test Book", 10);

        // When
        orderService.placeOrder(bookId, 2);

        // Then
        var book = bookRepository.findById(bookId).orElseThrow();
        assert book.getStock() == 8 : "Expected stock to be 8 after placing order, but was " + book.getStock();
    }

    @Test
    void placeOrderWithInsufficientStock() {
        // Given
        var bookId = inventoryService.addBook("0987654321", "Another Test Book", 1);

        assertThatThrownBy(() -> orderService.placeOrder(bookId, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough stock to decrease");
    }
    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded, which includes checking for module dependencies and configurations.
    }
}
