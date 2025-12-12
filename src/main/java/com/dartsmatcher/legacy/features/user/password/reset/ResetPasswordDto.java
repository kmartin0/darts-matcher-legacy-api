package com.dartsmatcher.legacy.features.user.password.reset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto {
	@NotNull
	private UUID token;

	@NotBlank
	private String newPassword;

}
