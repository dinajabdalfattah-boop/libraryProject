package service;

import domain.User;
import domain.Loan;
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

    public boolean sendReminders(List<Loan> overdueLoans) {
        if (overdueLoans.isEmpty()) return false;

        for (Loan loan : overdueLoans) {
            User user = loan.getUser();
            String message = "You have " + user.getOverdueCount() + " overdue book(s).";
            notifyObservers(user, message);
        }
        return true;
    }

    private void notifyObservers(User user, String message) {
        for (Observer o : observers) {
            o.notify(user, message);
        }
    }
}
