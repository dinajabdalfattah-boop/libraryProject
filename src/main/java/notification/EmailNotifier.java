package notification;

import domain.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * EmailNotifier
 * ---------------------------
 * This class implements the Observer interface and is responsible for sending
 * email notifications to library users when they have overdue books or other events.
 *
 * The class uses:
 * - Jakarta Mail API (for sending emails)
 * - Dotenv (to load email credentials from the .env file)
 *
 * The email credentials are NOT stored in code for security reasons.
 * Instead, they are loaded from environment variables defined in a `.env` file:
 *
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 */
public class EmailNotifier implements Observer {

    private final String senderEmail;
    private final String appPassword;

    /**
     * Constructor:
     * Loads the environment variables using Dotenv.
     * This avoids hardcoding sensitive information in the source code.
     */
    public EmailNotifier() {
        Dotenv dotenv = Dotenv.load();
        this.senderEmail = dotenv.get("EMAIL_USERNAME");
        this.appPassword = dotenv.get("EMAIL_PASSWORD");
    }

    /**
     * notify()
     * -------------------------------------------------
     * Sends an email notification to a specific user.
     *
     * @param user    The user who will receive the email (email taken from user.getEmail())
     * @param message The content of the notification message
     *
     * Steps performed:
     * 1. Configure SMTP properties for Gmail
     * 2. Authenticate using the email + app password
     * 3. Build the email (from, to, subject, body)
     * 4. Send the email using Transport.send()
     */
    @Override
    public void notify(User user, String message) {

        String to = user.getEmail();

        try {
            // 1. SMTP server settings for Gmail
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

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
            e.printStackTrace();
        }
    }
}
