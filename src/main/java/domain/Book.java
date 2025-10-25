package domain;

import java.time.LocalDate;

public class Book {
    // Attributes
    private final String title;
    private final String author;
    private final String isbn;
    private boolean available;

    // Borrowing info
    private LocalDate borrowDate;
    private LocalDate dueDate;

    // Constructor
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }


    // When user borrows a book
    public void borrowBook() {
        if (!available) {
            System.out.println("This book is already borrowed!");
            return;
        }
        this.available = false;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28); // due after 28 days
        System.out.println("Book borrowed successfully. Due date: " + dueDate);
    }

    // When user returns the book
    public void returnBook() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
        System.out.println("Book returned successfully.");
    }

    // Check if the book is overdue
    public boolean isOverdue() {
        if (dueDate == null) {
            return false; // Not borrowed
        }
        return LocalDate.now().isAfter(dueDate);
    }

    // --- Getters & Setters ---
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        String status = available ? "Available" : "Borrowed (Due: " + dueDate + ")";
        return "Title: " + title + ", Author: " + author + ", ISBN: " + isbn + ", Status: " + status;
    }
}
