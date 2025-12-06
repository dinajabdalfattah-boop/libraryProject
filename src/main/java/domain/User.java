package domain;

import java.util.ArrayList;
import java.util.List;

public class User {

    private final String name;
    private String email;
    private double fineBalance;

    /** Active book loans */
    private final List<Loan> activeBookLoans;

    /** Active CD loans */
    private final List<CDLoan> activeCDLoans;

    // -------------------- Constructors --------------------

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

    // ---------------------- Getters -----------------------

    public String getUserName() { return name; }
    public String getEmail() { return email; }
    public double getFineBalance() { return fineBalance; }

    public List<Loan> getActiveBookLoans() { return activeBookLoans; }
    public List<CDLoan> getActiveCDLoans() { return activeCDLoans; }

    // ---------------------- Setters -----------------------

    public void setEmail(String email) { this.email = email; }
    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }

    // ---------------------- Core Logic --------------------

    /** Borrow a book */
    public void addLoan(Loan loan) {

        // Sprint 4 Restrictions
        if (fineBalance > 0)
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");

        if (hasOverdueLoans())
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");

        // Loan already marks the book as borrowed, so no duplication here
        activeBookLoans.add(loan);
    }

    /** Borrow a CD */
    public void addCDLoan(CDLoan loan) {

        if (fineBalance > 0)
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");

        if (hasOverdueLoans())
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");

        activeCDLoans.add(loan);
    }

    /** Return book loan */
    public void returnLoan(Loan loan) {
        if (activeBookLoans.remove(loan)) {
            loan.returnBook(); // mark loan inactive + book returned
        }
    }

    /** Return CD loan */
    public void returnCDLoan(CDLoan loan) {
        if (activeCDLoans.remove(loan)) {
            loan.returnCD();
        }
    }

    /** Overdue detection */
    public boolean hasOverdueLoans() {
        return activeBookLoans.stream().anyMatch(Loan::isOverdue)
                || activeCDLoans.stream().anyMatch(CDLoan::isOverdue);
    }

    public int getOverdueCount() {
        return (int) activeBookLoans.stream().filter(Loan::isOverdue).count()
                + (int) activeCDLoans.stream().filter(CDLoan::isOverdue).count();
    }

    /** Pay fines */
    public void payFine(double amount) {
        if (amount >= fineBalance)
            fineBalance = 0;
        else
            fineBalance -= amount;
    }

    /** Unregister rules */
    public boolean canBeUnregistered() {
        boolean hasActiveLoans = !activeBookLoans.isEmpty() || !activeCDLoans.isEmpty();
        return !hasActiveLoans && fineBalance <= 0;
    }

    @Override
    public String toString() {
        return "User[" + name + ", email=" + email + ", fine=" + fineBalance + "]";
    }
}
