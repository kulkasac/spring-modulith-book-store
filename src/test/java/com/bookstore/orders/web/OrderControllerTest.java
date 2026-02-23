package com.bookstore.orders.web;

import com.bookstore.orders.application.OrderService;
import com.bookstore.orders.domain.Order;
import com.bookstore.orders.web.dto.PlaceOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  OrderService orderService;

    @Test
    void placeOrder_returns201WithLocationHeader() throws Exception {
        var bookId  = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(orderService.placeOrder(bookId, 2)).thenReturn(orderId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PlaceOrderRequest(bookId, 2))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/orders/" + orderId)));
    }

    @Test
    void placeOrder_returns400WhenRequestInvalid() throws Exception {
        // null bookId, zero quantity
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PlaceOrderRequest(null, 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bookId").exists())
                .andExpect(jsonPath("$.quantity").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    void getAllOrders_returnsOrderList() throws Exception {
        var bookId  = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var order   = new Order(orderId, bookId, 2);
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].bookId").value(bookId.toString()))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void getOrder_returns200WhenFound() throws Exception {
        var bookId  = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var order   = new Order(orderId, bookId, 2);
        when(orderService.getOrder(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_returns404WhenNotFound() throws Exception {
        var orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }
}