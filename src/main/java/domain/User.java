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

    // Getters & Setters
    public String getName() { return name; }
    public double getFineBalance() { return fineBalance; }
    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }
    public List<Book> getBorrowedBooks() { return borrowedBooks; }
}
