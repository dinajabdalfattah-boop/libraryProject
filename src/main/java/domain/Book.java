package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * This class represents a book in the library system.
 * Each book has basic information like title, author, and ISBN.
 * A book can be borrowed for a fixed period of 28 days, after which it becomes overdue.
 */
public class Book {

    private final String title;
    private final String author;
    private final String isbn;
    private boolean available;
    private LocalDate borrowDate;
    private LocalDate dueDate;

    /**
     * Creates a new book instance. Books are created as available by default.
     *
     * @param title  the book's title
     * @param author the book's author
     * @param isbn   a unique identifier for the book
     */
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }

    /**
     * Checks whether the book is currently available for borrowing.
     *
     * @return true if the book is not borrowed, otherwise false
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Marks the book as borrowed starting from the given date.
     * Automatically sets the due date to 28 days after borrowing.
     *
     * @param borrowDate the date when the user borrowed the book
     * @throws IllegalStateException if the book is already borrowed
     */
    public void borrowBook(LocalDate borrowDate) {
        if (!available) {
            throw new IllegalStateException("Book is already borrowed!");
        }

        this.available = false;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(28);
    }

    /**
     * Borrows the book starting from today's date.
     * This is a convenience method for typical borrowing cases.
     */
    public void borrowBook() {
        borrowBook(LocalDate.now());
    }

    /**
     * Returns the book to the library.
     * This resets the availability and clears borrowing information.
     */
    public void returnBook() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    /**
     * Checks whether the book is overdue as of the given date.
     *
     * @param currentDate the date used to compare against the due date
     * @return true if the book should have already been returned, otherwise false
     */
    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    /**
     * Checks if the book is overdue today.
     *
     * @return true if overdue today, false otherwise
     */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /**
     * Calculates how many days remain until the due date.
     * A negative result means the book is already overdue.
     *
     * @param today the date to compare with the due date
     * @return number of remaining days (positive or negative)
     */
    public int getRemainingDays(LocalDate today) {
        if (dueDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(today, dueDate);
    }

    /**
     * @return the date when the book must be returned
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * @return the date when the book was borrowed
     */
    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    /**
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the name of the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the unique ISBN of the book
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Checks if the book is currently borrowed.
     *
     * @return true if borrowed, false if available
     */
    public boolean isBorrowed() {
        return !available;
    }

    /**
     * Sets whether the book is available or not.
     * This is mainly used when loading data from files.
     *
     * @param available new availability state
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Sets the borrow date manually.
     * Useful when restoring data from persistent storage.
     *
     * @param borrowDate the date the book was borrowed
     */
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    /**
     * Sets the due date manually (e.g., when loading saved data).
     *
     * @param dueDate the new due date
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns a readable string describing the book and its borrow status.
     *
     * @return formatted string with book details
     */
    @Override
    public String toString() {
        return String.format(
                "Book[%s by %s, ISBN=%s, Available=%s, BorrowDate=%s, DueDate=%s]",
                title, author, isbn, available, borrowDate, dueDate
        );
    }
}
