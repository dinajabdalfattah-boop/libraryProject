package notification;

import domain.User;

/**
 * Observer interface for notification channels (email, SMS, etc.).
 * Implements the Observer pattern.
 */
public interface Observer {

    /**
     * Notifies a user with the given message.
     *
     * @param user    the target user
     * @param message the notification message
     */
    void notify(User user, String message);
}
