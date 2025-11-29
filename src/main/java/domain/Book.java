package domain;

import java.time.LocalDate;

public class Book {
    private final String title;
    private final String author;
    private final String isbn;

    private boolean available;
    private LocalDate borrowDate;
    private LocalDate dueDate;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    // Borrow book with optional date (for testing or time mocking)
    public void borrowBook(LocalDate borrowDate) {
        if (!available) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        available = false;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(28); // 28 days borrowing rule
    }

    // Borrow with today's date
    public void borrowBook() {
        borrowBook(LocalDate.now());
    }

    public void returnBook() {
        available = true;
        borrowDate = null;
        dueDate = null;
    }

    // Overdue check with mockable date
    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    // Using system date
    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    // Getters
    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
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

    @Override
    public String toString() {
        String status = available ? "Available" : "Not Available (Due: " + dueDate + ")";
        return "Title: " + title + ", Author: " + author + ", ISBN: " + isbn + ", Status: " + status;
    }

    public boolean isBorrowed() {
        return !available;
    }
}
