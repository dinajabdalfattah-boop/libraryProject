package notification;

import domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EmailNotifierTest {

    @Test
    void testEmailNotifierDoesNotThrow() {
        EmailNotifier notifier = new EmailNotifier();
        User user = new User("TestUser", "test@example.com");

        assertDoesNotThrow(() ->
                notifier.notify(user, "Hello from test!")
        );
    }
}
