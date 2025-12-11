package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * This class represents a CD item in the library system.
 * Each CD has a title, artist, and a unique ID.
 * CDs can be borrowed for 7 days and may become overdue.
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
     */
    public CD(String title, String artist, String id) {
        this.title = title;
        this.artist = artist;
        this.id = id;
    }

    /**
     * Borrows the CD starting from the given date.
     * Due date is automatically set to 7 days later.
     */
    public void borrowCD(LocalDate date) {
        if (!available) {
            throw new IllegalStateException("CD is already borrowed!");
        }
        this.available = false;
        this.borrowDate = date;
        this.dueDate = date.plusDays(7);
    }

    /** Borrows the CD today. */
    public void borrowCD() {
        borrowCD(LocalDate.now());
    }

    /** Returns the CD and clears borrowing information. */
    public void returnCD() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    /** Checks if overdue based on a specific date. */
    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    /** Checks if overdue today. */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /** Calculates remaining days (negative if overdue). */
    public int getRemainingDays(LocalDate today) {
        if (dueDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(today, dueDate);
    }

    public boolean isAvailable() {
        return available;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getId() {
        return id;
    }

    /** Restores the borrow date when loading from a file. */
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    /** Restores the due date when loading from a file. */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** String representation for debugging. */
    @Override
    public String toString() {
        return String.format(
                "CD[%s by %s, ID=%s, Available=%s, Borrowed=%s, Due=%s]",
                title, artist, id, available, borrowDate, dueDate
        );
    }
}
