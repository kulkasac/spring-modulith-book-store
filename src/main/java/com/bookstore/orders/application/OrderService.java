package com.bookstore.orders.application;

import com.bookstore.orders.domain.Order;
import com.bookstore.orders.domain.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMetrics orderMetrics;

    public OrderService(OrderRepository orderRepository,
                        OrderMetrics orderMetrics) {
        this.orderRepository = orderRepository;
        this.orderMetrics = orderMetrics;
    }

    public UUID placeOrder(UUID bookId, int quantity) {
        // Create and save order
        Order order = new Order(UUID.randomUUID(), bookId, quantity);
        orderRepository.save(order);
        orderMetrics.recordOrderPlaced();
        return order.getId();
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public Optional<Order> getOrder(UUID orderId){
        return orderRepository.findById(orderId);
    }
}
