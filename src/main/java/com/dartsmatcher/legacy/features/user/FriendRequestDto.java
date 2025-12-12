package com.dartsmatcher.legacy.features.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDto {

	@MongoId
	private User id;

	@NotNull
	private User sender;

	@NotNull
	private ObjectId receiver;

	private LocalDateTime date;

}
