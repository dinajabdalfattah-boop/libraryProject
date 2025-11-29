package notification;

import domain.User;

import java.util.ArrayList;
import java.util.List;


public class MockNotifier implements Observer {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void notify(User user, String message) {
        messages.add("To " + user.getUserName() + ": " + message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
