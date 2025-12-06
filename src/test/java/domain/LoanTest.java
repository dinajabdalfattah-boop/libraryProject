package domain;

import domain.fine.FineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LoanTest {

    private User user;
    private Book book;

    @BeforeEach
    public void setUp() {
        user = new User("UserA", "email@test.com");
        book = new Book("Title1", "Author1", "1111");
    }

    // ---------------------------------------------------------
    // Constructor + Initial State
    // ---------------------------------------------------------

    @Test
    public void testConstructorAndGetters() {
        Loan loan = new Loan(user, book);

        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getBook());
        assertNotNull(loan.getBorrowDate());
        assertEquals(loan.getBorrowDate().plusDays(28), loan.getDueDate());
        assertTrue(loan.isActive());
        assertTrue(book.isBorrowed());
    }

    // ---------------------------------------------------------
    // Borrow & Due Date Manipulation
    // ---------------------------------------------------------

    @Test
    public void testSetBorrowDate() {
        Loan loan = new Loan(user, book);
        LocalDate custom = LocalDate.of(2024, 1, 1);

        loan.setBorrowDate(custom);
        assertEquals(custom, loan.getBorrowDate());
    }

    @Test
    public void testSetDueDate() {
        Loan loan = new Loan(user, book);
        LocalDate newDue = LocalDate.now().plusDays(50);
        loan.setDueDate(newDue);
        assertEquals(newDue, loan.getDueDate());
    }

    // ---------------------------------------------------------
    // Overdue Tests
    // ---------------------------------------------------------

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
    public void testIsOverdueWhenDueDateNull() {
        Loan loan = new Loan(user, book);
        loan.setDueDate(null);
        assertFalse(loan.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueWhenInactive() {
        Loan loan = new Loan(user, book);
        loan.returnBook();
        loan.setDueDate(LocalDate.now().minusDays(1));

        assertFalse(loan.isOverdue(LocalDate.now()));
    }

    // ---------------------------------------------------------
    // Overdue Days
    // ---------------------------------------------------------

    @Test
    public void testGetOverdueDaysZeroWhenNotOverdue() {
        Loan loan = new Loan(user, book);
        assertEquals(0, loan.getOverdueDays());
    }

    @Test
    public void testGetOverdueDaysPositive() {
        Loan loan = new Loan(user, book);
        loan.setDueDate(LocalDate.now().minusDays(5));

        assertTrue(loan.isOverdue());
        assertEquals(5, loan.getOverdueDays());
    }

    // ---------------------------------------------------------
    // Fine Strategy
    // ---------------------------------------------------------

    @Test
    public void testFineCalculationDefaultStrategy() {
        Loan loan = new Loan(user, book);
        loan.setDueDate(LocalDate.now().minusDays(3));

        int fine = loan.calculateFine();
        assertEquals(3 * 10, fine);
    }

    @Test
    public void testFineCalculationCustomStrategy() {
        FineStrategy testStrategy = overdue -> overdue * 100;

        Loan loan = new Loan(user, book, testStrategy);
        loan.setDueDate(LocalDate.now().minusDays(2));

        int fine = loan.calculateFine();
        assertEquals(200, fine);
    }

    // ⭐⭐ NEW TESTS FOR FULL COVERAGE ⭐⭐

    @Test
    public void testSetFineStrategyNull() {
        Loan loan = new Loan(user, book);
        loan.setFineStrategy(null);
        assertNull(loan.getFineStrategy());
    }

    @Test
    public void testCalculateFineWhenStrategyIsNullThrowsException() {
        Loan loan = new Loan(user, book);
        loan.setDueDate(LocalDate.now().minusDays(2));
        loan.setFineStrategy(null);

        assertThrows(NullPointerException.class, loan::calculateFine);
    }

    // ---------------------------------------------------------
    // Return Book Behavior
    // ---------------------------------------------------------

    @Test
    public void testReturnBook() {
        Loan loan = new Loan(user, book);

        assertTrue(book.isBorrowed());
        assertTrue(loan.isActive());

        loan.returnBook();

        assertFalse(book.isBorrowed());
        assertFalse(loan.isActive());
    }

    @Test
    public void testReturnBookWhenAlreadyInactive() {
        Loan loan = new Loan(user, book);

        loan.returnBook();
        loan.returnBook();

        assertFalse(loan.isActive());
        assertTrue(book.isAvailable());
    }

    // ---------------------------------------------------------
    // toString
    // ---------------------------------------------------------

    @Test
    public void testToStringContainsAllFields() {
        Loan loan = new Loan(user, book);
        String result = loan.toString();

        assertTrue(result.contains(user.getUserName()));
        assertTrue(result.contains(book.getTitle()));
        assertTrue(result.contains(loan.getBorrowDate().toString()));
        assertTrue(result.contains(loan.getDueDate().toString()));
        assertTrue(result.contains("active=true"));
    }

    @Test
    public void testToStringAfterReturn() {
        Loan loan = new Loan(user, book);
        loan.returnBook();

        String result = loan.toString();
        assertTrue(result.contains("active=false"));
    }
}
