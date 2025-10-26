package service;

import domain.Book;
import domain.Loan;
import domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer responsible for managing library operations.
 */
public class LibraryService {

    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final ReminderService reminderService;

    // ربط LibraryService مع ReminderService
    public LibraryService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * إضافة كتاب جديد إلى المكتبة
     */
    public void addBook(Book book) {
        books.add(book);
    }

    /**
     * إضافة مستخدم جديد
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * السماح للمستخدم باستعارة كتاب مع التحقق من قواعد Sprint 4
     */
    public void borrowBook(User user, Book book) {
        try {
            user.borrowBook(book); // يتحقق من القواعد (overdue books, unpaid fines)
            Loan loan = new Loan(user, book);
            loans.add(loan);
            System.out.println(user.getUserName() + " borrowed '" + book.getTitle() + "'");
        } catch (IllegalStateException e) {
            System.out.println("Cannot borrow book: " + e.getMessage());
        }
    }

    /**
     * استرجاع قائمة القروض المتأخرة
     */
    public List<Loan> getOverdueLoans() {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                overdue.add(loan);
            }
        }
        return overdue;
    }

    /**
     * إرسال تذكيرات للقروض المتأخرة باستخدام ReminderService
     */
    public void sendOverdueReminders() {
        List<Loan> overdueLoans = getOverdueLoans();
        reminderService.sendReminders(overdueLoans);
    }

    /**
     * إلغاء تسجيل المستخدم بعد التحقق من القروض النشطة والغرامات
     */
    public void unregisterUser(User user) {
        if (users.contains(user)) {
            if (user.canBeUnregistered()) {
                users.remove(user);
                System.out.println("User " + user.getUserName() + " unregistered successfully.");
            } else {
                System.out.println("Cannot unregister user " + user.getUserName() +
                        ": has active loans or unpaid fines.");
            }
        } else {
            System.out.println("User " + user.getUserName() + " not found.");
        }
    }

    /**
     * الحصول على جميع المستخدمين (اختياري)
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * الحصول على جميع الكتب (اختياري)
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    /**
     * الحصول على جميع القروض (اختياري)
     */
    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }
}
