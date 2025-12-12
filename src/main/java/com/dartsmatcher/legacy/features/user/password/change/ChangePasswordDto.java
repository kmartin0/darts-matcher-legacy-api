package com.dartsmatcher.legacy.features.user.password.change;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {
	@NotBlank
	private String currentPassword;

	@NotBlank
	private String newPassword;
}
