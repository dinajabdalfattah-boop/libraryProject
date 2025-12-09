package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CDLoanTest {

    private static final String USER_NAME = "UserA";
    private static final String USER_EMAIL = "u@test.com";
    private static final String CD_TITLE = "CD1";
    private static final String CD_ARTIST = "Artist1";
    private static final String CD_CODE = "CD100";

    private User user;
    private CD cd;

    @BeforeEach
    public void setUp() {
        user = new User(USER_NAME, USER_EMAIL);
        cd = new CD(CD_TITLE, CD_ARTIST, CD_CODE);
    }

    // ---------------------------------------------------------
    // Constructor + Basic State
    // ---------------------------------------------------------

    @Test
    public void testConstructorInitializesFieldsCorrectly() {
        CDLoan loan = new CDLoan(user, cd);

        assertEquals(user, loan.getUser());
        assertEquals(cd, loan.getCD());
        assertNotNull(loan.getBorrowDate());
        assertEquals(loan.getBorrowDate().plusDays(7), loan.getDueDate());
        assertTrue(loan.isActive());
        assertFalse(cd.isAvailable());
    }

    // ---------------------------------------------------------
    // Return CD behavior
    // ---------------------------------------------------------

    @Test
    public void testReturnCDMakesLoanInactiveAndCDAvailable() {
        CDLoan loan = new CDLoan(user, cd);

        loan.returnCD();

        assertFalse(loan.isActive());
        assertTrue(cd.isAvailable());
    }

    @Test
    public void testReturnCDDoesNothingIfAlreadyInactive() {
        CDLoan loan = new CDLoan(user, cd);
        loan.returnCD();

        // second call should not break
        loan.returnCD();

        assertFalse(loan.isActive());
        assertTrue(cd.isAvailable());
    }

    // ---------------------------------------------------------
    // Overdue tests
    // ---------------------------------------------------------

    @Test
    public void testIsOverdueTrue() {
        CDLoan loan = new CDLoan(user, cd);
        loan.setDueDate(LocalDate.now().minusDays(2));

        assertTrue(loan.isOverdue());
        assertTrue(loan.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueFalseFutureDueDate() {
        CDLoan loan = new CDLoan(user, cd);
        loan.setDueDate(LocalDate.now().plusDays(3));

        assertFalse(loan.isOverdue());
        assertFalse(loan.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueFalseWhenLoanInactive() {
        CDLoan loan = new CDLoan(user, cd);
        loan.returnCD();
        loan.setDueDate(LocalDate.now().minusDays(10));

        assertFalse(loan.isOverdue(LocalDate.now()));
    }

    // ---------------------------------------------------------
    // Overdue Days
    // ---------------------------------------------------------

    @Test
    public void testGetOverdueDaysWhenNotOverdue() {
        CDLoan loan = new CDLoan(user, cd);
        loan.setDueDate(LocalDate.now().plusDays(5));

        assertEquals(0, loan.getOverdueDays());
    }

    @Test
    public void testGetOverdueDaysWhenOverdue() {
        CDLoan loan = new CDLoan(user, cd);
        loan.setDueDate(LocalDate.now().minusDays(4));

        assertEquals(4, loan.getOverdueDays());
    }

    // ---------------------------------------------------------
    // Fine calculation
    // ---------------------------------------------------------

    @Test
    public void testCalculateFineUsingDefaultStrategy() {
        CDLoan loan = new CDLoan(user, cd);
        loan.setDueDate(LocalDate.now().minusDays(3));

        assertEquals(3 * 20, loan.calculateFine());
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    @Test
    public void testSetBorrowDate() {
        CDLoan loan = new CDLoan(user, cd);
        LocalDate d = LocalDate.of(2023, 1, 1);

        loan.setBorrowDate(d);
        assertEquals(d, loan.getBorrowDate());
    }

    @Test
    public void testSetDueDate() {
        CDLoan loan = new CDLoan(user, cd);
        LocalDate d = LocalDate.of(2024, 1, 1);

        loan.setDueDate(d);
        assertEquals(d, loan.getDueDate());
    }

    // ---------------------------------------------------------
    // toString
    // ---------------------------------------------------------

    @Test
    public void testToStringContainsFields() {
        CDLoan loan = new CDLoan(user, cd);

        String s = loan.toString();
        assertTrue(s.contains(user.getUserName()));
        assertTrue(s.contains(cd.getTitle()));
        assertTrue(s.contains("active=true"));
    }

    @Test
    public void testToStringAfterReturn() {
        CDLoan loan = new CDLoan(user, cd);
        loan.returnCD();

        String s = loan.toString();
        assertTrue(s.contains("active=false"));
    }
}
