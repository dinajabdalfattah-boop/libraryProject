package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a book in the library.
 * Books can be borrowed for 28 days.
 */
public class Book {

    // ---------- Identity & basic info ----------

    private final String title;
    private final String author;
    private final String isbn;

    // ---------- State ----------

    /** true if the book is currently available for borrowing. */
    private boolean available;

    /** Date when the book was borrowed. */
    private LocalDate borrowDate;

    /** Due date for returning the book. */
    private LocalDate dueDate;

    /**
     * Creates a new available book.
     *
     * @param title  book title
     * @param author book author
     * @param isbn   unique ISBN
     */
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }

    // ---------- Borrow / Return logic ----------

    public boolean isAvailable() {
        return available;
    }

    /**
     * Marks this book as borrowed starting from the given date
     * for 28 days.
     */
    public void borrowBook(LocalDate borrowDate) {
        if (!available) {
            throw new IllegalStateException("Book is already borrowed!");
        }

        this.available = false;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(28);
    }

    /** Borrow starting from today. */
    public void borrowBook() {
        borrowBook(LocalDate.now());
    }

    /** Marks this book as returned and clears borrowing dates. */
    public void returnBook() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    // ---------- Overdue helpers ----------

    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /**
     * Calculates remaining days until due date.
     * Returns positive if still within time, negative if overdue.
     */
    public int getRemainingDays(LocalDate today) {
        if (dueDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(today, dueDate);
    }

    // ---------- Getters ----------

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isBorrowed() {
        return !available;
    }

    // ---------- Setters (mainly for loading from file) ----------

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return String.format(
                "Book[%s by %s, ISBN=%s, Available=%s, BorrowDate=%s, DueDate=%s]",
                title, author, isbn, available, borrowDate, dueDate
        );
    }
}
