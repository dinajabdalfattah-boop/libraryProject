package service;

import domain.User;
import domain.Loan;
import notification.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for sending reminders to users with overdue books.
 */
public class ReminderService {

    private final List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void sendReminders(List<Loan> overdueLoans) {
        for (Loan loan : overdueLoans) {
            User user = loan.getUser();
            String message = "You have " + user.getOverdueCount() + " overdue book(s).";
            notifyObservers(user, message);
        }
    }

    private void notifyObservers(User user, String message) {
        for (Observer o : observers) {
            o.notify(user, message);
        }
    }
}
