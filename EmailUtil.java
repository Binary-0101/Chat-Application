import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtil {
	public static void sendEmail(String receiver_email, String subject, String msgContent) {
		Properties properties = new Properties();

		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("sanjaycab17@gmail.com", "uwkn kfwg szix pgtl");
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sanjaycab17@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver_email));
			message.setSubject(subject);
			message.setText(msgContent);

			Transport.send(message);
			//System.out.println("Email sent successfully to "+ receiver_email);
		} catch (Exception e) {
			//System.out.println("Sending email failed: "+e.getMessage());
			return;
		}
	}  
}