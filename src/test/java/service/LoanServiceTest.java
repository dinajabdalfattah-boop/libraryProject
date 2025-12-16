package service;

import domain.Book;
import domain.Loan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoanServiceTest {

    private LoanService loanService;
    private BookService bookService;
    private UserService userService;
    private static final String FILE = "src/main/resources/data/loans.txt";

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        userService = mock(UserService.class);
        loanService = new LoanService(bookService, userService);
    }

    @Test
    void createLoan_userHasFine_false() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(u.getFineBalance()).thenReturn(10.0);

        assertFalse(loanService.createLoan(u, b));
        assertTrue(loanService.getAllLoans().isEmpty());
    }

    @Test
    void createLoan_userHasOverdue_false() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(u.getFineBalance()).thenReturn(0.0);
        when(u.hasOverdueLoans()).thenReturn(true);

        assertFalse(loanService.createLoan(u, b));
        assertTrue(loanService.getAllLoans().isEmpty());
    }

    @Test
    void createLoan_bookBorrowed_false() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(u.getFineBalance()).thenReturn(0.0);
        when(u.hasOverdueLoans()).thenReturn(false);
        when(b.isBorrowed()).thenReturn(true);

        assertFalse(loanService.createLoan(u, b));
        assertTrue(loanService.getAllLoans().isEmpty());
    }

    @Test
    void createLoan_success_adds_and_saves() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(u.getFineBalance()).thenReturn(0.0);
        when(u.hasOverdueLoans()).thenReturn(false);
        when(b.isBorrowed()).thenReturn(false);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(loanService.createLoan(u, b));

            assertEquals(1, loanService.getAllLoans().size());
            verify(u, times(1)).addLoan(any(Loan.class));
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void saveAllLoansToFile_skips_invalid_loans_and_writes_only_valid() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        when(u.getUserName()).thenReturn("Ahmad");
        when(b.getIsbn()).thenReturn("111");

        Loan valid = mock(Loan.class);
        when(valid.getUser()).thenReturn(u);
        when(valid.getBook()).thenReturn(b);
        when(valid.getBorrowDate()).thenReturn(LocalDate.parse("2025-01-01"));
        when(valid.getDueDate()).thenReturn(LocalDate.parse("2025-01-20"));
        when(valid.isActive()).thenReturn(true);

        Loan nullLoan = null;

        Loan missingUser = mock(Loan.class);
        when(missingUser.getUser()).thenReturn(null);

        Loan missingBook = mock(Loan.class);
        when(missingBook.getUser()).thenReturn(u);
        when(missingBook.getBook()).thenReturn(null);

        Loan missingBorrow = mock(Loan.class);
        when(missingBorrow.getUser()).thenReturn(u);
        when(missingBorrow.getBook()).thenReturn(b);
        when(missingBorrow.getBorrowDate()).thenReturn(null);

        Loan missingDue = mock(Loan.class);
        when(missingDue.getUser()).thenReturn(u);
        when(missingDue.getBook()).thenReturn(b);
        when(missingDue.getBorrowDate()).thenReturn(LocalDate.parse("2025-01-01"));
        when(missingDue.getDueDate()).thenReturn(null);

        loanService.getAllLoans().add(nullLoan);
        loanService.getAllLoans().add(missingUser);
        loanService.getAllLoans().add(missingBook);
        loanService.getAllLoans().add(missingBorrow);
        loanService.getAllLoans().add(missingDue);
        loanService.getAllLoans().add(valid);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            loanService.saveAllLoansToFile();

            fm.verify(() -> FileManager.writeLines(eq(FILE), argThat(list -> {
                if (list == null) return false;
                List<?> lines = (List<?>) list;
                if (lines.size() != 1) return false;
                String s = String.valueOf(lines.get(0));
                return s.equals("Ahmad,111,2025-01-01,2025-01-20,true");
            })), times(1));
        }
    }

    @Test
    void loadLoansFromFile_linesNull_noCrash() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);

            assertDoesNotThrow(() -> loanService.loadLoansFromFile());
            assertTrue(loanService.getAllLoans().isEmpty());
        }
    }

    @Test
    void loadLoansFromFile_skips_invalid_lines_and_missing_refs() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(Arrays.asList(
                    null,
                    "",
                    "bad,short,line",
                    "Ghost,999,2025-01-01,2025-01-10,true",
                    "Ahmad,111,2025-01-01,2025-01-10,true"
            ));

            when(userService.findUserByName("Ghost")).thenReturn(null);
            when(bookService.findBookByISBN("999")).thenReturn(null);

            User u = mock(User.class);
            Book b = mock(Book.class);
            when(userService.findUserByName("Ahmad")).thenReturn(u);
            when(bookService.findBookByISBN("111")).thenReturn(b);
            when(u.getActiveBookLoans()).thenReturn(new ArrayList<>());

            loanService.loadLoansFromFile();

            assertEquals(1, loanService.getAllLoans().size());
            assertEquals(1, u.getActiveBookLoans().size());
        }
    }

    @Test
    void loadLoansFromFile_inactive_sets_book_available_and_not_added_to_user_list() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(List.of("Ahmad,111,2025-01-01,2025-01-10,false"));

            User u = mock(User.class);
            Book b = mock(Book.class);

            when(userService.findUserByName("Ahmad")).thenReturn(u);
            when(bookService.findBookByISBN("111")).thenReturn(b);

            List<Loan> userLoans = new ArrayList<>();
            when(u.getActiveBookLoans()).thenReturn(userLoans);

            loanService.loadLoansFromFile();

            assertEquals(1, loanService.getAllLoans().size());
            assertTrue(userLoans.isEmpty());

            verify(b, times(1)).setAvailable(true);
            verify(b, times(1)).setBorrowDate(null);
            verify(b, times(1)).setDueDate(null);
        }
    }

    @Test
    void returnLoan_noMatch_false() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        assertFalse(loanService.returnLoan(u, b));
    }

    @Test
    void returnLoan_skips_inactive_and_returns_false() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(u);
        when(loan.getBook()).thenReturn(b);
        when(loan.isActive()).thenReturn(false);

        loanService.getAllLoans().add(loan);

        assertFalse(loanService.returnLoan(u, b));
        verify(u, never()).returnLoan(any());
    }

    @Test
    void returnLoan_userMatches_bookDiffers_false() {
        User u = mock(User.class);
        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(u);
        when(loan.getBook()).thenReturn(b1);
        when(loan.isActive()).thenReturn(true);

        loanService.getAllLoans().add(loan);

        assertFalse(loanService.returnLoan(u, b2));
    }

    @Test
    void returnLoan_bookMatches_userDiffers_false() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        Book b = mock(Book.class);

        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(u1);
        when(loan.getBook()).thenReturn(b);
        when(loan.isActive()).thenReturn(true);

        loanService.getAllLoans().add(loan);

        assertFalse(loanService.returnLoan(u2, b));
    }

    @Test
    void returnLoan_success_updates_and_saves() {
        User u = mock(User.class);
        Book b = mock(Book.class);

        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(u);
        when(loan.getBook()).thenReturn(b);
        when(loan.isActive()).thenReturn(true);

        loanService.getAllLoans().add(loan);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(loanService.returnLoan(u, b));

            verify(loan, times(1)).returnBook();
            verify(u, times(1)).returnLoan(loan);
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void getOverdueLoans_filters_overdue_only() {
        Loan overdue = mock(Loan.class);
        Loan ok = mock(Loan.class);

        when(overdue.isOverdue()).thenReturn(true);
        when(ok.isOverdue()).thenReturn(false);

        loanService.getAllLoans().add(overdue);
        loanService.getAllLoans().add(ok);

        List<Loan> res = loanService.getOverdueLoans();

        assertEquals(1, res.size());
        assertTrue(res.contains(overdue));
        assertFalse(res.contains(ok));
    }

    @Test
    void getOverdueLoans_empty_when_none() {
        assertTrue(loanService.getOverdueLoans().isEmpty());
    }

    @Test
    void getAllLoans_returns_live_list() {
        assertTrue(loanService.getAllLoans().isEmpty());
        loanService.getAllLoans().add(mock(Loan.class));
        assertEquals(1, loanService.getAllLoans().size());
    }
}
