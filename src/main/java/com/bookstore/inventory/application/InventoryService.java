package com.bookstore.inventory.application;

import com.bookstore.inventory.domain.Book;
import com.bookstore.inventory.domain.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private final BookRepository bookRepository;

    public InventoryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public UUID addBook(String isbn, String title, int initialStock) {
        Book book = new Book(UUID.randomUUID(), isbn, title, initialStock);
        bookRepository.save(book);
        return book.getId();
    }

    public void increaseStock(UUID bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new IllegalArgumentException("Book not found"));
        book.increaseStock(quantity);
    }

    public void decreaseStock(UUID bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new IllegalArgumentException("Book not found"));
        book.decreaseStock(quantity);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBook(UUID bookId) {
        return bookRepository.findById(bookId);
    }
}
