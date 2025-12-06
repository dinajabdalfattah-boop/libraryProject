package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a CD in the library.
 * CDs can be borrowed for 7 days.
 */
public class CD {

    private final String title;
    private final String artist;
    private final String id;

    /** Availability status */
    private boolean available = true;

    /** Borrow date */
    private LocalDate borrowDate;

    /** Due date (7 days from borrow date) */
    private LocalDate dueDate;

    public CD(String title, String artist, String id) {
        this.title = title;
        this.artist = artist;
        this.id = id;
    }

    // ---------- Borrow / Return Logic ----------

    /** Borrow CD for 7 days starting from given date */
    public void borrowCD(LocalDate date) {
        if (!available) {
            throw new IllegalStateException("CD is already borrowed!");
        }
        this.available = false;
        this.borrowDate = date;
        this.dueDate = date.plusDays(7);
    }

    /** Borrow starting from today */
    public void borrowCD() {
        borrowCD(LocalDate.now());
    }

    /** Return CD */
    public void returnCD() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    // ---------- Overdue Logic ----------

    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /** Days remaining or overdue days (negative if late) */
    public int getRemainingDays(LocalDate today) {
        if (dueDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(today, dueDate);
    }

    // ---------- Getters ----------

    public boolean isAvailable() { return available; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format(
                "CD[%s by %s, ID=%s, Available=%s, BorrowDate=%s, DueDate=%s]",
                title, artist, id, available, borrowDate, dueDate
        );
    }
}
