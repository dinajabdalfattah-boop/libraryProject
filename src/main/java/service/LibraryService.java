package service;

import domain.Book;
import domain.Loan;
import domain.User;

import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final ReminderService reminderService;

    public LibraryService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public boolean addBook(Book book) {
        for (Book b : books) {
            if (b.getIsbn().equals(book.getIsbn())) {
                return false;
            }
        }
        books.add(book);
        return true;
    }

    public boolean addUser(User user) {
        for (User u : users) {
            if (u.getUserName().equals(user.getUserName())) {
                return false;
            }
        }
        users.add(user);
        return true;
    }

    public boolean borrowBook(User user, Book book) {
        try {
            user.borrowBook(book);
            Loan loan = new Loan(user, book);
            loans.add(loan);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public List<Loan> getOverdueLoans() {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                overdue.add(loan);
            }
        }
        return overdue;
    }

    public void sendOverdueReminders() {
        List<Loan> overdueLoans = getOverdueLoans();
        reminderService.sendReminders(overdueLoans);
    }

    // Unregister user (Sprint 4)
    public boolean unregisterUser(User user) {
        if (!users.contains(user)) {
            return false;
        }

        if (!user.canBeUnregistered()) {
            return false;
        }

        users.remove(user);
        return true;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }
}
