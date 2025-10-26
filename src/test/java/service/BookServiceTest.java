package service;

import domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {

    private BookService bookService;

    @BeforeEach
    public void setUp() {
        bookService = new BookService();
    }

    @Test
    public void testAddBookSuccess() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
    }

    @Test
    public void testAddBookDuplicateISBN() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.addBook("Another Book", "Some Author", "978-0132350884");
    }

    @Test
    public void testSearchBookFound() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.searchBook("Clean Code");
        bookService.searchBook("Robert Martin");
        bookService.searchBook("978-0132350884");
    }

    @Test
    public void testSearchBookNotFound() {
        bookService.searchBook("Nonexistent Book");
    }

    @Test
    public void testShowAllBooksEmpty() {
        bookService.showAllBooks();
    }

    @Test
    public void testShowAllBooksNonEmpty() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.showAllBooks();
    }

    @Test
    public void testBorrowBookSuccess() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.borrowBook("978-0132350884");
    }

    @Test
    public void testBorrowBookNotFound() {
        bookService.borrowBook("000-0000000000");
    }

    @Test
    public void testBorrowBookAlreadyBorrowed() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.borrowBook("978-0132350884");
        bookService.borrowBook("978-0132350884");
    }

    @Test
    public void testReturnBookSuccess() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.borrowBook("978-0132350884");
        bookService.returnBook("978-0132350884");
    }

    @Test
    public void testReturnBookNotFound() {
        bookService.returnBook("000-0000000000");
    }

    @Test
    public void testIsBookOverdueTrue() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        Book b = bookService.findBookByISBN("978-0132350884");
        LocalDate pastDate = LocalDate.now().minus(30, ChronoUnit.DAYS);
        b.borrowBook(pastDate);
        assertTrue(bookService.isBookOverdue("978-0132350884"));
    }

    @Test
    public void testIsBookOverdueFalse() {
        bookService.addBook("Clean Code", "Robert Martin", "978-0132350884");
        bookService.borrowBook("978-0132350884");
        assertFalse(bookService.isBookOverdue("978-0132350884"));
    }

    @Test
    public void testIsBookOverdueBookNotFound() {
        assertFalse(bookService.isBookOverdue("000-0000000000"));
    }
}
