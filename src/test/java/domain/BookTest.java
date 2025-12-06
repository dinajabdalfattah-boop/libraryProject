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

    // ---------------------------------------------------------
    // Constructor & basic getters
    // ---------------------------------------------------------

    @Test
    public void testConstructor() {
        assertEquals("Book", book.getTitle());
        assertEquals("Ahmad", book.getAuthor());
        assertEquals("123", book.getIsbn());
        assertTrue(book.isAvailable());
        assertFalse(book.isBorrowed());
        assertNull(book.getBorrowDate());
        assertNull(book.getDueDate());
    }

    // ---------------------------------------------------------
    // Borrowing tests
    // ---------------------------------------------------------

    @Test
    public void testBorrowBookWithDate() {
        LocalDate date = LocalDate.of(2025, 10, 26);
        book.borrowBook(date);

        assertFalse(book.isAvailable());
        assertTrue(book.isBorrowed());
        assertEquals(date, book.getBorrowDate());
        assertEquals(date.plusDays(28), book.getDueDate());
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

    // ---------------------------------------------------------
    // Return tests
    // ---------------------------------------------------------

    @Test
    public void testReturnBook() {
        book.borrowBook(LocalDate.now());
        book.returnBook();

        assertTrue(book.isAvailable());
        assertFalse(book.isBorrowed());
        assertNull(book.getBorrowDate());
        assertNull(book.getDueDate());
    }

    @Test
    public void testReturnBookWhenAlreadyAvailable() {
        // Should not crash or behave incorrectly
        book.returnBook();
        assertTrue(book.isAvailable());
        assertNull(book.getBorrowDate());
        assertNull(book.getDueDate());
    }

    // ---------------------------------------------------------
    // Overdue tests
    // ---------------------------------------------------------

    @Test
    public void testIsOverdueWithCustomDate() {
        LocalDate borrowDate = LocalDate.now().minusDays(35);
        book.borrowBook(borrowDate);

        assertTrue(book.isOverdue(LocalDate.now()));
        assertFalse(book.isOverdue(borrowDate.plusDays(10)));  // before due
    }

    @Test
    public void testIsOverdueDefaultMethod() {
        book.borrowBook(LocalDate.now().minusDays(40));
        assertTrue(book.isOverdue());
    }

    @Test
    public void testIsNotOverdueIfNeverBorrowed() {
        assertFalse(book.isOverdue());
        assertFalse(book.isOverdue(LocalDate.now()));
    }

    // ---------------------------------------------------------
    // Remaining days
    // ---------------------------------------------------------

    @Test
    public void testGetRemainingDaysWhenNoDueDate() {
        assertEquals(0, book.getRemainingDays(LocalDate.now()));
    }

    @Test
    public void testGetRemainingDaysPositive() {
        LocalDate date = LocalDate.now();
        book.borrowBook(date);

        assertEquals(28, book.getRemainingDays(date));
    }

    @Test
    public void testGetRemainingDaysNegative() {
        LocalDate date = LocalDate.now().minusDays(40);
        book.borrowBook(date);

        int days = book.getRemainingDays(LocalDate.now());
        assertTrue(days < 0);
    }

    // ---------------------------------------------------------
    // Setters (used for file loading)
    // ---------------------------------------------------------

    @Test
    public void testSettersForLoading() {
        book.setAvailable(false);
        book.setBorrowDate(LocalDate.of(2025, 1, 1));
        book.setDueDate(LocalDate.of(2025, 1, 29));

        assertFalse(book.isAvailable());
        assertEquals(LocalDate.of(2025, 1, 1), book.getBorrowDate());
        assertEquals(LocalDate.of(2025, 1, 29), book.getDueDate());
        assertTrue(book.isBorrowed());
    }

    // ---------------------------------------------------------
    // toString tests
    // ---------------------------------------------------------

    @Test
    public void testToStringWhenAvailable() {
        String result = book.toString();

        assertTrue(result.contains("Book"));
        assertTrue(result.contains("Ahmad"));
        assertTrue(result.contains("123"));
        assertTrue(result.contains("Available=true"));
    }

    @Test
    public void testToStringWhenBorrowed() {
        LocalDate date = LocalDate.now();
        book.borrowBook(date);

        String result = book.toString();
        assertTrue(result.contains(book.getTitle()));
        assertTrue(result.contains(book.getAuthor()));
        assertTrue(result.contains(book.getIsbn()));
        assertTrue(result.contains("Available=false"));
        assertTrue(result.contains(book.getDueDate().toString()));
    }
}
