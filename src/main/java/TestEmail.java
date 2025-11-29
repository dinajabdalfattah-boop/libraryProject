import domain.User;
import notification.EmailNotifier;

public class TestEmail {
    public static void main(String[] args) {

        EmailNotifier notifier = new EmailNotifier();

        // إرسال التست على إيميلك
        User u = new User("Dina", "abdalfattahdina8@gmail.com");

        notifier.notify(u, "🔥 This is a TEST EMAIL from Java! If you received it, everything works! 🔥");

        System.out.println("Test email sent... check your inbox!");
    }
}
