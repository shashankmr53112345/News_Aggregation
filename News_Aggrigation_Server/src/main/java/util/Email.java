package util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
	private final String smtpHost = "smtp.gmail.com";
	private final String smtpPort = "587";
	private final String username;
	private final String password;
	private final Properties props;

	public Email() {
		// Load credentials from environment variables or fallback to defaults
		this.username = System.getenv("EMAIL_USERNAME") != null ? System.getenv("EMAIL_USERNAME")
				: "shashankmr531@gmail.com";
		this.password = System.getenv("EMAIL_PASSWORD") != null ? System.getenv("EMAIL_PASSWORD") : "ooylefwueurngxor";
		props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
	}

	public void sendEmail(String to, String subject, String body) throws MessagingException {
		if (to == null || to.trim().isEmpty()) {
			throw new MessagingException("Recipient email address is required");
		}
		if (subject == null || subject.trim().isEmpty()) {
			throw new MessagingException("Email subject is required");
		}
		if (body == null || body.trim().isEmpty()) {
			throw new MessagingException("Email body is required");
		}

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
			System.out.println("Debug: Email sent to " + to + " with subject: " + subject);
		} catch (MessagingException e) {
			System.err.println("Error sending email to " + to + ": " + e.getMessage());
			throw e;
		}
	}
}