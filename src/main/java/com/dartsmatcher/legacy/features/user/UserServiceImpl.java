package com.dartsmatcher.legacy.features.user;

import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.features.email.IEmailService;
import com.dartsmatcher.legacy.features.friendrequest.FriendsDetails;
import com.dartsmatcher.legacy.features.user.password.PasswordDto;
import com.dartsmatcher.legacy.features.user.password.change.ChangePasswordDto;
import com.dartsmatcher.legacy.features.user.password.forgot.ForgotPasswordDto;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordToken;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordTokenRepository;
import com.dartsmatcher.legacy.features.user.password.reset.ResetPasswordDto;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

	private final UserRepository userRepository;

	private final PasswordTokenRepository passwordTokenRepository;

	private final IEmailService emailService;

	private final PasswordEncoder passwordEncoder;

	private final MessageResolver messageResolver;

	private final Clock clock;

	public UserServiceImpl(UserRepository userRepository, PasswordTokenRepository passwordTokenRepository, IEmailService emailService, PasswordEncoder passwordEncoder, MessageResolver messageResolver, Clock clock) {
		this.userRepository = userRepository;
		this.passwordTokenRepository = passwordTokenRepository;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
		this.messageResolver = messageResolver;
		this.clock = clock;
	}

	/**
	 * Validate uniqueness of a user, encrypt the password and saves the user using the user repository.
	 *
	 * @param user User object to be stored.
	 * @return User stored user.
	 */
	@Override
	public User saveUser(@Valid User user) {
		// Make sure the database assigns a new id by setting it to null.
		user.setId(null);

		// Validate if the user doesn't exist yet. Or throw resource exists exception.
		userRepository.findByEmailIgnoreCase(user.getEmail()).ifPresent(u -> {
			throw new ResourceAlreadyExistsException(User.class, "email", user.getEmail());
		});


		userRepository.findByUserNameIgnoreCase(user.getUserName()).ifPresent(u -> {
			throw new ResourceAlreadyExistsException(User.class, "userName", user.getUserName());
		});

		// Initialize friends array
		user.setFriends(new ArrayList<>());

		// Encrypt the user password.
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Save and return the user.
		return userRepository.save(user);
	}

	/**
	 * @return User current logged in user.
	 */
	@Override
	public User getAuthenticatedUser() {
		// Get authentication context.
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// Resource server only supports JWT authentication so the authentication object must be of type JwtAuthenticationToken
		if (authentication instanceof JwtAuthenticationToken) {
			// Get email from token and return the User object.
			String email = ((Jwt) authentication.getPrincipal()).getClaim("user_name");
			return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new InvalidGrantException(messageResolver.getMessage("exception.username.not.found")));
		} else {
			throw new AuthenticationCredentialsNotFoundException(messageResolver.getMessage("exception.authentication.credentials.not.found"));
		}
	}

	@Override
	public User getUser(ObjectId userId) {
		return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(User.class, userId));

	}

	@Override
	public ArrayList<User> searchUsers(String query) {
		TextCriteria textCriteria = TextCriteria.forDefaultLanguage().caseSensitive(false).matchingAny(query.split("\\s+"));

		return userRepository.findAllBy(textCriteria);
	}

	@Override
	public ArrayList<User> getUserFriendsDetails(ObjectId userId) {

		// TODO: Check if findFriendsDetails return empty arraylist
		return userRepository.findFriendsDetails(userId).orElse(new FriendsDetails(new ArrayList<>())).getFriendsDetails();
	}

	/**
	 * Updates a user if the current principal matches the user to be updated and the password supplied matches.
	 * If a new e-mail is supplied then it's validated that the new e-mail is unique.
	 * Does not change a users' password.
	 *
	 * @param user User with updated values.
	 * @return User updated user.
	 */
	@Override
	public User updateUser(@Valid User user) {
		// Get the user that needs to be updated.
		User userToUpdate = getAuthenticatedUser();

		// Don't allow for the id to be changed.
		user.setId(userToUpdate.getId());

		// Check if the supplied password matches the current password. Else throw ForbiddenException
		if (passwordEncoder.matches(user.getPassword(), userToUpdate.getPassword())) {
			user.setPassword(userToUpdate.getPassword());
		} else {
			throw new InvalidGrantException(messageResolver.getMessage("exception.bad.credentials"));
		}

		// If the email is being changed, check if the new email is not already in use.
		if (!user.getEmail().equals(userToUpdate.getEmail())) {
			userRepository.findByEmailIgnoreCase(user.getEmail()).ifPresent(u -> {
				throw new ResourceAlreadyExistsException(User.class, "email", user.getEmail());
			});
		}
		// If the username is being changed, check if the new username is not already in use.
		if (!user.getUserName().equals(userToUpdate.getUserName())) {
			userRepository.findByUserNameIgnoreCase(user.getUserName()).ifPresent(u -> {
				throw new ResourceAlreadyExistsException(User.class, "userName", user.getUserName());
			});
		}

		// Friends can only be updated by accepting a friend request.
		user.setFriends(userToUpdate.getFriends());

		// TODO: If first, last or username has changed. Update the matches from the user.

		// Save and return the updated user.
		return userRepository.save(user);
	}

	@Override
	public User updateFriends(ObjectId userId, ArrayList<ObjectId> friends) {
		User userToUpdate = getUser(userId);
		userToUpdate.setFriends(friends);

		return userRepository.save(userToUpdate);
	}

	@Override
	public void deleteFriend(ObjectId friendId) {
		User authenticatedUser = getAuthenticatedUser();
		User friendToDelete = getUser(friendId);

		authenticatedUser.getFriends().remove(friendToDelete.getId());
		userRepository.save(authenticatedUser);

		friendToDelete.getFriends().remove(authenticatedUser.getId());
		userRepository.save(friendToDelete);
	}

	/**
	 * Deletes the current principal if the password supplied matches.
	 *
	 * @param passwordDto PasswordDto used to verify a user.
	 */
	@Override
	public void deleteUser(@Valid PasswordDto passwordDto) {
		// Get the user that needs to be deleted using the principal.
		User userToDelete = getAuthenticatedUser();

		// Validate the password matches. Else throw ForbiddenException.
		if (!passwordEncoder.matches(passwordDto.getPassword(), userToDelete.getPassword())) {
			throw new InvalidGrantException(messageResolver.getMessage("exception.bad.credentials"));
		}

		// TODO: Delete the friend requests involving this user.
		// TODO: Check what happens to existing matches from this user.

		// Delete the user
		userRepository.delete(userToDelete);
	}

	/**
	 * Changes a users' password after verifying the supplied password.
	 *
	 * @param changePasswordDto ChangePasswordDto containing the current and new password.
	 */
	@Override
	public void changePassword(@Valid ChangePasswordDto changePasswordDto) {
		// Get the user that needs to be updated.
		User userToUpdate = getAuthenticatedUser();

		// encode and save new password if the old password matches the user password. Else throw forbidden exception.
		if (passwordEncoder.matches(changePasswordDto.getCurrentPassword(), userToUpdate.getPassword())) {
			userToUpdate.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
			userRepository.save(userToUpdate);
		} else {
			throw new InvalidGrantException(messageResolver.getMessage("exception.bad.credentials"));
		}
	}

	/**
	 * Stores a reset password token and sends the user an email containing a reset link.
	 *
	 * @param forgotPasswordDto ForgotPasswordDto used to send the user an email.
	 */
	@Override
	public void forgotPassword(@Valid ForgotPasswordDto forgotPasswordDto) {
		// Find the User the email belongs to.
		Optional<User> dbUser = userRepository.findByEmailIgnoreCase(forgotPasswordDto.getEmail());

		// If user exists create password token and store in repository.
		dbUser.ifPresent(user -> {
			PasswordToken passwordToken = new PasswordToken(
					null,
					UUID.randomUUID(),
					user.getId(),
					LocalDateTime.now(clock).plusDays(7)
			);

			// Store the token in the database.
			passwordTokenRepository.save(passwordToken);

			// Send an email with the reset link
			try {
				emailService.sendForgotPasswordEmail(user, passwordToken);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Changes a users' password using a valid PasswordToken.
	 *
	 * @param resetPasswordDto ResetPasswordDto containing the token and new password.
	 */
	@Override
	public void resetPassword(@Valid ResetPasswordDto resetPasswordDto) {
		// Find the token
		Optional<PasswordToken> passwordToken = passwordTokenRepository.findByToken(resetPasswordDto.getToken());

		// Throw bad credentials exception when password token not present or expired.
		if (!passwordToken.isPresent() || passwordToken.get().getExpiration().isBefore(LocalDateTime.now(clock))) {
			throw new InvalidGrantException(messageResolver.getMessage("exception.password.reset.token"));
		}

		// Change the password and save.
		User user = userRepository.findById(passwordToken.get().getUser()).orElseThrow(() ->
				new ResourceNotFoundException(User.class, "id")
		);

		user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
		userRepository.save(user);

		// Remove the token from repository.
		passwordTokenRepository.delete(passwordToken.get());
	}
}
