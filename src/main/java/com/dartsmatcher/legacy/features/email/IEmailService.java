package com.dartsmatcher.legacy.features.email;

import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordToken;

import javax.mail.MessagingException;


public interface IEmailService {
	void sendForgotPasswordEmail(User user, PasswordToken passwordToken) throws MessagingException;
}
