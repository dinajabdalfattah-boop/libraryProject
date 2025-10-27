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

    public String getName() { return name; }
    public double getFineBalance() { return fineBalance; }
    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }
    public List<Book> getBorrowedBooks() { return borrowedBooks; }

    // Borrow a book with rules
    public void borrowBook(Book book) {
        if (fineBalance > 0) throw new IllegalStateException("Pay fines first");
        for (Book b : borrowedBooks) {
            if (b.isOverdue()) throw new IllegalStateException("Return overdue books first");
        }
        book.borrowBook();
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        if (borrowedBooks.remove(book)) {
            book.returnBook();
        }
    }

    public void payFine(double amount) {
        if (amount > fineBalance) fineBalance = 0;
        else fineBalance -= amount;
    }
}
