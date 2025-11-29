package service;

import domain.Book;
import domain.Loan;
import domain.User;
import notification.MockNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReminderServiceTest {

    private ReminderService reminderService;
    private MockNotifier notifier;

    private User user;
    private Book book;
    private Loan loan;

    private String name;
    private String title;
    private String author;
    private String isbn;

    @BeforeEach
    public void setUp() {
        reminderService = new ReminderService();

        notifier = new MockNotifier();
        reminderService.addObserver(notifier);

        name = "UserA";
        title = "BookA";
        author = "AuthorA";
        isbn = "1234";

        user = new User(name);
        book = new Book(title, author, isbn);

        book.borrowBook(LocalDate.now().minusDays(40));
        loan = new Loan(user, book);
        user.getBorrowedBooks().add(book);
    }

    @Test
    public void testAddObserverSuccess() {
        ReminderService rs = new ReminderService();
        assertTrue(rs.addObserver(new MockNotifier()));
    }

    @Test
    public void testAddObserverDuplicateFails() {
        ReminderService rs = new ReminderService();

        MockNotifier obs = new MockNotifier();
        assertTrue(rs.addObserver(obs));
        assertFalse(rs.addObserver(obs));
    }

    @Test
    public void testSendRemindersSuccess() {
        List<Loan> overdue = new ArrayList<>();
        overdue.add(loan);

        boolean result = reminderService.sendReminders(overdue);

        assertTrue(result);
        assertEquals(1, notifier.getMessages().size());
        assertTrue(notifier.getMessages().get(0).contains(user.getUserName()));
    }

    @Test
    public void testSendRemindersMultipleLoans() {
        List<Loan> overdue = new ArrayList<>();

        Book b2 = new Book("X", "Y", "999");
        b2.borrowBook(LocalDate.now().minusDays(50));
        Loan loan2 = new Loan(user, b2);
        user.getBorrowedBooks().add(b2);

        overdue.add(loan);
        overdue.add(loan2);

        boolean result = reminderService.sendReminders(overdue);

        assertTrue(result);
        assertEquals(2, notifier.getMessages().size());
    }

    @Test
    public void testSendRemindersNoOverdue() {
        List<Loan> empty = new ArrayList<>();
        boolean result = reminderService.sendReminders(empty);

        assertFalse(result);
        assertEquals(0, notifier.getMessages().size());
    }
}
