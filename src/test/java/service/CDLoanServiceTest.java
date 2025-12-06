package service;

import domain.*;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CDLoanServiceTest {

    private CDLoanService cdLoanService;
    private BookService bookService;
    private UserService userService;

    private User userA;
    private User userB;

    private CD cd1;
    private CD cd2;

    private final String FILE_PATH = "src/main/resources/data/cdloans.txt";

    @BeforeEach
    public void setUp() {
        bookService = new BookService();
        userService = new UserService();
        cdLoanService = new CDLoanService(bookService, userService);

        FileManager.writeLines(FILE_PATH, new ArrayList<>());

        userA = new User("A", "a@mail.com");
        userB = new User("B", "b@mail.com");

        userService.addUser("A", "a@mail.com");
        userService.addUser("B", "b@mail.com");

        cd1 = new CD("CD1", "Artist1", "ID1");
        cd2 = new CD("CD2", "Artist2", "ID2");
    }

    // ---------------------------------------------------------
    // CREATE CD LOAN
    // ---------------------------------------------------------

    @Test
    public void testCreateCDLoanSuccess() {
        boolean result = cdLoanService.createCDLoan(userA, cd1);

        assertTrue(result);
        assertEquals(1, cdLoanService.getAllCDLoans().size());
        assertFalse(cd1.isAvailable());
        assertEquals(1, userA.getActiveCDLoans().size());
    }

    @Test
    public void testCreateCDLoanFailsFine() {
        userA.setFineBalance(10);
        assertFalse(cdLoanService.createCDLoan(userA, cd1));
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    @Test
    public void testCreateCDLoanFailsOverdueLoans() {
        Book b = new Book("T1", "A1", "111");
        Loan loan = new Loan(userA, b);
        loan.setDueDate(LocalDate.now().minusDays(5));
        userA.getActiveBookLoans().add(loan);

        assertFalse(cdLoanService.createCDLoan(userA, cd1));
    }

    @Test
    public void testCreateCDLoanFailsCDAlreadyBorrowed() {
        cd1.borrowCD(LocalDate.now().minusDays(1));
        assertFalse(cdLoanService.createCDLoan(userA, cd1));
    }

    // NEW: null user
    @Test
    public void testCreateCDLoanFailsWhenUserNull() {
        assertFalse(cdLoanService.createCDLoan(null, cd1));
    }

    // NEW: null CD
    @Test
    public void testCreateCDLoanFailsWhenCDNull() {
        assertFalse(cdLoanService.createCDLoan(userA, null));
    }

    // ---------------------------------------------------------
    // RETURN CD LOAN
    // ---------------------------------------------------------

    @Test
    public void testReturnCDLoanSuccess() {
        cdLoanService.createCDLoan(userA, cd1);

        boolean result = cdLoanService.returnCDLoan(userA, cd1);

        assertTrue(result);
        assertTrue(cd1.isAvailable());
        assertEquals(0, userA.getActiveCDLoans().size());
    }

    @Test
    public void testReturnCDLoanFailsNoLoanFound() {
        assertFalse(cdLoanService.returnCDLoan(userA, cd1));
    }

    @Test
    public void testReturnCDLoanFailsDifferentUser() {
        cdLoanService.createCDLoan(userA, cd1);
        assertFalse(cdLoanService.returnCDLoan(userB, cd1));
    }

    // NEW: loan exists but inactive
    @Test
    public void testReturnCDLoanFailsWhenLoanInactive() {
        cdLoanService.createCDLoan(userA, cd1);

        CDLoan loan = cdLoanService.getAllCDLoans().get(0);
        loan.returnCD(); // deactivate

        assertFalse(cdLoanService.returnCDLoan(userA, cd1));
    }

    // ---------------------------------------------------------
    // LOAD FROM FILE
    // ---------------------------------------------------------

    @Test
    public void testLoadCDLoansFromFile() {

        List<String> lines = new ArrayList<>();
        LocalDate borrow = LocalDate.now().minusDays(10);
        LocalDate due = LocalDate.now().minusDays(3);

        lines.add("A,ID1," + borrow + "," + due + ",true");
        lines.add("B,ID2," + borrow + "," + due + ",false");
        lines.add("Unknown,ID1," + borrow + "," + due + ",true");
        lines.add("A,InvalidID," + borrow + "," + due + ",true");
        lines.add("");

        FileManager.writeLines(FILE_PATH, lines);

        cdLoanService.loadCDLoansFromFile(List.of(cd1, cd2));

        List<CDLoan> loaded = cdLoanService.getAllCDLoans();
        assertEquals(2, loaded.size());

        CDLoan loanA = loaded.stream().filter(l -> l.getCD().getId().equals("ID1")).findFirst().orElse(null);
        assertNotNull(loanA);
        assertTrue(loanA.isActive());

        CDLoan loanB = loaded.stream().filter(l -> l.getCD().getId().equals("ID2")).findFirst().orElse(null);
        assertNotNull(loanB);
        assertFalse(loanB.isActive());
        assertEquals(0, userB.getActiveCDLoans().size());
    }

    // NEW: active loan but user not found
    @Test
    public void testLoadCDLoansFromFileActiveLoanUnknownUserIgnored() {
        List<String> lines = new ArrayList<>();
        LocalDate borrow = LocalDate.now().minusDays(5);
        LocalDate due = LocalDate.now().minusDays(1);

        lines.add("Ghost,ID1," + borrow + "," + due + ",true");

        FileManager.writeLines(FILE_PATH, lines);

        cdLoanService.loadCDLoansFromFile(List.of(cd1, cd2));

        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    // ---------------------------------------------------------
    // OVERDUE
    // ---------------------------------------------------------

    @Test
    public void testGetOverdueCDLoans() {
        cdLoanService.createCDLoan(userA, cd1);
        CDLoan loan = cdLoanService.getAllCDLoans().get(0);
        loan.setDueDate(LocalDate.now().minusDays(5));

        List<CDLoan> result = cdLoanService.getOverdueCDLoans();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetOverdueCDLoansEmpty() {
        assertTrue(cdLoanService.getOverdueCDLoans().isEmpty());
    }

    @Test
    public void testGetOverdueCDLoansNoneOverdue() {
        cdLoanService.createCDLoan(userA, cd1);
        CDLoan loan = cdLoanService.getAllCDLoans().get(0);
        loan.setDueDate(LocalDate.now().plusDays(10));

        assertTrue(cdLoanService.getOverdueCDLoans().isEmpty());
    }

    // ---------------------------------------------------------
    // GET ALL LOANS
    // ---------------------------------------------------------

    @Test
    public void testGetAllCDLoansEmpty() {
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    @Test
    public void testGetAllCDLoansNonEmpty() {
        cdLoanService.createCDLoan(userA, cd1);
        cdLoanService.createCDLoan(userB, cd2);

        assertEquals(2, cdLoanService.getAllCDLoans().size());
    }
}
