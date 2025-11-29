package service;

import domain.Book;
import domain.Loan;
import domain.User;
import notification.Observer;

import java.util.ArrayList;
import java.util.List;

public class ReminderService {

    private final List<Observer> observers = new ArrayList<>();

    public boolean addObserver(Observer observer) {
        if (observers.contains(observer)) {
            return false;
        }
        observers.add(observer);
        return true;
    }

    // NEW FUNCTION (fixes your error)
    public void sendReminder(User user, Book book) {
        if (user == null || book == null) return;

        String message = "Reminder: Your book '" + book.getTitle()
                + "' is overdue. Please return it.";

        notifyObservers(user, message);
    }

    public boolean sendReminders(List<Loan> overdueLoans) {
        if (overdueLoans.isEmpty()) {
            return false;
        }

        for (Loan loan : overdueLoans) {
            User user = loan.getUser();
            String message = "You have " + user.getOverdueCount() + " overdue book(s).";

            notifyObservers(user, message);
        }

        return true;
    }

    private void notifyObservers(User user, String message) {
        for (Observer observer : observers) {
            observer.notify(user, message);
        }
    }
}
