package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * This class represents a CD item in the library system.
 * Each CD has a title, artist, and a unique ID.
 * CDs can be borrowed for a shorter period than books, specifically 7 days.
 */
public class CD {

    private final String title;
    private final String artist;
    private final String id;
    private boolean available = true;
    private LocalDate borrowDate;
    private LocalDate dueDate;

    /**
     * Creates a new CD with the given title, artist, and ID.
     * CDs are available by default when created.
     *
     * @param title  the title of the CD
     * @param artist the artist of the CD
     * @param id     the unique ID of the CD
     */
    public CD(String title, String artist, String id) {
        this.title = title;
        this.artist = artist;
        this.id = id;
    }

    /**
     * Borrows the CD starting from the given date.
     * The due date is automatically set to 7 days after borrowing.
     *
     * @param date the date when the CD is borrowed
     * @throws IllegalStateException if the CD is already borrowed
     */
    public void borrowCD(LocalDate date) {
        if (!available) {
            throw new IllegalStateException("CD is already borrowed!");
        }
        this.available = false;
        this.borrowDate = date;
        this.dueDate = date.plusDays(7);
    }

    /**
     * Borrows the CD starting from today's date.
     * This makes borrowing easier without manually passing a date.
     */
    public void borrowCD() {
        borrowCD(LocalDate.now());
    }

    /**
     * Returns the CD back to the library.
     * This clears the borrow information and sets it as available again.
     */
    public void returnCD() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    /**
     * Checks if the CD is overdue based on a specific date.
     *
     * @param currentDate the date to compare with the due date
     * @return true if the CD is overdue, otherwise false
     */
    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    /**
     * Checks if the CD is overdue today.
     *
     * @return true if overdue today, otherwise false
     */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /**
     * Calculates how many days remain before the due date.
     * A negative number means the CD is already overdue.
     *
     * @param today the date used for comparison
     * @return positive or negative number representing remaining or overdue days
     */
    public int getRemainingDays(LocalDate today) {
        if (dueDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(today, dueDate);
    }

    /**
     * @return true if the CD is available for borrowing, otherwise false
     */
    public boolean isAvailable() { return available; }

    /**
     * @return the date when the CD was borrowed
     */
    public LocalDate getBorrowDate() { return borrowDate; }

    /**
     * @return the due date for returning the CD
     */
    public LocalDate getDueDate() { return dueDate; }

    /**
     * @return the CD's title
     */
    public String getTitle() { return title; }

    /**
     * @return the name of the artist of the CD
     */
    public String getArtist() { return artist; }

    /**
     * @return the unique ID of the CD
     */
    public String getId() { return id; }

    /**
     * Provides a readable summary of the CD, mainly for debugging.
     *
     * @return formatted text describing the CD's state
     */
    @Override
    public String toString() {
        return String.format(
                "CD[%s by %s, ID=%s, Available=%s, BorrowDate=%s, DueDate=%s]",
                title, artist, id, available, borrowDate, dueDate
        );
    }
}
