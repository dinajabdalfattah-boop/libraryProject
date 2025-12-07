package domain;

import domain.fine.BookFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * This class represents a loan for a single book in the library system.
 * A loan connects a user with the book they borrowed and stores the borrow date,
 * due date, and a fine strategy that determines the overdue fine.
 * The default borrowing period for books is 28 days.
 */
public class Loan {

    private final User user;
    private final Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private FineStrategy fineStrategy;
    private boolean active = true;

    /**
     * Creates a loan with a custom fine strategy.
     * The borrow date is set to today, and the due date is set to 28 days later.
     *
     * @param user the user borrowing the book
     * @param book the book being borrowed
     * @param fineStrategy the strategy used to calculate overdue fines
     */
    public Loan(User user, Book book, FineStrategy fineStrategy) {
        this.user = user;
        this.book = book;
        this.fineStrategy = fineStrategy;

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);

        // Mark the book as borrowed in the book object
        book.borrowBook(borrowDate);
    }

    /**
     * Creates a standard book loan using the default BookFineStrategy.
     *
     * @param user the user borrowing the book
     * @param book the book being borrowed
     */
    public Loan(User user, Book book) {
        this(user, book, new BookFineStrategy());
    }

    /**
     * Ends the loan and marks the book as available again.
     * Once returned, the loan becomes inactive.
     */
    public void returnBook() {
        if (!active) return;
        this.active = false;
        book.returnBook();
    }

    /**
     * Checks whether the loan is still active.
     *
     * @return true if the book has not been returned yet
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks if the loan is overdue based on a specific date.
     *
     * @param date the date used for comparison
     * @return true if the book should have been returned before this date
     */
    public boolean isOverdue(LocalDate date) {
        return active && dueDate != null && date.isAfter(dueDate);
    }

    /**
     * Checks if the loan is overdue as of today.
     *
     * @return true if today is past the due date
     */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /**
     * Calculates how many days the loan is overdue.
     * Returns 0 if the book is not overdue.
     *
     * @return overdue days as an integer
     */
    public int getOverdueDays() {
        if (!isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /**
     * Calculates the fine based on the overdue days.
     * Uses the FineStrategy object assigned to this loan.
     *
     * @return the fine amount
     */
    public int calculateFine() {
        return fineStrategy.calculateFine(getOverdueDays());
    }

    /**
     * Sets the borrow date manually.
     * Usually used when loading data from a file or during testing.
     *
     * @param borrowDate the date the book was borrowed
     */
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    /**
     * Sets the due date manually.
     * Mainly used when restoring loan data from storage.
     *
     * @param dueDate the expected return date
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Changes the fine strategy used for this loan.
     * Helpful for testing different fine behaviors.
     *
     * @param fineStrategy the new fine strategy
     */
    public void setFineStrategy(FineStrategy fineStrategy) {
        this.fineStrategy = fineStrategy;
    }

    /** @return the user who borrowed the book */
    public User getUser() { return user; }

    /** @return the book being loaned */
    public Book getBook() { return book; }

    /** @return the borrow date of the loan */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** @return the due date for the loan */
    public LocalDate getDueDate() { return dueDate; }

    /** @return the fine strategy applied to this loan */
    public FineStrategy getFineStrategy() { return fineStrategy; }

    /**
     * Returns a readable description of the loan.
     * Mainly useful for debugging and printing loan information.
     *
     * @return formatted string describing the loan
     */
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
