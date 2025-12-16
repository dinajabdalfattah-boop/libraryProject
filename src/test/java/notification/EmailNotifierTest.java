package notification;

import domain.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class EmailNotifierTest {

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void notify_userNull_noThrow() {
        EmailNotifier notifier = new EmailNotifier();
        assertDoesNotThrow(() -> notifier.notify(null, "msg"));
    }

    @Test
    void notify_emailNull_noThrow() {
        EmailNotifier notifier = new EmailNotifier();
        User user = new User("Ali", null);
        assertDoesNotThrow(() -> notifier.notify(user, "msg"));
    }

    @Test
    void notify_emailBlank_noThrow() {
        EmailNotifier notifier = new EmailNotifier();
        User user = new User("Ali", "   ");
        assertDoesNotThrow(() -> notifier.notify(user, "msg"));
    }

    @Test
    void notify_emailLiteralNull_noThrow() {
        EmailNotifier notifier = new EmailNotifier();
        User user = new User("Ali", "null");
        assertDoesNotThrow(() -> notifier.notify(user, "msg"));
    }

    @Test
    void notify_envMissing_noThrow() {
        EmailNotifier notifier = new EmailNotifier();
        setField(notifier, "senderEmail", null);
        setField(notifier, "appPassword", null);

        User user = new User("Ali", "a@a.com");
        assertDoesNotThrow(() -> notifier.notify(user, "msg"));
    }

    @Test
    void notify_successPath_mockTransportSend() {
        EmailNotifier notifier = new EmailNotifier();
        setField(notifier, "senderEmail", "sender@example.com");
        setField(notifier, "appPassword", "appPass");

        User user = new User("Ali", "a@a.com");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> notifier.notify(user, "Hello"));
            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    void notify_failurePath_hitsCatchBlock() {
        EmailNotifier notifier = new EmailNotifier();
        setField(notifier, "senderEmail", "sender@example.com");
        setField(notifier, "appPassword", "appPass");

        User user = new User("Ali", "a@a.com");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("fail"));

            assertDoesNotThrow(() -> notifier.notify(user, "Hello"));
            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }
}
