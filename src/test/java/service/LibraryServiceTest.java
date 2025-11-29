package service;

import domain.Book;
import domain.Loan;
import domain.User;
import notification.MockNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryServiceTest {

    private LibraryService library;
    private ReminderService reminderService;

    private User u1, u2;
    private Book b1, b2;

    @BeforeEach
    public void setUp() {

        // نستخدم الـ constructor اللي ما بعمل loadAll()
        reminderService = new ReminderService();
        reminderService.addObserver(new MockNotifier());

        library = new LibraryService(reminderService, false);

        // Test data
        u1 = new User("Ahmad", "a@a.com");
        u2 = new User("Sara", "s@s.com");

        b1 = new Book("T1", "A1", "111");
        b2 = new Book("T2", "A2", "222");
    }

    // =========================
    // USERS TESTS
    // =========================

    @Test
    public void testAddUserSuccess() {
        assertTrue(library.addUser(u1));
        assertEquals(1, library.getAllUsers().size());
    }

    @Test
    public void testAddUserDuplicate() {
        library.addUser(u1);
        assertFalse(library.addUser(u1)); // duplicate
    }

    @Test
    public void testUnregisterUserSuccess() {
        library.addUser(u1);
        assertTrue(library.unregisterUser(u1));
    }

    @Test
    public void testUnregisterUserFailsDueToActiveLoans() {
        library.addUser(u1);
        library.addBook(b1);

        library.borrowBook(u1, b1); // صار عنده كتاب

        assertFalse(library.unregisterUser(u1));
    }

    @Test
    public void testUnregisterUserFailsDueToFine() {
        library.addUser(u1);
        u1.setFineBalance(10);

        assertFalse(library.unregisterUser(u1));
    }

    // =========================
    // BOOK TESTS
    // =========================

    @Test
    public void testAddBookSuccess() {
        assertTrue(library.addBook(b1));
        assertEquals(1, library.getAllBooks().size());
    }

    @Test
    public void testAddBookDuplicateISBN() {
        library.addBook(b1);
        assertFalse(library.addBook(b1)); // duplicate
    }

    // =========================
    // BORROW TESTS
    // =========================

    @Test
    public void testBorrowBookSuccess() {
        library.addUser(u1);
        library.addBook(b1);

        assertTrue(library.borrowBook(u1, b1));
    }

    @Test
    public void testBorrowBookFailsDueToRules() {
        library.addUser(u1);
        library.addBook(b1);

        // نعطيه غرامة → لازم يمنع الاستعارة
        u1.setFineBalance(20);

        assertFalse(library.borrowBook(u1, b1));
    }

    // =========================
    // GET LISTS TESTS
    // =========================

    @Test
    public void testGetAllListsDefaultEmpty() {
        assertTrue(library.getAllUsers().isEmpty());
        assertTrue(library.getAllBooks().isEmpty());
        assertTrue(library.getAllLoans().isEmpty());
    }

    // =========================
    // OVERDUE TESTS
    // =========================

    @Test
    public void testGetOverdueLoans() {
        library.addUser(u1);
        library.addBook(b1);

        library.borrowBook(u1, b1);

        // نخلي الكتاب متأخر
        Loan loan = library.getAllLoans().get(0);
        loan.setDueDate(LocalDate.now().minusDays(10));

        assertEquals(1, library.getOverdueLoans().size());
    }

}
