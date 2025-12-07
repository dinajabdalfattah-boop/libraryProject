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

    /**
     * Creates a User with a default email when no email is provided.
     *
     * @param name the user's name
     */
    public User(String name) {
        this(name, "no-email@none.com");
    }

    /**
     * Creates a User with a given name and email.
     * Fine balance starts at zero, and no active loans exist initially.
     *
     * @param name  the user's name
     * @param email the user's email
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.fineBalance = 0;
        this.activeBookLoans = new ArrayList<>();
        this.activeCDLoans = new ArrayList<>();
    }

    /** @return the user's name */
    public String getUserName() { return name; }

    /** @return the user's email */
    public String getEmail() { return email; }

    /** @return the user's outstanding fine balance */
    public double getFineBalance() { return fineBalance; }

    /** @return a list of all currently active book loans */
    public List<Loan> getActiveBookLoans() { return activeBookLoans; }

    /** @return a list of all currently active CD loans */
    public List<CDLoan> getActiveCDLoans() { return activeCDLoans; }

    /**
     * Updates the user's email address.
     *
     * @param email the new email value
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Updates the user's fine balance.
     *
     * @param fineBalance new fine amount
     */
    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }

    /**
     * Adds a new loan for a borrowed book.
     * Borrowing is only allowed if the user does NOT have unpaid fines
     * and does NOT have any overdue items.
     *
     * @param loan the Loan object representing the borrowed book
     * @throws IllegalStateException if the user has unpaid fines or overdue loans
     */
    public void addLoan(Loan loan) {
        
        if (fineBalance > 0)
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");

        if (hasOverdueLoans())
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");

        activeBookLoans.add(loan);
    }

    /**
     * Adds a new loan for a borrowed CD.
     * Same borrowing restrictions as books apply.
     *
     * @param loan the CDLoan object
     * @throws IllegalStateException if the user has unpaid fines or overdue CDs/books
     */
    public void addCDLoan(CDLoan loan) {

        if (fineBalance > 0)
            throw new IllegalStateException("Cannot borrow: Unpaid fines.");

        if (hasOverdueLoans())
            throw new IllegalStateException("Cannot borrow: Overdue loans exist.");

        activeCDLoans.add(loan);
    }

    /**
     * Returns a borrowed book.
     * Removes the loan from the active list and marks the book as returned.
     *
     * @param loan the loan to be returned
     */
    public void returnLoan(Loan loan) {
        if (activeBookLoans.remove(loan)) {
            loan.returnBook(); 
            loan.returnBook(); 
        }
    }

    /**
     * Returns a borrowed CD.
     *
     * @param loan the CD loan to return
     */
    public void returnCDLoan(CDLoan loan) {
        if (activeCDLoans.remove(loan)) {
            loan.returnCD();
        }
    }

    /**
     * Checks whether the user has any overdue items (book or CD).
     *
     * @return true if at least one overdue loan exists
     */
    public boolean hasOverdueLoans() {
        return activeBookLoans.stream().anyMatch(Loan::isOverdue)
                || activeCDLoans.stream().anyMatch(CDLoan::isOverdue);
    }

    /**
     * Counts the number of overdue items the user currently has.
     *
     * @return total overdue items
     */
    public int getOverdueCount() {
        return (int) activeBookLoans.stream().filter(Loan::isOverdue).count()
                + (int) activeCDLoans.stream().filter(CDLoan::isOverdue).count();
    }

    /**
     * Allows the user to pay part or all of their outstanding fine.
     *
     * @param amount the amount paid
     */
    public void payFine(double amount) {
        if (amount >= fineBalance)
            fineBalance = 0;
        else
            fineBalance -= amount;
    }

    /**
     * Checks whether the user can be unregistered from the system.
     * A user can be unregistered only if:
     *  - they have no active loans
     *  - they have no unpaid fines
     *
     * @return true if the user meets unregister requirements
     */
    public boolean canBeUnregistered() {
        boolean hasActiveLoans = !activeBookLoans.isEmpty() || !activeCDLoans.isEmpty();
        return !hasActiveLoans && fineBalance <= 0;
    }

    /**
     * Returns a readable summary of the user.
     *
     * @return formatted user info
     */
    @Override
    public String toString() {
        return "User[" + name + ", email=" + email + ", fine=" + fineBalance + "]";
    }
}
