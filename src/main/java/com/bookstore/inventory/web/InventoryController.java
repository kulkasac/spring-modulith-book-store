package com.bookstore.inventory.web;

import com.bookstore.inventory.application.InventoryService;
import com.bookstore.inventory.web.dto.AddBookRequest;
import com.bookstore.inventory.web.dto.BookResponse;
import com.bookstore.inventory.web.dto.UpdateStockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/books")
    public ResponseEntity<Void> addBook(@Valid @RequestBody AddBookRequest request) {
        UUID bookId = inventoryService.addBook(
                request.isbn(),
                request.title(),
                request.initialStock()
        );
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        var books = inventoryService.getAllBooks()
                .stream()
                .map(BookResponse::from)
                .toList();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable UUID id) {
        return inventoryService.getBook(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/books/{id}/increase-stock")
    public ResponseEntity<Void> increaseStock(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateStockRequest request) {
        inventoryService.increaseStock(id, request.quantity());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/books/{id}/decrease-stock")
    public ResponseEntity<Void> decreaseStock(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateStockRequest request) {
        inventoryService.decreaseStock(id, request.quantity());
        return ResponseEntity.noContent().build();
    }
}
