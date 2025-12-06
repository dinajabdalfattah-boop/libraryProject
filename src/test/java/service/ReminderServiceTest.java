package service;

import domain.*;
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

    private User userA;
    private User userB;

    @BeforeEach
    public void setUp() {
        reminderService = new ReminderService();
        notifier = new MockNotifier();
        reminderService.addObserver(notifier);

        userA = new User("UserA", "a@mail.com");
        userB = new User("UserB", "b@mail.com");
    }

    // ---------------------------------------------------------
    // OBSERVER TESTS
    // ---------------------------------------------------------

    @Test
    public void testAddObserverSuccess() {
        ReminderService rs = new ReminderService();
        assertTrue(rs.addObserver(new MockNotifier()));
    }

    @Test
    public void testAddObserverNullFails() {
        ReminderService rs = new ReminderService();
        assertFalse(rs.addObserver(null));
    }

    @Test
    public void testAddObserverDuplicateFails() {
        ReminderService rs = new ReminderService();
        MockNotifier m = new MockNotifier();
        assertTrue(rs.addObserver(m));
        assertFalse(rs.addObserver(m));
    }

    // ---------------------------------------------------------
    // SEND REMINDERS TESTS
    // ---------------------------------------------------------

    @Test
    public void testSendRemindersNoOverdue() {
        reminderService.sendReminders(new ArrayList<>(), new ArrayList<>());
        assertEquals(0, notifier.getMessages().size());
    }

    @Test
    public void testSendRemindersOverdueBooksOnly() {
        Book b = new Book("B1", "A1", "111");

        Loan l = new Loan(userA, b);
        l.setDueDate(LocalDate.now().minusDays(10)); // overdue

        reminderService.sendReminders(List.of(l), new ArrayList<>());

        assertEquals(1, notifier.getMessages().size());
        assertTrue(notifier.getMessages().get(0).contains("1 overdue item"));
        assertTrue(notifier.getMessages().get(0).contains("UserA"));
    }

    @Test
    public void testSendRemindersOverdueCDsOnly() {
        CD cd = new CD("CD1", "Artist", "CD01");
        CDLoan cdLoan = new CDLoan(userB, cd);
        cdLoan.setDueDate(LocalDate.now().minusDays(5));

        reminderService.sendReminders(new ArrayList<>(), List.of(cdLoan));

        assertEquals(1, notifier.getMessages().size());
        assertTrue(notifier.getMessages().get(0).contains("UserB"));
    }

    @Test
    public void testSendRemindersMixedBookAndCD() {
        Book b = new Book("B1", "A1", "111");
        Loan l = new Loan(userA, b);
        l.setDueDate(LocalDate.now().minusDays(5));

        CD cd = new CD("CD1", "X", "CD01");
        CDLoan cdLoan = new CDLoan(userA, cd);
        cdLoan.setDueDate(LocalDate.now().minusDays(3));

        reminderService.sendReminders(List.of(l), List.of(cdLoan));

        assertEquals(1, notifier.getMessages().size());
        assertTrue(notifier.getMessages().get(0).contains("2 overdue item"));
        assertTrue(notifier.getMessages().get(0).contains("UserA"));
    }

    @Test
    public void testSendRemindersTwoDifferentUsers() {
        Book b1 = new Book("B1", "A1", "111");
        Loan l1 = new Loan(userA, b1);
        l1.setDueDate(LocalDate.now().minusDays(5));

        CD cd = new CD("CD1", "Art", "CD01");
        CDLoan cdLoan = new CDLoan(userB, cd);
        cdLoan.setDueDate(LocalDate.now().minusDays(7));

        reminderService.sendReminders(List.of(l1), List.of(cdLoan));

        assertEquals(2, notifier.getMessages().size());
    }
}
