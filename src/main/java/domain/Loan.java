package domain;

import java.time.LocalDate;

/**
 * Represents a loan between a user and a book.
 * Stores borrow date, due date, and overdue logic.
 */
public class Loan {

    private final User user;
    private final Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate;

    /**
     * Creates a loan today using book.borrowBook()
     */
    public Loan(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);
    }

    // ========================
    //       GETTERS
    // ========================

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

    // ========================
    //     IS OVERDUE LOGIC
    // ========================

    /**
     * Check overdue with a custom date (used in tests)
     */
    public boolean isOverdue(LocalDate date) {
        return dueDate != null && date.isAfter(dueDate);
    }

    /**
     * Check overdue using today's date
     */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    // ========================
    //     SETTERS (needed for file loading)
    // ========================

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // ========================
    //        toString
    // ========================

    @Override
    public String toString() {
        return "Loan{" +
                "user=" + user.getUserName() +
                ", book=" + book.getTitle() +
                ", borrow=" + borrowDate +
                ", due=" + dueDate +
                '}';
    }
}
