package notification;

import domain.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailNotifier implements Observer {

    private final String senderEmail = "abdalfattahdina8@gmail.com";  // اكتبي إيميلك
    private final String appPassword = "kwewarwyrdecjcqy";     // اكتبي الباسورد

    @Override
    public void notify(User user, String message) {

        String to = user.getEmail(); // هذا الصحيح

        try {
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
