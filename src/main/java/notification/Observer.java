package notification;

import domain.User;


public interface Observer {

    void notify(User user, String message);
}
