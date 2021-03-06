import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	public static void sendEmail(ArrayList<String> results) throws Exception {

		final String username = "noreply@email.mydomain.com";
		final String password = "noreply";
		
		StringBuilder msgBody = new StringBuilder();
		for(String result: results){
			msgBody.append(result);
			msgBody.append("\n");
		}

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", "25");

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("dishant.a.chawla@accenture.com"));
			message.setSubject("Autogenerated: Selenium test results");
			message.setText("Please find the test results below: \n\n"
					+ msgBody);

			Transport.send(message);

			System.out.println("Mail sent");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}