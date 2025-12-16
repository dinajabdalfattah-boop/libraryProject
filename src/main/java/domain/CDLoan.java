package domain;

import domain.fine.CDFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CDLoan {

    private final User user;
    private final CD cd;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private FineStrategy fineStrategy;
    private boolean active = true;

    public CDLoan(User user, CD cd) {
        this.user = user;
        this.cd = cd;

        this.fineStrategy = new CDFineStrategy();

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(7);

        cd.borrowCD(borrowDate);
    }

    public CDLoan(User user, CD cd, LocalDate borrowDate, LocalDate dueDate, boolean active) {
        this.user = user;
        this.cd = cd;
        this.fineStrategy = new CDFineStrategy();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.active = active;
    }

    public void returnCD() {
        if (!active) return;
        this.active = false;
        cd.returnCD();
    }

    public boolean isActive() {
        return active;
    }

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

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

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
