package com.dartsmatcher.legacy.features.user.password.forgot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDto {
	@Email
	private String email;
}
