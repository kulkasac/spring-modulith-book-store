package com.bookstore.inventory.application;

import com.bookstore.orders.events.OrderPlacedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryOrderListener {

    private final InventoryService inventoryService;

    public InventoryOrderListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(OrderPlacedEvent event) {
        inventoryService.decreaseStock(event.bookId(),event.quantity());
    }
}
