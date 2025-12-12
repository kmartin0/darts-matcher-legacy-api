package com.dartsmatcher.legacy.features.friendrequest;

import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.utils.CustomJsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "friend_requests")
@TypeAlias("FriendRequest")
public class FriendRequest {

	@MongoId
	@JsonView({CustomJsonViews.PublicView.class})
	private ObjectId id;

	@NotNull
	@Valid
	@JsonView({CustomJsonViews.PublicView.class})
	private User sender;

	@NotNull
	@Valid
	@JsonView({CustomJsonViews.PublicView.class})
	private User receiver;

	@JsonView({CustomJsonViews.PublicView.class})
	private LocalDateTime date;

}
