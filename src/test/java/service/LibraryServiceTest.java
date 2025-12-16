package service;

import domain.Book;
import domain.CD;
import domain.CDLoan;
import domain.Loan;
import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    private UserService userService;
    private BookService bookService;
    private LoanService loanService;
    private CDLoanService cdLoanService;
    private ReminderService reminderService;
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        bookService = mock(BookService.class);
        loanService = mock(LoanService.class);
        cdLoanService = mock(CDLoanService.class);
        reminderService = mock(ReminderService.class);

        libraryService = new LibraryService(userService, bookService, loanService, cdLoanService, reminderService);
    }

    @Test
    void findUserByName_delegates() {
        User u = mock(User.class);
        when(userService.findUserByName("A")).thenReturn(u);

        assertSame(u, libraryService.findUserByName("A"));
        verify(userService, times(1)).findUserByName("A");
    }

    @Test
    void findBookByISBN_delegates() {
        Book b = mock(Book.class);
        when(bookService.findBookByISBN("111")).thenReturn(b);

        assertSame(b, libraryService.findBookByISBN("111"));
        verify(bookService, times(1)).findBookByISBN("111");
    }

    @Test
    void findCDById_returnsMatch_orNull() {
        CD c1 = new CD("T1", "A1", "1");
        CD c2 = new CD("T2", "A2", "2");
        List<CD> cds = List.of(c1, c2);

        assertSame(c2, libraryService.findCDById(cds, "2"));
        assertNull(libraryService.findCDById(cds, "X"));
    }

    @Test
    void findLoanUser_returnsUserWhenActiveLoanMatches() {
        Book b = mock(Book.class);

        Loan l1 = mock(Loan.class);
        when(l1.getBook()).thenReturn(mock(Book.class));
        when(l1.isActive()).thenReturn(true);

        Loan l2 = mock(Loan.class);
        when(l2.getBook()).thenReturn(b);
        when(l2.isActive()).thenReturn(true);

        User u = mock(User.class);
        when(l2.getUser()).thenReturn(u);

        when(loanService.getAllLoans()).thenReturn(List.of(l1, l2));

        assertSame(u, libraryService.findLoanUser(b));
    }

    @Test
    void findLoanUser_returnsNullWhenNoMatch() {
        Book b = mock(Book.class);

        Loan l = mock(Loan.class);
        when(l.getBook()).thenReturn(mock(Book.class));
        when(l.isActive()).thenReturn(true);

        when(loanService.getAllLoans()).thenReturn(List.of(l));

        assertNull(libraryService.findLoanUser(b));
    }

    @Test
    void borrowBook_whenOk_savesBookAndUsers() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(loanService.createLoan(u, b)).thenReturn(true);

        assertTrue(libraryService.borrowBook(u, b));

        verify(loanService, times(1)).createLoan(u, b);
        verify(bookService, times(1)).saveBooksToFile();
        verify(userService, times(1)).saveUsers();
    }

    @Test
    void borrowBook_whenFail_doesNotSave() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(loanService.createLoan(u, b)).thenReturn(false);

        assertFalse(libraryService.borrowBook(u, b));

        verify(loanService, times(1)).createLoan(u, b);
        verify(bookService, never()).saveBooksToFile();
        verify(userService, never()).saveUsers();
    }

    @Test
    void returnBook_whenOk_savesBookAndUsers() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(loanService.returnLoan(u, b)).thenReturn(true);

        assertTrue(libraryService.returnBook(u, b));

        verify(loanService, times(1)).returnLoan(u, b);
        verify(bookService, times(1)).saveBooksToFile();
        verify(userService, times(1)).saveUsers();
    }

    @Test
    void returnBook_whenFail_doesNotSave() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(loanService.returnLoan(u, b)).thenReturn(false);

        assertFalse(libraryService.returnBook(u, b));

        verify(loanService, times(1)).returnLoan(u, b);
        verify(bookService, never()).saveBooksToFile();
        verify(userService, never()).saveUsers();
    }

    @Test
    void borrowCD_whenOk_savesUsers() {
        User u = mock(User.class);
        CD cd = mock(CD.class);

        when(cdLoanService.createCDLoan(u, cd)).thenReturn(true);
        when(cdLoanService.getAllCDLoans()).thenReturn(new ArrayList<>());

        assertTrue(libraryService.borrowCD(u, cd));

        verify(cdLoanService, times(1)).createCDLoan(u, cd);
        verify(userService, times(1)).saveUsers();
    }

    @Test
    void borrowCD_whenFail_doesNotSave() {
        User u = mock(User.class);
        CD cd = mock(CD.class);

        when(cdLoanService.createCDLoan(u, cd)).thenReturn(false);

        assertFalse(libraryService.borrowCD(u, cd));

        verify(cdLoanService, times(1)).createCDLoan(u, cd);
        verify(userService, never()).saveUsers();
    }

    @Test
    void returnCD_whenOk_savesUsers() {
        User u = mock(User.class);
        CD cd = mock(CD.class);

        when(cdLoanService.returnCDLoan(u, cd)).thenReturn(true);
        when(cdLoanService.getAllCDLoans()).thenReturn(new ArrayList<>());

        assertTrue(libraryService.returnCD(u, cd));

        verify(cdLoanService, times(1)).returnCDLoan(u, cd);
        verify(userService, times(1)).saveUsers();
    }

    @Test
    void returnCD_whenFail_doesNotSave() {
        User u = mock(User.class);
        CD cd = mock(CD.class);

        when(cdLoanService.returnCDLoan(u, cd)).thenReturn(false);

        assertFalse(libraryService.returnCD(u, cd));

        verify(cdLoanService, times(1)).returnCDLoan(u, cd);
        verify(userService, never()).saveUsers();
    }

    @Test
    void getOverdueLoans_delegates() {
        List<Loan> list = List.of(mock(Loan.class));
        when(loanService.getOverdueLoans()).thenReturn(list);

        assertSame(list, libraryService.getOverdueLoans());
        verify(loanService, times(1)).getOverdueLoans();
    }

    @Test
    void getOverdueCDLoans_delegates() {
        List<CDLoan> list = List.of(mock(CDLoan.class));
        when(cdLoanService.getOverdueCDLoans()).thenReturn(list);

        assertSame(list, libraryService.getOverdueCDLoans());
        verify(cdLoanService, times(1)).getOverdueCDLoans();
    }

    @Test
    void getAllLoans_delegates() {
        List<Loan> list = List.of(mock(Loan.class));
        when(loanService.getAllLoans()).thenReturn(list);

        assertSame(list, libraryService.getAllLoans());
        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    void getAllCDLoans_delegates() {
        List<CDLoan> list = List.of(mock(CDLoan.class));
        when(cdLoanService.getAllCDLoans()).thenReturn(list);

        assertSame(list, libraryService.getAllCDLoans());
        verify(cdLoanService, times(1)).getAllCDLoans();
    }

    @Test
    void sendOverdueReminders_passesBothLists() {
        List<Loan> overdueBooks = List.of(mock(Loan.class), mock(Loan.class));
        List<CDLoan> overdueCds = List.of(mock(CDLoan.class));

        when(loanService.getOverdueLoans()).thenReturn(overdueBooks);
        when(cdLoanService.getOverdueCDLoans()).thenReturn(overdueCds);

        libraryService.sendOverdueReminders();

        verify(reminderService, times(1)).sendReminders(overdueBooks, overdueCds);
    }

    @Test
    void getAllUsers_delegates() {
        List<User> users = List.of(mock(User.class));
        when(userService.getAllUsers()).thenReturn(users);

        assertSame(users, libraryService.getAllUsers());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllBooks_delegates() {
        List<Book> books = List.of(mock(Book.class));
        when(bookService.getAllBooks()).thenReturn(books);

        assertSame(books, libraryService.getAllBooks());
        verify(bookService, times(1)).getAllBooks();
    }
}
