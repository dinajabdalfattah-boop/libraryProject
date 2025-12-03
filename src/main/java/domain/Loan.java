package domain;

import domain.fine.FineStrategy;
import domain.fine.BookFineStrategy;

import java.time.LocalDate;

public class Loan {

    private final User user;
    private final Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate;

    private FineStrategy fineStrategy; // Strategy Pattern

    // Constructor with specific strategy
    public Loan(User user, Book book, FineStrategy fineStrategy) {
        this.user = user;
        this.book = book;
        this.fineStrategy = fineStrategy;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);
    }

    // Default constructor (BookFineStrategy)
    public Loan(User user, Book book) {
        this(user, book, new BookFineStrategy());
    }

    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public FineStrategy getFineStrategy() { return fineStrategy; }

    public boolean isOverdue(LocalDate date) {
        return dueDate != null && date.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    public int getOverdueDays() {
        if (!isOverdue()) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public int calculateFine() {
        return fineStrategy.calculateFine(getOverdueDays());
    }

    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setFineStrategy(FineStrategy fineStrategy) { this.fineStrategy = fineStrategy; }

    @Override
    public String toString() {
        return "Loan{" +
                "user=" + user.getUserName() +
                ", book=" + book.getTitle() +
                ", borrow=" + borrowDate +
                ", due=" + dueDate +
                '}';
    }
}
