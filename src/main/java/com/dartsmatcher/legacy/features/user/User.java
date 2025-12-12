package com.dartsmatcher.legacy.features.user;

import com.dartsmatcher.legacy.utils.CustomJsonViews;
import com.dartsmatcher.legacy.validators.nowhitespace.NoWhitespace;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
@TypeAlias("User")
public class User {

	@MongoId
	@NotNull(groups = NotNullId.class)
	@JsonView({CustomJsonViews.PublicView.class, CustomJsonViews.PrivateView.class})
	private ObjectId id;

	@NotBlank
	@Length(min = 4, max = 24)
	@NoWhitespace
	@TextIndexed
	@JsonView({CustomJsonViews.PublicView.class, CustomJsonViews.PrivateView.class})
//	@ValidMatchPlayerIds(groups = {Create.class, Update.class}) // TODO: FIX CHANGE VALIDATOR TYPE TO STRING AND IN MATCHPLAYER DO ARRAYLIST<@ValidMatchPlayerId String> PLAYERS
	private String userName;

	@NotBlank
	@TextIndexed
	@JsonView({CustomJsonViews.PublicView.class, CustomJsonViews.PrivateView.class})
	private String firstName;

	@NotBlank
	@TextIndexed
	@JsonView({CustomJsonViews.PublicView.class, CustomJsonViews.PrivateView.class})
	private String lastName;

	@NotBlank
	@Email
	@JsonView({CustomJsonViews.PrivateView.class})
	private String email;

	@JsonView({CustomJsonViews.PrivateView.class})
	private ArrayList<ObjectId> friends;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank
	@Length(min = 4, max = 24)
	private String password;

	public interface NotNullId {
	}

}