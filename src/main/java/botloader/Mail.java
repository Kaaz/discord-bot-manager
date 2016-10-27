package botloader;

/**
 * Gemaakt op 15-2-2016
 */

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mail {

	static Mail instance;
	private static Session session;
	private String FROM_EMAIL = "";

	private Mail() throws Exception {
		FROM_EMAIL = Config.EMAIL_USERNAME;
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", true); // added this line
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.user", Config.EMAIL_USERNAME);
		props.put("mail.smtp.password", Config.EMAIL_PASSWORD);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", true);

		session = Session.getInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD);
					}
				});
		if (Config.EMAIL_ENABLED && !isValidEmail(Config.EMAIL_RECIPIENTS)) {
			throw new AddressException("Email is enabled, but no valid recipients are configured! (see boot.cfg:email_recipients) Or simply disabled the email");
		}
	}

	public static Mail getInstance() throws Exception {
		if (instance == null) {
			instance = new Mail();
		}
		return instance;
	}

	/**
	 * Test if an email adres is legit
	 *
	 * @param emailAdresList an email adres or comma-separated list
	 * @return are they all legit?
	 */
	public boolean isValidEmail(String emailAdresList) {
		try {
			InternetAddress.parse(emailAdresList);
			return true;
		} catch (AddressException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * sends a mail to configured recipients
	 *
	 * @param body html text
	 */
	public void send(String body) {
		if (!Config.EMAIL_ENABLED) {
			return;
		}
		send(Config.EMAIL_DEFAULT_SUBJECT, body);
	}

	public void send(String title, String body) {
		if (!Config.EMAIL_ENABLED) {
			return;
		}
		try {
			String mailText = "";
			mailText += body;
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(FROM_EMAIL));
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(Config.EMAIL_RECIPIENTS));
			message.setSubject(Config.EMAIL_PREFIX_SUBJECT + title);
			message.setText(mailText, "utf-8", "html");
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}