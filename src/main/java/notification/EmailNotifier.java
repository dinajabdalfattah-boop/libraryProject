package notification;

import domain.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Real email notifier implementation using Jakarta Mail + Dotenv.
 *
 * Requires .env file with:
 *   EMAIL_USERNAME=your_email@gmail.com
 *   EMAIL_PASSWORD=your_app_password
 */
public class EmailNotifier implements Observer {

    private final String senderEmail;
    private final String appPassword;

    public EmailNotifier() {
        Dotenv dotenv = Dotenv.load();
        this.senderEmail = dotenv.get("EMAIL_USERNAME");
        this.appPassword = dotenv.get("EMAIL_PASSWORD");
    }

    @Override
    public void notify(User user, String message) {

        if (user == null) {
            System.out.println("EmailNotifier: user is null, skipping.");
            return;
        }

        String to = user.getEmail();

        if (to == null || to.trim().isEmpty() || "null".equalsIgnoreCase(to.trim())) {
            System.out.println("EmailNotifier: user " + user.getUserName() +
                    " has no valid email, skipping.");
            return;
        }

        if (senderEmail == null || appPassword == null) {
            System.out.println("EmailNotifier: EMAIL_USERNAME or EMAIL_PASSWORD not set in .env");
            return;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, appPassword);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject("Library Overdue Notice");
            msg.setText(message);

            Transport.send(msg);

            System.out.println("Email SENT to: " + to);

        } catch (Exception e) {
            System.out.println("EmailNotifier: failed to send email to " + to);
            e.printStackTrace();
        }
    }
}
