package service;

import domain.*;
import notification.Observer;

import java.util.*;

public class ReminderService {

    private final List<Observer> observers = new ArrayList<>();

    // ---------------------------------------------------------
    // Observer handling
    // ---------------------------------------------------------

    public boolean addObserver(Observer observer) {
        if (observer == null || observers.contains(observer)) {
            return false;
        }
        observers.add(observer);
        return true;
    }

    private void notifyObservers(User user, String message) {
        for (Observer obs : observers) {
            obs.notify(user, message);
        }
    }

    // ---------------------------------------------------------
    // Send reminders for Books + CDs
    // ---------------------------------------------------------

    /**
     * Sends reminder messages to all users who have overdue BOOK or CD loans.
     * Mixed-media overdue count is supported (Sprint 5).
     */
    public void sendReminders(List<Loan> overdueBookLoans,
                              List<CDLoan> overdueCDLoans) {

        // Map each user → count of overdue items
        Map<User, Integer> overdueCountMap = new HashMap<>();

        // Count overdue books
        for (Loan loan : overdueBookLoans) {
            overdueCountMap.put(
                    loan.getUser(),
                    overdueCountMap.getOrDefault(loan.getUser(), 0) + 1
            );
        }

        // Count overdue CDs
        for (CDLoan cdLoan : overdueCDLoans) {
            overdueCountMap.put(
                    cdLoan.getUser(),
                    overdueCountMap.getOrDefault(cdLoan.getUser(), 0) + 1
            );
        }

        // Notify each user once
        for (Map.Entry<User, Integer> entry : overdueCountMap.entrySet()) {
            User user = entry.getKey();
            int count = entry.getValue();

            String message = "You have " + count + " overdue item(s).";
            notifyObservers(user, message);
        }
    }

}
