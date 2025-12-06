package domain;

import domain.fine.CDFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a CD loan in the system.
 * CDs are borrowed for 7 days and use a CDFineStrategy for overdue fines.
 */
public class CDLoan {

    private final User user;
    private final CD cd;

    private LocalDate borrowDate;
    private LocalDate dueDate;

    private FineStrategy fineStrategy;

    /** Indicates whether the loan is active or returned. */
    private boolean active = true;

    public CDLoan(User user, CD cd) {
        this.user = user;
        this.cd = cd;

        this.fineStrategy = new CDFineStrategy();

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(7);

        // Mark the CD as borrowed in the CD object
        cd.borrowCD(borrowDate);
    }

    // ---------- Loan lifecycle ----------

    /** Ends the loan and marks the CD as returned. */
    public void returnCD() {
        if (!active) return;
        this.active = false;
        cd.returnCD();
    }

    public boolean isActive() {
        return active;
    }

    // ---------- Overdue logic ----------

    public boolean isOverdue(LocalDate date) {
        return active && date.isAfter(dueDate);
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

    // ---------- Setters for loading from file ----------

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // ---------- Getters ----------

    public User getUser() {
        return user;
    }

    public CD getCD() {
        return cd;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

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
