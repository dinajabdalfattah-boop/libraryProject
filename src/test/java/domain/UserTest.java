package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private Book book1;
    private Book book2;

    @BeforeEach
    public void setUp() {
        user = new User("John Doe");
        book1 = new Book("Clean Code", "Robert Martin", "978-0132350884");
        book2 = new Book("Design Patterns", "Gamma", "978-0201633610");
    }

    @Test
    public void testGetName() {
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void testFineBalance() {
        assertEquals(0, user.getFineBalance());
        user.setFineBalance(25.5);
        assertEquals(25.5, user.getFineBalance());
    }

    @Test
    public void testBorrowedBooksInitiallyEmpty() {
        assertTrue(user.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testBorrowBookSuccess() {
        user.borrowBook(book1);
        assertFalse(book1.isAvailable());
        assertEquals(1, user.getBorrowedBooks().size());
        assertTrue(user.getBorrowedBooks().contains(book1));
    }

    @Test
    public void testReturnBook() {
        user.borrowBook(book1);
        user.returnBook(book1);
        assertTrue(book1.isAvailable());
        assertTrue(user.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testPayFine() {
        user.setFineBalance(50);
        user.payFine(20);
        assertEquals(30, user.getFineBalance());
        user.payFine(50);
        assertEquals(0, user.getFineBalance());
    }

    // ======== Sprint 2 specific edge cases ========
    @Test
    public void testBorrowBookWithOutstandingFineThrowsException() {
        user.setFineBalance(50.0);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> user.borrowBook(book1)
        );
        assertEquals("Pay fines first", exception.getMessage());
        assertTrue(user.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testBorrowBookWithOverdueBookThrowsException() {
        book1.borrowBook(LocalDate.now().minusDays(30));
        user.getBorrowedBooks().add(book1);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> user.borrowBook(book2)
        );
        assertEquals("Return overdue books first", exception.getMessage());
        assertEquals(1, user.getBorrowedBooks().size());
        assertTrue(user.getBorrowedBooks().contains(book1));
        assertFalse(user.getBorrowedBooks().contains(book2));
    }

    @Test
    public void testMultipleBooksInBorrowedList() {
        user.borrowBook(book1);
        user.borrowBook(book2);
        List<Book> books = user.getBorrowedBooks();
        assertEquals(2, books.size());
        assertTrue(books.contains(book1));
        assertTrue(books.contains(book2));
    }
}
