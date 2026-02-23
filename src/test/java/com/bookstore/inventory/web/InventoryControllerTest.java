package com.bookstore.inventory.web;

import com.bookstore.inventory.application.InventoryService;
import com.bookstore.inventory.domain.Book;
import com.bookstore.inventory.web.dto.AddBookRequest;
import com.bookstore.inventory.web.dto.UpdateStockRequest;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    InventoryService inventoryService;

    @Test
    void addBook_returns201WithLocation() throws Exception {
        var bookId = UUID.randomUUID();

        when(inventoryService.addBook("978-0-13-468599-1", "Clean Code", 10))
                .thenReturn(bookId);

        mockMvc.perform(post("/api/inventory/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("978-0-13-468599-1", "Clean Code", 10))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/inventory/books/" + bookId)));
    }

    @Test
    void addBook_returns400WhenRequestIsInvalid() throws Exception {
        // blank isbn, negative stock
        mockMvc.perform(post("/api/inventory/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("", "Clean Code", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").exists())
                .andExpect(jsonPath("$.initialStock").exists());

        verifyNoInteractions(inventoryService);
    }
    @Test
    void getAllBooks_returnsBookList() throws Exception {
        var book = new Book(UUID.randomUUID(), "978-0-13-468599-1", "Clean Code", 10);
        when(inventoryService.getAllBooks()).thenReturn(List.of(book));

        mockMvc.perform(get("/api/inventory/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }

    @Test
    void getBook_returns200WhenFound() throws Exception {
        var bookId = UUID.randomUUID();
        var book = new Book(bookId, "978-0-13-468599-1", "Clean Code", 10);
        when(inventoryService.getBook(bookId)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/inventory/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void getBook_returns404WhenNotFound() throws Exception {
        var bookId = UUID.randomUUID();
        when(inventoryService.getBook(bookId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/books/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    void increaseStock_returns204() throws Exception {
        var bookId = UUID.randomUUID();
        doNothing().when(inventoryService).increaseStock(eq(bookId), eq(5));

        mockMvc.perform(patch("/api/inventory/books/{id}/increase-stock", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStockRequest(5))))
                .andExpect(status().isNoContent());

        verify(inventoryService).increaseStock(bookId, 5);
    }

    @Test
    void decreaseStock_returns422WhenInsufficientStock() throws Exception {
        var bookId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("Not enough stock to decrease"))
                .when(inventoryService).decreaseStock(eq(bookId), eq(99));

        mockMvc.perform(patch("/api/inventory/books/{id}/decrease-stock", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStockRequest(99))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Not enough stock to decrease"));
    }
}
