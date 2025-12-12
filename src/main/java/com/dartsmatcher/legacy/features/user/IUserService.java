package com.dartsmatcher.legacy.features.user;

import com.dartsmatcher.legacy.features.user.password.change.ChangePasswordDto;
import com.dartsmatcher.legacy.features.user.password.forgot.ForgotPasswordDto;
import com.dartsmatcher.legacy.features.user.password.PasswordDto;
import com.dartsmatcher.legacy.features.user.password.reset.ResetPasswordDto;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Validated
public interface IUserService {

	User saveUser(@Valid User user);

	@PreAuthorize("isAuthenticated()")
	User getAuthenticatedUser();

	User getUser(@NotNull ObjectId userId);

	ArrayList<User> searchUsers(String query);

	@PreAuthorize("isAuthenticated()")
	ArrayList<User> getUserFriendsDetails(ObjectId userId);

	@PreAuthorize("isAuthenticated()")
	User updateUser(@Valid User user);

	@PreAuthorize("isAuthenticated()")
	User updateFriends(ObjectId userId, ArrayList<ObjectId> friends);

	@PreAuthorize("isAuthenticated()")
	void deleteFriend(ObjectId friendId);

	@PreAuthorize("isAuthenticated()")
	void deleteUser(@Valid PasswordDto passwordDto);

	@PreAuthorize("isAuthenticated()")
	void changePassword(@Valid ChangePasswordDto changePasswordDto);

	void forgotPassword(@Valid ForgotPasswordDto forgotPasswordDto);

	void resetPassword(@Valid ResetPasswordDto resetPasswordDto);

}
