package email;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

public class EmailSender implements Runnable {
	
	private String emailLogin;
	private String emailPassword;
	private Session session;
	private final String HOST = "smtp.gmail.com";
	private Transport transport;
	private String messageSubject; 
	private String messageBody;
	private String messageSender;
	private String messageRecipients;
	
	/**
	 * Constructor of the class EmailSender.
	 * @param emailLogin - The login creditentials to an email which will be used for sending the messages. 
	 * @param emailPassword - The password creditentials to an email which will be used for sending the messages.
	 * @param messageSubject - The subject of the email message.
	 * @param messageBody - The body of the email message.
	 * @param messageSender - The email address of the sender.
	 * @param messageRecipients - Recipients of the message. For addressing multiple recipients use comma to separate the email addresses.
	 */
	public EmailSender(final String emailLogin, final String emailPassword, String messageSubject, String messageBody,
			String messageSender, String messageRecipients) {
		System.out.println("Constructor()");
		this.emailLogin = emailLogin;
		this.emailPassword = emailPassword;
		this.messageSubject = messageSubject;
		this.messageBody = messageBody;
		this.messageSender = messageSender;
		this.messageRecipients = messageRecipients;

		Properties properties = new Properties();
		/*properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.quitwait", "false");
		properties.setProperty("mail.smtp.host", HOST);*/
		properties.put("mail.smtp.user", emailLogin);
		properties.put("mail.smtp.host", HOST);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.starttls.enable","true");
		properties.put("mail.smtp.debug", "true");
		properties.put("mail.smtp.auth", "true");
		/*properties.put("mail.smtp.password", password);*/
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");

		/*
		 * JSSEProvider auth = new JSSEProvider(); Session session =
		 * Session.getInstance(properties, auth);
		 */
		session = Session.getInstance(properties,
				new javax.mail.Authenticator() {

					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(emailLogin, emailPassword);
					}
				});
		session.setDebug(true);
	}
	
/*	public synchronized void sendMail() {
	
	}*/

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("sendMail()");
			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(
					messageBody.getBytes(), "text/plain"));
			message.setSender(new InternetAddress(messageSender));
			message.setSubject(messageSubject);
			System.out.println("Subject set.");
			message.setDataHandler(handler);
			System.out.println("Data handler set.");
			if (messageRecipients.indexOf(',') > 0) {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(messageRecipients));
			} else {
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(messageRecipients));
			}
			System.out.println("GmailSender" +
					"Attempting to send message to - " + messageRecipients
							+ " With message - subj: " + message.getSubject()
							+ " content: " + message.getContent().toString());
			if (message != null) {
				System.out.println("GmailSender" + "message is NOT null");
			}

			transport = session.getTransport("smtps");
			transport.connect(HOST, 465, emailLogin, emailPassword);

			transport.sendMessage(message, message.getAllRecipients());

			transport.close();
			/* Transport.send(message); */

			System.out.println("GmailSender" + "Sent message.");
		} catch (AddressException ae) {
			ae.printStackTrace();
			System.out.println("GmailSender" +
					"AddressException - " + ae.getLocalizedMessage());
		} catch (MessagingException me) {
			me.printStackTrace();
			System.out.println("GmailSender" +
					"MessagingException - " + me.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("GmailSender" + "Exception - " + e.getLocalizedMessage());
		}
	}
}
