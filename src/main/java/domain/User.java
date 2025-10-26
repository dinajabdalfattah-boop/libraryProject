package domain;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private double fineBalance;
    private List<Book> borrowedBooks;

    public User(String name) {
        this.name = name;
        this.fineBalance = 0;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return name;
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public void setFineBalance(double fineBalance) {
        this.fineBalance = fineBalance;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    /**
     * Borrow a book with advanced rules:
     * - Cannot borrow if user has unpaid fines
     * - Cannot borrow if user has overdue books
     */
    public void borrowBook(Book book) {
        if (fineBalance > 0) {
            throw new IllegalStateException("Cannot borrow: Pay fines first!");
        }

        for (Book b : borrowedBooks) {
            if (b.isOverdue()) {
                throw new IllegalStateException("Cannot borrow: Return overdue books first!");
            }
        }

        book.borrowBook();
        borrowedBooks.add(book);
    }

    /**
     * Return a borrowed book
     */
    public void returnBook(Book book) {
        if (borrowedBooks.remove(book)) {
            book.returnBook();
        }
    }

    /**
     * Pay fines partially or fully
     */
    public void payFine(double amount) {
        if (amount >= fineBalance) {
            fineBalance = 0;
        } else {
            fineBalance -= amount;
        }
    }

    /**
     * Count how many borrowed books are overdue
     */
    public int getOverdueCount() {
        int count = 0;
        for (Book book : borrowedBooks) {
            if (book.isOverdue()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if the user can be unregistered:
     * - No active loans
     * - No unpaid fines
     */
    public boolean canBeUnregistered() {
        boolean hasActiveLoans = borrowedBooks.stream().anyMatch(book -> !book.isAvailable());
        return !hasActiveLoans && fineBalance <= 0;
    }
}
