package notification;

import domain.User;

public class EmailNotifier implements Observer {

    @Override
    public void notify(User user, String message) {
        // Mock sending email
        System.out.println("Email to " + user.getUserName() + ": " + message);
    }
}
