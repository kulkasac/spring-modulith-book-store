package com.bookstore.orders.web;

import com.bookstore.orders.application.OrderService;
import com.bookstore.orders.web.dto.OrderResponse;
import com.bookstore.orders.web.dto.PlaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
       UUID orderId = orderService.placeOrder(request.bookId(), request.quantity());
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        var orders = orderService.getAllOrders()
                .stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
