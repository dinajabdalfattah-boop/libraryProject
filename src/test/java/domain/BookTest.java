package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private Book book;

    @BeforeEach
    public void setUp() {
        book = new Book("Book", "Ahmad", "123");
    }

    @Test
    public void testBorrowBookWithDate() {
        LocalDate date = LocalDate.of(2025, 10, 26);
        book.borrowBook(date);

        assertFalse(book.isAvailable());
        assertEquals(date, book.getBorrowDate());
        assertEquals(date.plusDays(28), book.getDueDate());
        assertTrue(book.isBorrowed());
    }

    @Test
    public void testBorrowBookWithoutDate() {
        book.borrowBook();

        assertFalse(book.isAvailable());
        assertTrue(book.isBorrowed());
        assertNotNull(book.getBorrowDate());
        assertNotNull(book.getDueDate());
    }

    @Test
    public void testBorrowThrowsIfAlreadyBorrowed() {
        book.borrowBook(LocalDate.now());
        assertThrows(IllegalStateException.class, () -> book.borrowBook(LocalDate.now()));
    }

    @Test
    public void testReturnBook() {
        book.borrowBook(LocalDate.now());
        book.returnBook();

        assertTrue(book.isAvailable());
        assertNull(book.getBorrowDate());
        assertNull(book.getDueDate());
        assertFalse(book.isBorrowed());
    }

    @Test
    public void testIsOverdueWithCustomDate() {
        LocalDate borrowDate = LocalDate.now().minusDays(35);
        book.borrowBook(borrowDate);

        assertTrue(book.isOverdue(LocalDate.now()));
        assertFalse(book.isOverdue(borrowDate.plusDays(5)));
    }

    @Test
    public void testIsOverdueDefault() {
        book.borrowBook(LocalDate.now().minusDays(40));
        assertTrue(book.isOverdue());
    }

    @Test
    public void testIsNotOverdueIfNeverBorrowed() {
        assertFalse(book.isOverdue());
    }

    @Test
    public void testToStringWhenAvailable() {
        String result = book.toString();

        assertTrue(result.contains("Available"));
        assertTrue(result.contains(book.getTitle()));
        assertTrue(result.contains(book.getAuthor()));
        assertTrue(result.contains(book.getIsbn()));
    }

    @Test
    public void testToStringWhenBorrowed() {
        LocalDate date = LocalDate.now();
        book.borrowBook(date);

        String result = book.toString();

        assertTrue(result.contains("Not Available"));
        assertTrue(result.contains(date.plusDays(28).toString()));
    }
}
