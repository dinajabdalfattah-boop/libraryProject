package domain;

import java.time.LocalDate;

/**
 * Represents a book loan in the library system.
 * Each loan connects a user with a borrowed book and has a due date.
 * @author Dina
 * @version 1.1
 */
public class Loan {

    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;

    /**
     * Creates a new loan for a given user and book.
     * The due date is usually 28 days from the borrow date.
     *
     * @param user The user who borrowed the book.
     * @param book The book being borrowed.
     */
    public Loan(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();       // ✅ التعيين الصحيح للتاريخ الحالي
        this.dueDate = borrowDate.plusDays(28); // 28 يوم كفترة استعارة
    }

    /** Checks if the loan is overdue based on the current date. */
    public boolean isOverdue(LocalDate currentDate) {
        return currentDate.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "user=" + user.getUserName() +
                ", book=" + book.getTitle() +
                ", dueDate=" + dueDate +
                '}';
    }
}
