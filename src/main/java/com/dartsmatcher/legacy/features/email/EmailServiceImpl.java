package com.dartsmatcher.legacy.features.email;

import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordToken;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements IEmailService {

	private final JavaMailSender javaMailSender;

	private final MessageResolver messageResolver;

	public EmailServiceImpl(JavaMailSender javaMailSender, MessageResolver messageResolver) {
		this.javaMailSender = javaMailSender;
		this.messageResolver = messageResolver;
	}

	/**
	 * Sends an email containing the password reset instructions to a user.
	 *
	 * @param user User prompting the forgot password action.
	 * @param passwordToken PasswordToken used to reset the password.
	 * @throws MessagingException passing any exceptions thrown by MimeMessage
	 */
	public void sendForgotPasswordEmail(User user, PasswordToken passwordToken) throws MessagingException {

		String mailFrom = "dartsmatcher@gmail.com";
		String resetPasswordUrl = "http://localhost:8180/reset-password?token=" + passwordToken.getToken();

		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setTo(user.getEmail());
		helper.setFrom(mailFrom);
		helper.setSubject(messageResolver.getMessage("mail.forgot.password.subject"));
		helper.setText(messageResolver.getMessage("mail.forgot.password.text", user.getFirstName() + " " + user.getLastName(), resetPasswordUrl), true);

		javaMailSender.send(message);
	}

}