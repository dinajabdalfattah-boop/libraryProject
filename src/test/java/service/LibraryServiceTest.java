package service;

import domain.*;
import notification.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    private UserService userService;
    private BookService bookService;
    private LoanService loanService;
    private CDLoanService cdLoanService;
    private ReminderService reminderService;

    private LibraryService library;

    private User user;
    private Book book;
    private CD cd;
    private Loan loan;
    private CDLoan cdLoan;

    @BeforeEach
    public void setUp() {
        // Create mocks
        userService = mock(UserService.class);
        bookService = mock(BookService.class);
        loanService = mock(LoanService.class);
        cdLoanService = mock(CDLoanService.class);
        reminderService = mock(ReminderService.class);

        library = new LibraryService(
                userService, bookService, loanService, cdLoanService, reminderService
        );

        user = new User("Ahmad", "a@a.com");
        book = new Book("T1", "A1", "111");
        cd = new CD("C1", "Artist", "CD01");

        loan = mock(Loan.class);
        cdLoan = mock(CDLoan.class);
    }

    // ==========================================
    // FINDERS TESTS
    // ==========================================

    @Test
    public void testFindUserByName() {
        when(userService.findUserByName("Ahmad")).thenReturn(user);

        User result = library.findUserByName("Ahmad");
        assertEquals(user, result);
    }

    @Test
    public void testFindBookByISBN() {
        when(bookService.findBookByISBN("111")).thenReturn(book);

        Book result = library.findBookByISBN("111");
        assertEquals(book, result);
    }

    @Test
    public void testFindCDByIdSuccess() {
        List<CD> cds = List.of(cd);
        CD result = library.findCDById(cds, "CD01");

        assertEquals(cd, result);
    }

    @Test
    public void testFindCDByIdNotFound() {
        List<CD> cds = List.of(cd);
        CD result = library.findCDById(cds, "NOT_FOUND");

        assertNull(result);
    }

    // EXTRA BRANCH: list is null
    @Test
    public void testFindCDByIdListNull() {
        assertThrows(NullPointerException.class, () -> library.findCDById(null, "CD01"));
    }

    @Test
    public void testFindLoanUserReturnsCorrectUser() {
        when(loan.getBook()).thenReturn(book);
        when(loan.isActive()).thenReturn(true);
        when(loan.getUser()).thenReturn(user);
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        User result = library.findLoanUser(book);

        assertEquals(user, result);
    }

    @Test
    public void testFindLoanUserReturnsNullIfNotFound() {
        when(loanService.getAllLoans()).thenReturn(List.of());

        assertNull(library.findLoanUser(book));
    }

    // EXTRA BRANCH: wrong book
    @Test
    public void testFindLoanUserIgnoresDifferentBook() {
        Book differentBook = new Book("X", "Y", "999");

        when(loan.getBook()).thenReturn(differentBook);
        when(loan.isActive()).thenReturn(true);
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        assertNull(library.findLoanUser(book));
    }

    // EXTRA BRANCH: inactive loan
    @Test
    public void testFindLoanUserIgnoresInactiveLoan() {
        when(loan.getBook()).thenReturn(book);
        when(loan.isActive()).thenReturn(false);
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        assertNull(library.findLoanUser(book));
    }

    // ==========================================
    // BORROW / RETURN TESTS
    // ==========================================

    @Test
    public void testBorrowBookSuccess() {
        when(loanService.createLoan(user, book)).thenReturn(true);

        assertTrue(library.borrowBook(user, book));
        verify(loanService).createLoan(user, book);
    }

    @Test
    public void testBorrowBookFails() {
        when(loanService.createLoan(user, book)).thenReturn(false);

        assertFalse(library.borrowBook(user, book));
    }

    @Test
    public void testReturnBookSuccess() {
        when(loanService.returnLoan(user, book)).thenReturn(true);

        assertTrue(library.returnBook(user, book));
        verify(loanService).returnLoan(user, book);
    }

    @Test
    public void testBorrowCDSuccess() {
        when(cdLoanService.createCDLoan(user, cd)).thenReturn(true);

        assertTrue(library.borrowCD(user, cd));
        verify(cdLoanService).createCDLoan(user, cd);
    }

    @Test
    public void testReturnCDSuccess() {
        when(cdLoanService.returnCDLoan(user, cd)).thenReturn(true);

        assertTrue(library.returnCD(user, cd));
        verify(cdLoanService).returnCDLoan(user, cd);
    }

    // ==========================================
    // OVERDUE TESTS
    // ==========================================

    @Test
    public void testGetOverdueLoans() {
        when(loanService.getOverdueLoans()).thenReturn(List.of(loan));

        List<Loan> result = library.getOverdueLoans();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetOverdueCDLoans() {
        when(cdLoanService.getOverdueCDLoans()).thenReturn(List.of(cdLoan));

        List<CDLoan> result = library.getOverdueCDLoans();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllLoans() {
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        assertEquals(1, library.getAllLoans().size());
    }

    @Test
    public void testGetAllCDLoans() {
        when(cdLoanService.getAllCDLoans()).thenReturn(List.of(cdLoan));

        assertEquals(1, library.getAllCDLoans().size());
    }

    // ==========================================
    // REMINDER TEST
    // ==========================================

    @Test
    public void testSendOverdueReminders() {
        List<Loan> overdueLoans = List.of(loan);
        List<CDLoan> overdueCDLoans = List.of(cdLoan);

        when(loanService.getOverdueLoans()).thenReturn(overdueLoans);
        when(cdLoanService.getOverdueCDLoans()).thenReturn(overdueCDLoans);

        library.sendOverdueReminders();

        verify(reminderService).sendReminders(overdueLoans, overdueCDLoans);
    }

    // ==========================================
    // LISTS FROM SERVICES
    // ==========================================

    @Test
    public void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(user));

        assertEquals(1, library.getAllUsers().size());
    }

    @Test
    public void testGetAllBooks() {
        when(bookService.getAllBooks()).thenReturn(List.of(book));

        assertEquals(1, library.getAllBooks().size());
    }
}
