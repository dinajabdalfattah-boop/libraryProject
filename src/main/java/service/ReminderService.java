package service;

import domain.*;
import notification.Observer;

import java.util.*;

/**
 * This service is responsible for sending reminder notifications to users
 * who have overdue book or CD loans. It uses the Observer pattern:
 * different notification handlers (such as email or SMS) can register
 * themselves as observers, and the service will notify all of them when
 * a reminder needs to be sent.
 */
public class ReminderService {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Registers a new observer to receive reminders.
     * An observer will only be added if it is not null and not already registered.
     *
     * @param observer the observer to add
     * @return true if added successfully, false otherwise
     */
    public boolean addObserver(Observer observer) {
        if (observer == null || observers.contains(observer)) {
            return false;
        }
        observers.add(observer);
        return true;
    }

    /**
     * Notifies all registered observers with a reminder message
     * for a particular user.
     *
     * @param user    the user receiving the reminder
     * @param message the reminder message to send
     */
    private void notifyObservers(User user, String message) {
        for (Observer obs : observers) {
            obs.notify(user, message);
        }
    }

    /**
     * Sends reminder messages to all users who have overdue items.
     * This includes both book loans and CD loans. If a user has
     * multiple overdue items (mixed types count together), they will
     * receive only one reminder with the total number of overdue items.
     *
     * This functionality supports the Sprint 5 requirement of
     * combined media overdue notifications.
     *
     * @param overdueBookLoans list of overdue book loans
     * @param overdueCDLoans   list of overdue CD loans
     */
    public void sendReminders(List<Loan> overdueBookLoans,List<CDLoan> overdueCDLoans) {

        Map<User, Integer> overdueCountMap = new HashMap<>();

        for (Loan loan : overdueBookLoans) {
            overdueCountMap.put(
                    loan.getUser(),
                    overdueCountMap.getOrDefault(loan.getUser(), 0) + 1
            );
        }

        for (CDLoan cdLoan : overdueCDLoans) {
            overdueCountMap.put(
                    cdLoan.getUser(),
                    overdueCountMap.getOrDefault(cdLoan.getUser(), 0) + 1
            );
        }

        for (Map.Entry<User, Integer> entry : overdueCountMap.entrySet()) {
            User user = entry.getKey();
            int count = entry.getValue();

            String message = "You have " + count + " overdue item(s).";
            notifyObservers(user, message);
        }
    }

}
