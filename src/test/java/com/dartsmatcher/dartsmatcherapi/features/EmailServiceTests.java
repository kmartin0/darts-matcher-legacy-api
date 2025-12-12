package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.legacy.config.LocaleConfig;
import com.dartsmatcher.legacy.features.email.EmailServiceImpl;
import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordToken;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith({SpringExtension.class})
@ContextConfiguration
@Import(LocaleConfig.class)
public class EmailServiceTests {

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private MessageResolver messageResolver;

	@InjectMocks
	private EmailServiceImpl emailService;

	@Test
	void testSendForgotPasswordEmail_sendEmailWithResetLink() throws MessagingException, IOException {
		User user = createUser();
		PasswordToken passwordToken = new PasswordToken(new ObjectId(), UUID.randomUUID(), user.getId(), LocalDateTime.now().plusDays(3));

		Mockito.when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
		Mockito.when(messageResolver.getMessage("mail.forgot.password.subject")).thenReturn("Subject");
		Mockito.when(messageResolver.getMessage(eq("mail.forgot.password.text"), any(), any())).thenReturn("Text");

		// When
		emailService.sendForgotPasswordEmail(user, passwordToken);

		// Assert the mail is sent to the email address of the user with forgot password subject and text.
		ArgumentCaptor<MimeMessage> argumentUser = ArgumentCaptor.forClass(MimeMessage.class);
		Mockito.verify(javaMailSender).send(argumentUser.capture());
		Assertions.assertEquals(user.getEmail(), argumentUser.getValue().getRecipients(Message.RecipientType.TO)[0].toString());
		Assertions.assertEquals("Subject", argumentUser.getValue().getSubject());
		Assertions.assertEquals("Text", argumentUser.getValue().getContent());
	}

	User createUser() {
		return new User(
				new ObjectId(),
				"johndoe1",
				"John",
				"Doe",
				"johndoe@gmail.com",
				new ArrayList<>(),
				"pass123"
		);
	}
}
