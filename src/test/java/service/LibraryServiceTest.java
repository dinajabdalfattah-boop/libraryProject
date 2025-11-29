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

    private LibraryService libraryService;
    private MockNotifier notifier;

    private User user1;
    private User user2;

    private Book book1;
    private Book book2;

    private String name1;
    private String name2;

    private String title1;
    private String title2;

    private String author1;
    private String author2;

    private String isbn1;
    private String isbn2;

    @BeforeEach
    public void setUp() {

        notifier = new MockNotifier();
        ReminderService reminderService = new ReminderService();
        reminderService.addObserver(notifier);

        libraryService = new LibraryService(reminderService);

        name1 = "UserA";
        name2 = "UserB";

        title1 = "BookA";
        title2 = "BookB";

        author1 = "AuthorA";
        author2 = "AuthorB";

        isbn1 = "1111";
        isbn2 = "2222";

        user1 = new User(name1);
        user2 = new User(name2);

        book1 = new Book(title1, author1, isbn1);
        book2 = new Book(title2, author2, isbn2);
    }

    @Test
    public void testAddBookSuccess() {
        assertTrue(libraryService.addBook(book1));
        assertEquals(1, libraryService.getAllBooks().size());
    }

    @Test
    public void testAddBookDuplicateISBN() {
        assertTrue(libraryService.addBook(book1));
        assertFalse(libraryService.addBook(new Book("X", "Y", isbn1)));

        assertEquals(1, libraryService.getAllBooks().size());
    }

    @Test
    public void testAddUserSuccess() {
        assertTrue(libraryService.addUser(user1));
        assertEquals(1, libraryService.getAllUsers().size());
    }

    @Test
    public void testAddUserDuplicate() {
        assertTrue(libraryService.addUser(user1));
        assertFalse(libraryService.addUser(new User(name1)));

        assertEquals(1, libraryService.getAllUsers().size());
    }

    @Test
    public void testBorrowBookSuccess() {
        libraryService.addUser(user1);
        libraryService.addBook(book1);

        assertTrue(libraryService.borrowBook(user1, book1));
        assertEquals(1, libraryService.getAllLoans().size());

        Loan loan = libraryService.getAllLoans().get(0);
        assertEquals(book1, loan.getBook());
        assertEquals(user1, loan.getUser());
    }

    @Test
    public void testBorrowBookFailsDueToRules() {
        libraryService.addUser(user1);
        libraryService.addBook(book1);

        user1.setFineBalance(20);

        assertFalse(libraryService.borrowBook(user1, book1));
        assertTrue(libraryService.getAllLoans().isEmpty());
    }

    @Test
    public void testGetOverdueLoans() {
        libraryService.addUser(user1);
        libraryService.addBook(book1);

        book1.borrowBook(LocalDate.now().minusDays(40));
        Loan loan = new Loan(user1, book1);
        libraryService.getAllLoans().add(loan);

        List<Loan> overdue = libraryService.getOverdueLoans();

        assertEquals(1, overdue.size());
        assertEquals(book1, overdue.get(0).getBook());
    }

    @Test
    public void testGetOverdueLoansEmpty() {
        assertTrue(libraryService.getOverdueLoans().isEmpty());
    }

    @Test
    public void testSendOverdueReminders() {
        libraryService.addUser(user1);
        libraryService.addBook(book1);

        book1.borrowBook(LocalDate.now().minusDays(40));
        Loan loan = new Loan(user1, book1);
        libraryService.getAllLoans().add(loan);

        libraryService.sendOverdueReminders();

        assertEquals(1, notifier.getMessages().size());
        assertTrue(notifier.getMessages().get(0).contains(user1.getUserName()));
    }

    @Test
    public void testUnregisterUserSuccess() {
        libraryService.addUser(user1);

        assertTrue(libraryService.unregisterUser(user1));
        assertTrue(libraryService.getAllUsers().isEmpty());
    }

    @Test
    public void testUnregisterUserFailsNotFound() {
        assertFalse(libraryService.unregisterUser(new User("Unknown")));
    }

    @Test
    public void testUnregisterUserFailsDueToActiveLoans() {
        libraryService.addUser(user1);
        libraryService.addBook(book1);

        libraryService.borrowBook(user1, book1);

        assertFalse(libraryService.unregisterUser(user1));
        assertEquals(1, libraryService.getAllUsers().size());
    }

    @Test
    public void testUnregisterUserFailsDueToFine() {
        libraryService.addUser(user1);

        user1.setFineBalance(10);
        assertFalse(libraryService.unregisterUser(user1));
    }

    @Test
    public void testGetAllListsDefaultEmpty() {
        assertTrue(libraryService.getAllBooks().isEmpty());
        assertTrue(libraryService.getAllUsers().isEmpty());
        assertTrue(libraryService.getAllLoans().isEmpty());
    }
}
