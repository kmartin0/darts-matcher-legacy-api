package com.dartsmatcher.legacy.features.user.password.reset;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_tokens")
@TypeAlias("PasswordToken")
public class PasswordToken {

	@MongoId
	private ObjectId id;

	@NotBlank
	private UUID token;

	@NotBlank
	private ObjectId user;

	@NotNull
	private LocalDateTime expiration;
}
