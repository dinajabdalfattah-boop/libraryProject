package domain;

import java.time.LocalDate;

public class Loan {

    private final User user;
    private final Book book;
    private final LocalDate borrowDate;
    private LocalDate dueDate;


    public Loan(User user, Book book) {

        if (book.isBorrowed()) {
            throw new IllegalStateException("Cannot create Loan: Book is already borrowed.");
        }

        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);
    }

    public boolean isOverdue(LocalDate currentDate) {
        return currentDate.isAfter(dueDate);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    // Getters
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

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "Loan{" + "user=" + user.getUserName() + ", book=" + book.getTitle() + ", dueDate=" + dueDate + '}';
    }
}
