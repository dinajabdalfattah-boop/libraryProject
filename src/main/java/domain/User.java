package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a user in the library system.
 * A user can borrow books and CDs, accumulate fines, and return borrowed items.
 * The class also applies borrowing rules such as preventing users with unpaid fines
 * or overdue items from borrowing more materials.
 */
public class User {

    private final String name;
    private String email;
    private double fineBalance;
    private final List<Loan> activeBookLoans;
    private final List<CDLoan> activeCDLoans;

    public User(String name) {
        this(name, "no-email@none.com");
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.fineBalance = 0;
        this.activeBookLoans = new ArrayList<>();
        this.activeCDLoans = new ArrayList<>();
    }

    public String getUserName() { return name; }
    public String getEmail() { return email; }
    public double getFineBalance() { return fineBalance; }
    public List<Loan> getActiveBookLoans() { return activeBookLoans; }
    public List<CDLoan> getActiveCDLoans() { return activeCDLoans; }

    public void setEmail(String email) { this.email = email; }
    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }

    /**
     * Adds a new loan for a borrowed book.
     * Enforces rules required by tests.
     */
    public void addLoan(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("loan is null");
        }

        // 🔴 MESSAGE MUST MATCH TEST EXACTLY
        if (fineBalance > 0) {
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");
        }

        if (hasOverdueLoans()) {
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");
        }

        activeBookLoans.add(loan);
    }

    /**
     * Adds a new loan for a borrowed CD.
     * Enforces rules required by tests.
     */
    public void addCDLoan(CDLoan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("loan is null");
        }

        // 🔴 MESSAGE MUST MATCH TEST EXACTLY
        if (fineBalance > 0) {
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");
        }

        if (hasOverdueLoans()) {
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");
        }

        activeCDLoans.add(loan);
    }

    public void returnLoan(Loan loan) {
        if (activeBookLoans.remove(loan)) {
            loan.returnBook();
        }
    }

    public void returnCDLoan(CDLoan loan) {
        if (activeCDLoans.remove(loan)) {
            loan.returnCD();
        }
    }

    public boolean hasOverdueLoans() {
        return activeBookLoans.stream().anyMatch(Loan::isOverdue)
                || activeCDLoans.stream().anyMatch(CDLoan::isOverdue);
    }

    public int getOverdueCount() {
        return (int) activeBookLoans.stream().filter(Loan::isOverdue).count()
                + (int) activeCDLoans.stream().filter(CDLoan::isOverdue).count();
    }

    public void payFine(double amount) {
        if (amount >= fineBalance)
            fineBalance = 0;
        else
            fineBalance -= amount;
    }

    public boolean canBeUnregistered() {

        if (fineBalance > 0) {
            return false;
        }

        boolean hasActiveBookLoan =
                activeBookLoans.stream().anyMatch(loan -> loan != null && loan.isActive());

        boolean hasActiveCDLoan =
                activeCDLoans.stream().anyMatch(cdLoan -> cdLoan != null && cdLoan.isActive());

        return !hasActiveBookLoan && !hasActiveCDLoan;
    }

    @Override
    public String toString() {
        return "User[" + name + ", email=" + email + ", fine=" + fineBalance + "]";
    }
}
