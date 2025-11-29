package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private Book book1;
    private Book book2;

    private String userName;
    private String title1;
    private String author1;
    private String isbn1;

    private String title2;
    private String author2;
    private String isbn2;

    @BeforeEach
    public void setUp() {

        userName = "UserA";
        title1 = "Title1";
        author1 = "Author1";
        isbn1 = "1111";

        title2 = "Title2";
        author2 = "Author2";
        isbn2 = "2222";

        user = new User(userName);
        book1 = new Book(title1, author1, isbn1);
        book2 = new Book(title2, author2, isbn2);
    }

    @Test
    public void testGetUserName() {
        assertEquals(userName, user.getUserName());
    }

    @Test
    public void testFineBalance() {
        assertEquals(0.0, user.getFineBalance());
        user.setFineBalance(10.5);
        assertEquals(10.5, user.getFineBalance());
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
        user.setFineBalance(40);
        user.payFine(15);
        assertEquals(25, user.getFineBalance());

        user.payFine(100);
        assertEquals(0, user.getFineBalance());
    }

    @Test
    public void testBorrowFailsWhenUserHasOutstandingFine() {
        user.setFineBalance(50);

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> user.borrowBook(book1)
        );

        assertEquals("Cannot borrow: Pay fines first!", e.getMessage());
        assertTrue(user.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testBorrowFailsWhenUserHasOverdueBook() {

        // make book1 overdue
        LocalDate overdueDate = LocalDate.now().minusDays(40);
        book1.borrowBook(overdueDate);

        user.getBorrowedBooks().add(book1);

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> user.borrowBook(book2)
        );

        assertEquals("Cannot borrow: Return overdue books first!", e.getMessage());
        assertEquals(1, user.getBorrowedBooks().size());
        assertTrue(user.getBorrowedBooks().contains(book1));
    }

    @Test
    public void testBorrowMultipleBooks() {
        user.borrowBook(book1);
        user.borrowBook(book2);

        assertEquals(2, user.getBorrowedBooks().size());
        assertTrue(user.getBorrowedBooks().contains(book1));
        assertTrue(user.getBorrowedBooks().contains(book2));
    }

    @Test
    public void testOverdueCount() {
        book1.borrowBook(LocalDate.now().minusDays(50));
        user.getBorrowedBooks().add(book1);

        book2.borrowBook(LocalDate.now().minusDays(10));
        user.getBorrowedBooks().add(book2);

        assertEquals(1, user.getOverdueCount());
    }

    @Test
    public void testCanBeUnregisteredWhenClean() {
        assertTrue(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredDueToActiveLoan() {
        user.borrowBook(book1);
        assertFalse(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredDueToFine() {
        user.setFineBalance(5);
        assertFalse(user.canBeUnregistered());
    }
}
