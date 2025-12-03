package service;

import domain.Book;
import domain.Loan;
import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.FileManager;

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

        // نفرّغ ملف القروض قبل كل تيست
        FileManager.writeLines("src/main/resources/data/loans.txt", new ArrayList<>());
    }

    @Test
    public void testCreateLoanSuccess() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        boolean created = loanService.createLoan(u, b);

        assertTrue(created);
        List<Loan> loans = loanService.getAllLoans();
        assertEquals(1, loans.size());
        assertEquals(u, loans.get(0).getUser());
        assertEquals(b, loans.get(0).getBook());
    }

    @Test
    public void testCreateLoanFailsWhenBookAlreadyBorrowed() {
        User u = new User("Ali", "a@a.com");
        Book b = new Book("T1", "A1", "111");

        // نخلي الكتاب مستعار أصلاً
        b.borrowBook(LocalDate.now().minusDays(1));
        assertTrue(b.isBorrowed());

        boolean created = loanService.createLoan(u, b);

        assertFalse(created);
        assertTrue(loanService.getAllLoans().isEmpty());
    }

    @Test
    public void testGetOverdueLoans() {
        User u = new User("Ali", "a@a.com");
        Book b1 = new Book("T1", "A1", "111");
        Book b2 = new Book("T2", "A2", "222");

        loanService.createLoan(u, b1);
        loanService.createLoan(u, b2);

        List<Loan> all = loanService.getAllLoans();
        // الأول متأخر
        all.get(0).setDueDate(LocalDate.now().minusDays(10));
        // الثاني ليس متأخر
        all.get(1).setDueDate(LocalDate.now().plusDays(5));

        List<Loan> overdue = loanService.getOverdueLoans();
        assertEquals(1, overdue.size());
        assertEquals(b1, overdue.get(0).getBook());
    }

    @Test
    public void testLoadLoansFromFile() {
        // نضيف يوزر وكتاب في السيرفسات (عشان findUser / findBook يشتغلوا)
        userService.addUser("Ahmad", "a@a.com");
        bookService.addBook("T1", "A1", "111");

        LocalDate borrow = LocalDate.now().minusDays(3);
        LocalDate due = LocalDate.now().plusDays(7);

        List<String> lines = new ArrayList<>();
        // سطر صحيح
        lines.add("Ahmad,111," + borrow + "," + due);
        // user غير موجود
        lines.add("Unknown,111," + borrow + "," + due);
        // book غير موجود
        lines.add("Ahmad,9999," + borrow + "," + due);

        FileManager.writeLines("src/main/resources/data/loans.txt", lines);

        loanService.loadLoansFromFile();

        List<Loan> loans = loanService.getAllLoans();
        assertEquals(1, loans.size());

        Loan loan = loans.get(0);
        assertEquals("Ahmad", loan.getUser().getUserName());
        assertEquals("111", loan.getBook().getIsbn());
        assertEquals(due, loan.getDueDate());

        // تم إضافة الكتاب لقائمة borrowedBooks عند اليوزر
        User uFromService = userService.findUserByName("Ahmad");
        assertEquals(1, uFromService.getBorrowedBooks().size());
    }

    // ✅ TESTS ADDED لرفع الكافريج أكثر

    /** يغطي حالة: ما في أي قروض في السيستم */
    @Test
    public void testGetAllLoansInitiallyEmpty() {
        assertTrue(loanService.getAllLoans().isEmpty());
        assertTrue(loanService.getOverdueLoans().isEmpty());
    }

    /** يغطي حالة: في قروض لكن ولا واحد متأخر */
    @Test
    public void testGetOverdueLoansWhenNoneOverdue() {
        User u = new User("Mona", "m@m.com");
        Book b1 = new Book("T3", "A3", "333");
        Book b2 = new Book("T4", "A4", "444");

        loanService.createLoan(u, b1);
        loanService.createLoan(u, b2);

        // نخلي كل الـ due في المستقبل
        for (Loan l : loanService.getAllLoans()) {
            l.setDueDate(LocalDate.now().plusDays(10));
        }

        List<Loan> overdue = loanService.getOverdueLoans();
        assertTrue(overdue.isEmpty());
    }

    /** يغطي سطر line.isBlank() في loadLoansFromFile + حالة تجاهل كل الأسطر */
    @Test
    public void testLoadLoansFromFileWithBlankAndInvalid() {
        List<String> lines = new ArrayList<>();
        lines.add(""); // سطر فاضي
        // user/book مش موجودين → لازم يتجاهلهم
        lines.add("Ghost,9999,2024-01-01,2024-01-10");

        FileManager.writeLines("src/main/resources/data/loans.txt", lines);

        loanService.loadLoansFromFile();

        // ولا قرض ينضاف لأن user/book غير معروفين
        assertTrue(loanService.getAllLoans().isEmpty());
    }
}
