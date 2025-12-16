package domain;

import domain.fine.BookFineStrategy;
import domain.fine.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {

    private final User user;
    private final Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private FineStrategy fineStrategy;
    private boolean active = true;

    public Loan(User user, Book book, FineStrategy fineStrategy) {
        this.user = user;
        this.book = book;
        this.fineStrategy = fineStrategy;

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);

        book.borrowBook(borrowDate);
    }

    public Loan(User user, Book book) {
        this(user, book, new BookFineStrategy());
    }

    public Loan(User user, Book book, LocalDate borrowDate, LocalDate dueDate, boolean active) {
        this.user = user;
        this.book = book;
        this.fineStrategy = new BookFineStrategy();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.active = active;
    }

    public void returnBook() {
        if (!active) return;
        this.active = false;
        book.returnBook();
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOverdue(LocalDate date) {
        return active && dueDate != null && date.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    public int getOverdueDays() {
        if (!isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public int calculateFine() {
        return fineStrategy.calculateFine(getOverdueDays());
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setFineStrategy(FineStrategy fineStrategy) {
        this.fineStrategy = fineStrategy;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public FineStrategy getFineStrategy() {
        return fineStrategy;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "user=" + user.getUserName() +
                ", book=" + book.getTitle() +
                ", borrow=" + borrowDate +
                ", due=" + dueDate +
                ", active=" + active +
                '}';
    }
}
