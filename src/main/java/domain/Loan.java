package domain;

import domain.fine.BookFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a loan of a single book to a user.
 * Default loan period is 28 days for books.
 */
public class Loan {

    private final User user;
    private final Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate;

    /** Strategy used to calculate fines for this loan. */
    private FineStrategy fineStrategy;

    /** Indicates if this loan is still active. */
    private boolean active = true;

    /**
     * Creates a loan with a custom fine strategy.
     * Borrow date = today, due date = today + 28 days.
     */
    public Loan(User user, Book book, FineStrategy fineStrategy) {
        this.user = user;
        this.book = book;
        this.fineStrategy = fineStrategy;

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);

        // Mark book as borrowed
        book.borrowBook(borrowDate);
    }

    /**
     * Creates a normal book loan with default BookFineStrategy.
     */
    public Loan(User user, Book book) {
        this(user, book, new BookFineStrategy());
    }

    // ---------- Loan lifecycle ----------

    /** Ends the loan and makes the book available again. */
    public void returnBook() {
        if (!active) return;
        this.active = false;
        book.returnBook();
    }

    public boolean isActive() {
        return active;
    }

    // ---------- Overdue & fine logic ----------

    public boolean isOverdue(LocalDate date) {
        return active && dueDate != null && date.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    public int getOverdueDays() {
        if (!isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public int calculateFine() {
        return fineStrategy.calculateFine(getOverdueDays());
    }

    // ---------- Setters (for tests / persistence) ----------

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setFineStrategy(FineStrategy fineStrategy) {
        this.fineStrategy = fineStrategy;
    }

    // ---------- Getters ----------

    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public FineStrategy getFineStrategy() { return fineStrategy; }

    @Override
    public String toString() {
        return "Loan{" +
                "user=" + user.getUserName() +
                ", book=" + book.getTitle() +
                ", borrow=" + borrowDate +
                ", due=" + dueDate +
                ", active=" + active +
                '}';
    }
}
