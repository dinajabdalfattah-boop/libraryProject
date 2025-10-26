package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private Book book;

    @BeforeEach
    public void setUp() {
        book = new Book("Clean Code", "Robert Martin", "978-0132350884");
    }

    @Test
    public void testInitialAvailability() {
        assertTrue(book.isAvailable());
        assertNull(book.getDueDate());
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("978-0132350884", book.getIsbn());
    }

    @Test
    public void testBorrowBookWithDate() {
        LocalDate borrowDate = LocalDate.of(2025, 10, 26);
        book.borrowBook(borrowDate);

        assertFalse(book.isAvailable());
        assertEquals(borrowDate.plusDays(28), book.getDueDate());
    }

    @Test
    public void testBorrowBookThrowsWhenAlreadyBorrowed() {
        book.borrowBook(LocalDate.now());
        assertThrows(IllegalStateException.class, () -> book.borrowBook(LocalDate.now()));
    }

    @Test
    public void testBorrowBookWithoutDate() {
        book.borrowBook();
        assertFalse(book.isAvailable());
        assertNotNull(book.getDueDate());
    }

    @Test
    public void testReturnBook() {
        book.borrowBook();
        book.returnBook();

        assertTrue(book.isAvailable());
        assertNull(book.getDueDate());
    }

    @Test
    public void testIsOverdueWithCustomDate() {
        LocalDate borrowDate = LocalDate.now().minusDays(30);
        book.borrowBook(borrowDate);

        assertTrue(book.isOverdue(LocalDate.now()));
        assertFalse(book.isOverdue(borrowDate.plusDays(10)));
    }

    @Test
    public void testIsOverdueDefault() {
        book.borrowBook(LocalDate.now().minusDays(30));
        assertTrue(book.isOverdue());
    }

    @Test
    public void testIsOverdueNotBorrowed() {
        assertFalse(book.isOverdue());
    }

    @Test
    public void testToStringAvailable() {
        String str = book.toString();
        assertTrue(str.contains("Available"));
        assertTrue(str.contains("Clean Code"));
        assertTrue(str.contains("Robert Martin"));
        assertTrue(str.contains("978-0132350884"));
    }

    @Test
    public void testToStringNotAvailable() {
        LocalDate borrowDate = LocalDate.now();
        book.borrowBook(borrowDate);
        String str = book.toString();
        assertTrue(str.contains("Not Available"));
        assertTrue(str.contains(borrowDate.plusDays(28).toString()));
    }
}
