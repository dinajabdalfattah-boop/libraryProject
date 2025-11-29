package notification;

import domain.User;
import java.util.ArrayList;
import java.util.List;

/**
 * A mock implementation of the Observer interface.
 * Used ONLY for testing reminder notifications.
 * Instead of sending real emails, it stores messages in memory.
 */
public class MockNotifier implements Observer {

    /** Stores all messages sent to users */
    private final List<String> messages = new ArrayList<>();

    /**
     * Records a notification message for testing.
     *
     * @param user    the user receiving the message
     * @param message the reminder message
     */
    @Override
    public void notify(User user, String message) {
        messages.add("To " + user.getUserName() + ": " + message);
    }

    /**
     * @return all recorded messages (for assertions in tests)
     */
    public List<String> getMessages() {
        return messages;
    }
}
