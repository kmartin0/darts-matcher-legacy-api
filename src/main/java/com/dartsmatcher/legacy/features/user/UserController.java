package com.dartsmatcher.legacy.features.user;

import com.dartsmatcher.legacy.features.user.password.PasswordDto;
import com.dartsmatcher.legacy.features.user.password.change.ChangePasswordDto;
import com.dartsmatcher.legacy.features.user.password.forgot.ForgotPasswordDto;
import com.dartsmatcher.legacy.features.user.password.reset.ResetPasswordDto;
import com.dartsmatcher.legacy.utils.CustomJsonViews;
import com.dartsmatcher.legacy.utils.Endpoints;
import com.dartsmatcher.legacy.utils.Websockets;
import com.fasterxml.jackson.annotation.JsonView;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.springframework.messaging.core.AbstractMessageSendingTemplate.CONVERSION_HINT_HEADER;

@RestController
@Controller
@Validated
public class UserController {

	private final IUserService userService;
	private final SimpMessagingTemplate messagingTemplate;

	public UserController(IUserService userService, SimpMessagingTemplate messagingTemplate) {
		this.userService = userService;
		this.messagingTemplate = messagingTemplate;
	}

	@PostMapping(path = Endpoints.SAVE_USER, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public User saveUser(@Valid @RequestBody User user) {

		return userService.saveUser(user);
	}

	@GetMapping(path = Endpoints.GET_USER)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public User getAuthenticatedUser() {

		return userService.getAuthenticatedUser();
	}

	@GetMapping(path = Endpoints.GET_USER_BY_ID)
	@ResponseStatus(HttpStatus.OK)
	public User getUserById(@PathVariable ObjectId id) {

		return userService.getUser(id);
	}

	@GetMapping(path = Endpoints.SEARCH_USER_BY_QUERY)
	@ResponseStatus(HttpStatus.OK)
	@JsonView(CustomJsonViews.PublicView.class)
	public ArrayList<User> searchUsers(@RequestParam @Valid @NotBlank String query) {

		return userService.searchUsers(query);
	}

	@PutMapping(path = Endpoints.UPDATE_USER)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public User updateUser(@Valid @RequestBody User user) {

		return userService.updateUser(user);
	}

	@DeleteMapping(path = Endpoints.DELETE_USER)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void deleteUser(@Valid @RequestBody PasswordDto passwordDto) {

		userService.deleteUser(passwordDto);
	}

	@PostMapping(path = Endpoints.CHANGE_PASSWORD)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {

		userService.changePassword(changePasswordDto);
	}

	@PostMapping(path = Endpoints.FORGOT_PASSWORD)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {

		userService.forgotPassword(forgotPasswordDto);
	}

	@PostMapping(path = Endpoints.RESET_PASSWORD)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {

		userService.resetPassword(resetPasswordDto);
	}

	@MessageMapping(Websockets.DELETE_FRIEND)
	@PreAuthorize("isAuthenticated()")
	public void deleteFriend(@Payload ObjectId friendId) {
		userService.deleteFriend(friendId);

		Map<String, Object> conversionHints = Collections.singletonMap(
				CONVERSION_HINT_HEADER, new Class[]{CustomJsonViews.PublicView.class}
		);

		User authenticatedUser = userService.getAuthenticatedUser();
		messagingTemplate.convertAndSendToUser(
				authenticatedUser.getEmail(),
				Websockets.GET_FRIENDS,
				userService.getUserFriendsDetails(authenticatedUser.getId()),
				conversionHints
		);

		User friendToDelete = userService.getUser(friendId);
		messagingTemplate.convertAndSendToUser(
				friendToDelete.getEmail(),
				Websockets.GET_FRIENDS,
				userService.getUserFriendsDetails(friendToDelete.getId()),
				conversionHints
		);
	}

	@SubscribeMapping(Websockets.GET_FRIENDS)
	@PreAuthorize("isAuthenticated()")
	@JsonView(CustomJsonViews.PublicView.class)
	public ArrayList<User> subscribeFriends() {
		User authenticatedUser = userService.getAuthenticatedUser();

		return userService.getUserFriendsDetails(authenticatedUser.getId());
	}

}
