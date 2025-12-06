package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private Book book1;
    private Book book2;
    private CD cd1;
    private CD cd2;

    @BeforeEach
    public void setUp() {
        user = new User("UserA", "user@test.com");
        book1 = new Book("Title1", "Author1", "1111");
        book2 = new Book("Title2", "Author2", "2222");
        cd1 = new CD("CD1", "Artist1", "CD01");
        cd2 = new CD("CD2", "Artist2", "CD02");
    }

    // ---------------------------------------------------------
    // Constructors & basic getters/setters
    // ---------------------------------------------------------

    @Test
    public void testConstructorWithNameOnly() {
        User u = new User("OnlyName");
        assertEquals("OnlyName", u.getUserName());
        assertEquals("no-email@none.com", u.getEmail());
        assertEquals(0.0, u.getFineBalance());
        assertTrue(u.getActiveBookLoans().isEmpty());
        assertTrue(u.getActiveCDLoans().isEmpty());
    }

    @Test
    public void testConstructorWithNameAndEmail() {
        assertEquals("UserA", user.getUserName());
        assertEquals("user@test.com", user.getEmail());
        assertEquals(0.0, user.getFineBalance());
        assertTrue(user.getActiveBookLoans().isEmpty());
        assertTrue(user.getActiveCDLoans().isEmpty());
    }

    @Test
    public void testSetEmail() {
        user.setEmail("new@mail.com");
        assertEquals("new@mail.com", user.getEmail());
    }

    @Test
    public void testSetFineBalance() {
        user.setFineBalance(25.5);
        assertEquals(25.5, user.getFineBalance());
    }

    // ---------------------------------------------------------
    // addLoan (Book) logic
    // ---------------------------------------------------------

    @Test
    public void testAddLoanSuccessWhenNoFineAndNoOverdue() {
        Loan loan = new Loan(user, book1);
        user.addLoan(loan);

        assertEquals(1, user.getActiveBookLoans().size());
        assertTrue(user.getActiveBookLoans().contains(loan));
    }

    @Test
    public void testAddLoanFailsWhenUserHasFine() {
        user.setFineBalance(10.0);
        Loan loan = new Loan(user, book1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> user.addLoan(loan)
        );
        assertEquals("Cannot borrow: Unpaid fines.", ex.getMessage());
        assertTrue(user.getActiveBookLoans().isEmpty());
    }

    @Test
    public void testAddLoanFailsWhenUserHasOverdueLoan() {
        // add an overdue loan manually
        Loan overdueLoan = new Loan(user, book1);
        overdueLoan.setDueDate(LocalDate.now().minusDays(5));
        user.getActiveBookLoans().add(overdueLoan);

        Loan newLoan = new Loan(user, book2);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> user.addLoan(newLoan)
        );
        assertEquals("Cannot borrow: Overdue loans exist.", ex.getMessage());
        assertEquals(1, user.getActiveBookLoans().size());
        assertTrue(user.getActiveBookLoans().contains(overdueLoan));
    }

    // ---------------------------------------------------------
    // addCDLoan logic
    // ---------------------------------------------------------

    @Test
    public void testAddCDLoanSuccess() {
        CDLoan loan = new CDLoan(user, cd1);
        user.addCDLoan(loan);

        assertEquals(1, user.getActiveCDLoans().size());
        assertTrue(user.getActiveCDLoans().contains(loan));
    }

    @Test
    public void testAddCDLoanFailsWhenFine() {
        user.setFineBalance(15.0);
        CDLoan loan = new CDLoan(user, cd1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> user.addCDLoan(loan)
        );
        assertEquals("Cannot borrow: Unpaid fines.", ex.getMessage());
        assertTrue(user.getActiveCDLoans().isEmpty());
    }

    @Test
    public void testAddCDLoanFailsWhenOverdueExists() {
        // overdue CD loan
        CDLoan overdue = new CDLoan(user, cd1);
        overdue.setDueDate(LocalDate.now().minusDays(3));
        user.getActiveCDLoans().add(overdue);

        CDLoan newLoan = new CDLoan(user, cd2);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> user.addCDLoan(newLoan)
        );
        assertEquals("Cannot borrow: Overdue loans exist.", ex.getMessage());
        assertEquals(1, user.getActiveCDLoans().size());
        assertTrue(user.getActiveCDLoans().contains(overdue));
    }

    // ---------------------------------------------------------
    // Return loans
    // ---------------------------------------------------------

    @Test
    public void testReturnLoanRemovesAndCallsReturnBook() {
        Loan loan = new Loan(user, book1);
        user.addLoan(loan);

        assertFalse(book1.isAvailable());
        assertTrue(loan.isActive());

        user.returnLoan(loan);

        assertTrue(book1.isAvailable());
        assertFalse(loan.isActive());
        assertFalse(user.getActiveBookLoans().contains(loan));
    }

    @Test
    public void testReturnLoanDoesNothingIfNotInList() {
        Loan loan = new Loan(user, book1);
        // not added to activeBookLoans
        user.returnLoan(loan); // should not throw
        // state unchanged
        assertTrue(user.getActiveBookLoans().isEmpty());
    }

    @Test
    public void testReturnCDLoanRemovesAndCallsReturnCD() {
        CDLoan loan = new CDLoan(user, cd1);
        user.addCDLoan(loan);

        assertFalse(cd1.isAvailable());
        assertTrue(loan.isActive());

        user.returnCDLoan(loan);

        assertTrue(cd1.isAvailable());
        assertFalse(loan.isActive());
        assertFalse(user.getActiveCDLoans().contains(loan));
    }

    @Test
    public void testReturnCDLoanDoesNothingIfNotInList() {
        CDLoan loan = new CDLoan(user, cd1);
        // not added
        user.returnCDLoan(loan);
        assertTrue(user.getActiveCDLoans().isEmpty());
    }

    // ---------------------------------------------------------
    // Overdue detection
    // ---------------------------------------------------------

    @Test
    public void testHasOverdueLoansFalseInitially() {
        assertFalse(user.hasOverdueLoans());
    }

    @Test
    public void testHasOverdueLoansWithOnlyNonOverdue() {
        Loan l1 = new Loan(user, book1);
        l1.setDueDate(LocalDate.now().plusDays(3));
        user.getActiveBookLoans().add(l1);

        CDLoan c1 = new CDLoan(user, cd1);
        c1.setDueDate(LocalDate.now().plusDays(2));
        user.getActiveCDLoans().add(c1);

        assertFalse(user.hasOverdueLoans());
    }

    @Test
    public void testHasOverdueLoansTrueForBook() {
        Loan l1 = new Loan(user, book1);
        l1.setDueDate(LocalDate.now().minusDays(2));
        user.getActiveBookLoans().add(l1);

        assertTrue(user.hasOverdueLoans());
    }

    @Test
    public void testHasOverdueLoansTrueForCD() {
        CDLoan c1 = new CDLoan(user, cd1);
        c1.setDueDate(LocalDate.now().minusDays(1));
        user.getActiveCDLoans().add(c1);

        assertTrue(user.hasOverdueLoans());
    }

    @Test
    public void testGetOverdueCountMixed() {
        Loan l1 = new Loan(user, book1);
        l1.setDueDate(LocalDate.now().minusDays(5)); // overdue

        Loan l2 = new Loan(user, book2);
        l2.setDueDate(LocalDate.now().plusDays(5)); // not overdue

        user.getActiveBookLoans().add(l1);
        user.getActiveBookLoans().add(l2);

        CDLoan c1 = new CDLoan(user, cd1);
        c1.setDueDate(LocalDate.now().minusDays(3)); // overdue

        CDLoan c2 = new CDLoan(user, cd2);
        c2.setDueDate(LocalDate.now().plusDays(1)); // not overdue

        user.getActiveCDLoans().add(c1);
        user.getActiveCDLoans().add(c2);

        assertEquals(2, user.getOverdueCount());
    }

    // ---------------------------------------------------------
    // Pay fines
    // ---------------------------------------------------------

    @Test
    public void testPayFinePartial() {
        user.setFineBalance(40);
        user.payFine(15);
        assertEquals(25, user.getFineBalance());
    }

    @Test
    public void testPayFineClearsAllWhenAmountGreaterOrEqual() {
        user.setFineBalance(30);
        user.payFine(30);
        assertEquals(0, user.getFineBalance());

        user.setFineBalance(50);
        user.payFine(100);
        assertEquals(0, user.getFineBalance());
    }

    // ---------------------------------------------------------
    // Unregister rules
    // ---------------------------------------------------------

    @Test
    public void testCanBeUnregisteredWhenNoLoansAndNoFine() {
        assertTrue(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredDueToBookLoan() {
        Loan loan = new Loan(user, book1);
        user.getActiveBookLoans().add(loan);
        assertFalse(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredDueToCDLoan() {
        CDLoan loan = new CDLoan(user, cd1);
        user.getActiveCDLoans().add(loan);
        assertFalse(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredDueToFine() {
        user.setFineBalance(5.0);
        assertFalse(user.canBeUnregistered());
    }

    @Test
    public void testCannotBeUnregisteredWithLoanAndFine() {
        Loan loan = new Loan(user, book1);
        user.getActiveBookLoans().add(loan);
        user.setFineBalance(5.0);

        assertFalse(user.canBeUnregistered());
    }

    // ---------------------------------------------------------
    // toString
    // ---------------------------------------------------------

    @Test
    public void testToStringContainsNameEmailAndFine() {
        user.setFineBalance(12.5);
        String text = user.toString();

        assertTrue(text.contains("UserA"));
        assertTrue(text.contains("user@test.com"));
        assertTrue(text.contains("12.5"));
    }
}
