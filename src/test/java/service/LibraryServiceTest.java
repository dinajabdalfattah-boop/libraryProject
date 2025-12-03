package service;

import domain.Book;
import domain.Loan;
import domain.User;
import notification.MockNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryServiceTest {

    private LibraryService library;
    private ReminderService reminderService;

    private User u1, u2;
    private Book b1, b2;

    @BeforeEach
    public void setUp() {
        // constructor بدون loadAll
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
        assertTrue(library.getAllUsers().isEmpty());
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

    @Test
    public void testUnregisterUserNotExisting() {
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
        assertEquals(1, library.getAllLoans().size());
    }

    @Test
    public void testBorrowBookFailsDueToRules() {
        library.addUser(u1);
        library.addBook(b1);

        // نعطيه غرامة → يمنع الاستعارة داخل user.borrowBook
        u1.setFineBalance(20);

        assertFalse(library.borrowBook(u1, b1));
        assertTrue(library.getAllLoans().isEmpty());
    }

    @Test
    public void testBorrowBookFailsUserNotRegistered() {
        // فقط كتاب في المكتبة
        library.addBook(b1);

        assertFalse(library.borrowBook(u1, b1)); // u1 مش مضاف
    }

    @Test
    public void testBorrowBookFailsBookNotInLibrary() {
        // فقط يوزر في المكتبة
        library.addUser(u1);

        assertFalse(library.borrowBook(u1, b1)); // b1 مش مضاف
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
    // OVERDUE + REMINDERS
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

    @Test
    public void testGetOverdueLoansEmpty() {
        assertTrue(library.getOverdueLoans().isEmpty());
    }

    @Test
    public void testSendOverdueRemindersNoOverdue() {
        assertDoesNotThrow(() -> library.sendOverdueReminders());
    }

    @Test
    public void testSendOverdueRemindersWithOverdue() {
        library.addUser(u1);
        library.addBook(b1);

        assertTrue(library.borrowBook(u1, b1));

        // نخليها متأخرة
        Loan loan = library.getAllLoans().get(0);
        loan.setDueDate(LocalDate.now().minusDays(5));

        assertDoesNotThrow(() -> library.sendOverdueReminders());
    }

    // =========================
    // LOAD FROM FILES
    // =========================

    @Test
    public void testLoadAllFromFiles() {
        // نجهز ملفات الـ data مثل ما يتوقع LibraryService

        // USERS_FILE: name,email,fine
        List<String> userLines = new ArrayList<>();
        userLines.add("User1,u1@mail.com,10.5");
        userLines.add(""); // سطر فاضي يغطي line.isBlank()
        FileManager.writeLines("src/main/resources/data/users.txt", userLines);

        // BOOKS_FILE: title,author,isbn,available,borrowDate,dueDate
        List<String> bookLines = new ArrayList<>();
        // كتاب غير متاح مع تواريخ
        bookLines.add("BT1,BA1,111,false,2024-01-01,2024-01-10");
        // كتاب متاح مع nulls
        bookLines.add("BT2,BA2,222,true,null,null");
        FileManager.writeLines("src/main/resources/data/books.txt", bookLines);

        // LOANS_FILE: userName,isbn,dueDate
        List<String> loanLines = new ArrayList<>();
        loanLines.add("User1,111,2024-01-10");         // valid
        loanLines.add("Unknown,111,2024-01-10");       // user غير موجود
        loanLines.add("User1,999,2024-01-10");         // book غير موجود
        loanLines.add("");                             // سطر فاضي
        FileManager.writeLines("src/main/resources/data/loans.txt", loanLines);

        // نعمل LibraryService جديد مع loadFromFiles = true
        reminderService = new ReminderService();
        reminderService.addObserver(new MockNotifier());
        library = new LibraryService(reminderService, true); // يستدعي loadAll()

        // نتحقق من الـ users
        List<User> loadedUsers = library.getAllUsers();
        assertEquals(1, loadedUsers.size());
        User loadedU = loadedUsers.get(0);
        assertEquals("User1", loadedU.getUserName());
        assertEquals("u1@mail.com", loadedU.getEmail());
        assertEquals(10.5, loadedU.getFineBalance());

        // نتحقق من الـ books
        List<Book> loadedBooks = library.getAllBooks();
        assertEquals(2, loadedBooks.size());

        Book book111 = library.findBookByISBN("111");
        Book book222 = library.findBookByISBN("222");

        assertNotNull(book111);
        assertNotNull(book222);

        assertFalse(book111.isAvailable()); // من الملف false
        assertTrue(book222.isAvailable());  // من الملف true

        // نتحقق من القروض: لازم بس واحدة valid
        List<Loan> loadedLoans = library.getAllLoans();
        assertEquals(1, loadedLoans.size());

        Loan l = loadedLoans.get(0);
        assertEquals("User1", l.getUser().getUserName());
        assertEquals("111", l.getBook().getIsbn());
        assertEquals(LocalDate.parse("2024-01-10"), l.getDueDate());
    }
}
