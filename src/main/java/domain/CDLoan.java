package domain;

import domain.fine.CDFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * This class represents a loan record for a CD in the library system.
 * It stores information about which user borrowed which CD, the borrowing dates,
 * and it uses a fine strategy (CDFineStrategy) to calculate overdue fines.
 * CDs are always borrowed for a fixed period of 7 days.
 */
public class CDLoan {

    private final User user;
    private final CD cd;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private FineStrategy fineStrategy;
    private boolean active = true;

    /**
     * Creates a new CDLoan object for the given user and CD.
     * Initializes the borrowing date to today, calculates the due date,
     * assigns the specific fine strategy for CDs, and updates the CD's status.
     *
     * @param user the user who borrowed the CD
     * @param cd   the CD being borrowed
     */
    public CDLoan(User user, CD cd) {
        this.user = user;
        this.cd = cd;

        this.fineStrategy = new CDFineStrategy();

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(7);

        // Mark the CD as borrowed in the CD object
        cd.borrowCD(borrowDate);
    }

    /**
     * Returns the CD and closes the loan.
     * Once returned, the loan becomes inactive.
     */
    public void returnCD() {
        if (!active) return;
        this.active = false;
        cd.returnCD();
    }

    /**
     * Checks whether the loan is still active.
     *
     * @return true if the CD has not been returned yet, otherwise false
     */
    public boolean isActive() {
        return active;
    }

    // ---------- Overdue logic ----------

    /**
     * Checks if the loan is overdue when compared to a specific date.
     *
     * @param date the date to compare against
     * @return true if the CD should have been returned before that date
     */
    public boolean isOverdue(LocalDate date) {
        return active && date.isAfter(dueDate);
    }

    /**
     * Checks whether the CD loan is overdue today.
     *
     * @return true if overdue today, otherwise false
     */
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    /**
     * Calculates how many days the CD is overdue.
     * Returns 0 if not overdue.
     *
     * @return number of overdue days
     */
    public int getOverdueDays() {
        if (!isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /**
     * Calculates the fine based on the number of overdue days.
     * Uses the CDFineStrategy assigned to this loan.
     *
     * @return the fine amount as an integer
     */
    public int calculateFine() {
        return fineStrategy.calculateFine(getOverdueDays());
    }

    /**
     * Sets the borrow date manually.
     * Used mainly when loading data from external files.
     *
     * @param borrowDate the date the CD was borrowed
     */
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    /**
     * Sets the due date manually.
     * Used when reconstructing loan information from storage.
     *
     * @param dueDate the date when the CD should be returned
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * @return the user who borrowed the CD
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the CD being loaned
     */
    public CD getCD() {
        return cd;
    }

    /**
     * @return the date the CD was borrowed
     */
    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    /**
     * @return the due date for returning the CD
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Provides a readable summary of the loan information,
     * mainly for debugging and logging.
     *
     * @return formatted string with loan details
     */
    @Override
    public String toString() {
        return "CDLoan{" +
                "user=" + user.getUserName() +
                ", cd=" + cd.getTitle() +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", active=" + active +
                '}';
    }
}
