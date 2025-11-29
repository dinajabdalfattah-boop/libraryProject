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

    public void borrowBook(LocalDate borrowDate) {
        if (!available)
            throw new IllegalStateException("Book is already borrowed!");

        this.available = false;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(28);
    }

    public void borrowBook() {
        borrowBook(LocalDate.now());
    }

    public void returnBook() {
        this.available = true;
        this.borrowDate = null;
        this.dueDate = null;
    }

    public boolean isOverdue(LocalDate currentDate) {
        return dueDate != null && currentDate.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

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

    public boolean isBorrowed() {
        return !available;
    }

    // ------------ SETTERS USED FOR LOADING FROM FILE ------------

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return String.format(
                "Book[%s by %s, ISBN=%s, Available=%s, BorrowDate=%s, DueDate=%s]",
                title, author, isbn, available, borrowDate, dueDate
        );
    }
}
