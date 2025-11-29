package domain;

import java.util.ArrayList;
import java.util.List;

public class User {

    private final String name;
    private String email;        // NOT FINAL (to allow setEmail)
    private double fineBalance;
    private final List<Book> borrowedBooks;

    /** OLD constructor (kept for compatibility) */
    public User(String name) {
        this(name, "no-email@none.com");
    }

    /** MAIN constructor */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.fineBalance = 0.0;
        this.borrowedBooks = new ArrayList<>();
    }

    // =======================
    //        GETTERS
    // =======================

    public String getUserName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    // =======================
    //        SETTERS
    // =======================

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFineBalance(double fineBalance) {
        this.fineBalance = fineBalance;
    }

    // =======================
    //       CORE LOGIC
    // =======================

    public void borrowBook(Book book) {
        if (fineBalance > 0)
            throw new IllegalStateException("Cannot borrow: Pay fines first!");

        for (Book b : borrowedBooks)
            if (b.isOverdue())
                throw new IllegalStateException("Cannot borrow: Return overdue books first!");

        book.borrowBook();
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        if (borrowedBooks.remove(book)) {
            book.returnBook();
        }
    }

    public void payFine(double amount) {
        if (amount >= fineBalance)
            fineBalance = 0;
        else
            fineBalance -= amount;
    }

    public int getOverdueCount() {
        int c = 0;
        for (Book b : borrowedBooks)
            if (b.isOverdue()) c++;
        return c;
    }

    public boolean canBeUnregistered() {
        boolean hasActive = borrowedBooks.stream().anyMatch(b -> !b.isAvailable());
        return !hasActive && fineBalance <= 0;
    }

    @Override
    public String toString() {
        return "User[" + name + ", email=" + email + ", fine=" + fineBalance + "]";
    }
}
