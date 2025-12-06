package service;

import domain.Book;
import domain.Loan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoanServiceTest {

    private LoanService loanService;
    private BookService bookService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        bookService = new BookService();
        userService = new UserService();
        loanService = new LoanService(bookService, userService);

        FileManager.writeLines("src/main/resources/data/loans.txt", new ArrayList<>());
    }

    // ---------------------------------------------------------
    // CREATE LOAN
    // ---------------------------------------------------------

    @Test
    public void testCreateLoanSuccess() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        assertTrue(loanService.createLoan(u, b));
        assertEquals(1, loanService.getAllLoans().size());
    }

    @Test
    public void testCreateLoanFailsWhenBookAlreadyBorrowed() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        b.borrowBook(LocalDate.now().minusDays(1));
        assertFalse(loanService.createLoan(u, b));
    }

    @Test
    public void testCreateLoanFailsWhenUserHasFine() {
        User u = new User("Ali", "a@a.com");
        u.setFineBalance(5);
        Book b = new Book("T1", "A1", "111");

        assertFalse(loanService.createLoan(u, b));
    }

    @Test
    public void testCreateLoanFailsWhenUserHasOverdueLoan() {
        User u = new User("Ali", "a@a.com");
        Book b1 = new Book("T1", "A1", "111");
        Book b2 = new Book("T2", "A2", "222");

        Loan l = new Loan(u, b1);
        l.setDueDate(LocalDate.now().minusDays(5));
        u.getActiveBookLoans().add(l);

        assertFalse(loanService.createLoan(u, b2));
    }

    // ⭐ NEW BRANCH COVERAGE TESTS ⭐

    @Test
    public void testCreateLoanFailsWhenUserIsNull() {
        Book b = new Book("T1", "A1", "111");
        assertThrows(NullPointerException.class, () -> loanService.createLoan(null, b));
    }

    @Test
    public void testCreateLoanFailsWhenBookIsNull() {
        User u = new User("Ali", "a@a.com");
        assertThrows(NullPointerException.class, () -> loanService.createLoan(u, null));
    }

    // ---------------------------------------------------------
    // RETURN LOAN
    // ---------------------------------------------------------

    @Test
    public void testReturnLoanSuccess() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        loanService.createLoan(u, b);

        assertTrue(loanService.returnLoan(u, b));
        assertTrue(b.isAvailable());
        assertFalse(loanService.getAllLoans().get(0).isActive());
    }

    @Test
    public void testReturnLoanFailsWhenNoLoan() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        assertFalse(loanService.returnLoan(u, b));
    }

    @Test
    public void testReturnLoanFailsWhenLoanInactive() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        loanService.createLoan(u, b);

        Loan loan = loanService.getAllLoans().get(0);
        loan.returnBook();

        assertFalse(loanService.returnLoan(u, b));
    }

    // ⭐ NEW BRANCH COVERAGE TESTS ⭐

    @Test
    public void testReturnLoanFailsWhenUserMatchesButBookDiffers() {
        User u = new User("Ali", "a@a.com");
        Book b1 = new Book("T1", "A1", "111");
        Book b2 = new Book("T2", "A2", "222");

        loanService.createLoan(u, b1);

        assertFalse(loanService.returnLoan(u, b2));
    }

    @Test
    public void testReturnLoanFailsWhenBookMatchesButUserDiffers() {
        User u1 = new User("Ali", "a@a.com");
        User u2 = new User("Sara", "s@s.com");
        Book b = new Book("T1", "A1", "111");

        loanService.createLoan(u1, b);

        assertFalse(loanService.returnLoan(u2, b));
    }

    // ---------------------------------------------------------
    // LOAD LOANS FROM FILE
    // ---------------------------------------------------------

    @Test
    public void testLoadLoansFromFile() {
        userService.addUser("Ahmad", "a@a.com");
        bookService.addBook("T1", "A1", "111");

        LocalDate borrow = LocalDate.now().minusDays(3);
        LocalDate due = LocalDate.now().plusDays(7);

        List<String> lines = new ArrayList<>();
        lines.add("Ahmad,111," + borrow + "," + due + ",true");
        lines.add("");
        FileManager.writeLines("src/main/resources/data/loans.txt", lines);

        loanService.loadLoansFromFile();

        List<Loan> loans = loanService.getAllLoans();
        assertEquals(1, loans.size());

        Loan l = loans.get(0);
        assertEquals("Ahmad", l.getUser().getUserName());
        assertEquals("111", l.getBook().getIsbn());
        assertEquals(due, l.getDueDate());
    }

    @Test
    public void testLoadLoansFromFileIgnoresInactiveLoan() {
        userService.addUser("Ahmad", "a@a.com");
        bookService.addBook("T1", "A1", "111");

        LocalDate borrow = LocalDate.now().minusDays(3);
        LocalDate due = LocalDate.now().minusDays(1);

        List<String> lines = new ArrayList<>();
        lines.add("Ahmad,111," + borrow + "," + due + ",false");
        FileManager.writeLines("src/main/resources/data/loans.txt", lines);

        loanService.loadLoansFromFile();

        Loan loaded = loanService.getAllLoans().get(0);
        assertFalse(loaded.isActive()); // inactive
    }

    @Test
    public void testLoadLoansFromFileWithInvalidLines() {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("Ghost,9999,2024-01-01,2024-01-10,false");
        FileManager.writeLines("src/main/resources/data/loans.txt", lines);

        loanService.loadLoansFromFile();

        assertTrue(loanService.getAllLoans().isEmpty());
    }

    // ---------------------------------------------------------
    // OVERDUE
    // ---------------------------------------------------------

    @Test
    public void testGetOverdueLoans() {
        User u = new User("Ali", "a@a.com");
        Book b1 = new Book("T1", "A1", "111");
        Book b2 = new Book("T2", "A2", "222");

        loanService.createLoan(u, b1);
        loanService.createLoan(u, b2);

        List<Loan> all = loanService.getAllLoans();
        all.get(0).setDueDate(LocalDate.now().minusDays(10));
        all.get(1).setDueDate(LocalDate.now().plusDays(5));

        List<Loan> overdue = loanService.getOverdueLoans();
        assertEquals(1, overdue.size());
    }

    @Test
    public void testGetOverdueLoansEmpty() {
        assertTrue(loanService.getOverdueLoans().isEmpty());
    }
}
