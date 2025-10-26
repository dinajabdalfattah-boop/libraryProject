package notification;

import domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock notifier for unit testing. Stores messages instead of sending.
 */
public class MockNotifier implements Observer {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void notify(User user, String message) {
        messages.add("To " + user.getName() + ": " + message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
