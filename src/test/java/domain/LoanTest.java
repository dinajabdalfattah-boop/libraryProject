package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LoanTest {

    private User user;
    private Book book;

    @BeforeEach
    public void setUp() {
        user = new User("UserA");
        book = new Book("Title1", "Author1", "1111");
    }

    @Test
    public void testConstructorAndGetters() {
        Loan loan = new Loan(user, book);

        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getBook());
        assertNotNull(loan.getBorrowDate());
        assertEquals(loan.getBorrowDate().plusDays(28), loan.getDueDate());
    }

    @Test
    public void testIsOverdueCustomDates() {
        Loan loan = new Loan(user, book);

        LocalDate beforeDue = loan.getBorrowDate().plusDays(5);
        assertFalse(loan.isOverdue(beforeDue));

        LocalDate afterDue = loan.getBorrowDate().plusDays(40);
        assertTrue(loan.isOverdue(afterDue));
    }

    @Test
    public void testIsOverdueDefaultFalse() {
        Loan loan = new Loan(user, book);
        assertFalse(loan.isOverdue());
    }

    @Test
    public void testIsOverdueDefaultTrue() {
        Loan loan = new Loan(user, book);
        loan.setDueDate(LocalDate.now().minusDays(1));
        assertTrue(loan.isOverdue());
    }

    @Test
    public void testSetDueDate() {
        Loan loan = new Loan(user, book);
        LocalDate newDue = LocalDate.now().plusDays(50);

        loan.setDueDate(newDue);
        assertEquals(newDue, loan.getDueDate());
    }

    @Test
    public void testToStringContainsUserBookAndDueDate() {
        Loan loan = new Loan(user, book);
        String result = loan.toString();

        assertTrue(result.contains(user.getUserName()));
        assertTrue(result.contains(book.getTitle()));
        assertTrue(result.contains(loan.getDueDate().toString()));
    }
}
