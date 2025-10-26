package notification;

import domain.User;

/**
 * Observer interface used for sending notifications to users.
 *
 * In the Observer design pattern, this interface is implemented
 * by all classes that need to be notified about specific events
 * (e.g., sending email reminders).
 *
 * @author Dina
 * @version 1.0
 */
public interface Observer {

    /**
     * Notifies a user with a given message.
     *
     * @param user the user to be notified
     * @param message the notification message
     */
    void notify(User user, String message);
}
