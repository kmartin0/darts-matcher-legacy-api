package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.legacy.config.LocaleConfig;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.features.email.IEmailService;
import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.user.UserRepository;
import com.dartsmatcher.legacy.features.user.UserServiceImpl;
import com.dartsmatcher.legacy.features.user.password.PasswordDto;
import com.dartsmatcher.legacy.features.user.password.change.ChangePasswordDto;
import com.dartsmatcher.legacy.features.user.password.forgot.ForgotPasswordDto;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordToken;
import com.dartsmatcher.legacy.features.user.password.reset.PasswordTokenRepository;
import com.dartsmatcher.legacy.features.user.password.reset.ResetPasswordDto;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith({SpringExtension.class})
@ContextConfiguration
@Import(LocaleConfig.class)
class UserServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private MessageResolver messageResolver;

	@Mock
	private PasswordTokenRepository passwordTokenRepository;

	@Mock
	private IEmailService emailService;

	@Mock
	private Clock clock;

	@InjectMocks
	@Spy
	private UserServiceImpl userService;


	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private Validator validator;

	@Test
	void testSaveUser_isSaved() {
		// Given
		User user = createUser();
		Mockito.when(passwordEncoder.encode(any())).thenReturn("encryptedPassword");

		// When
		userService.saveUser(user);

		// Then
		Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
		Assertions.assertEquals(0, constraintViolations.size());
		Assertions.assertEquals("encryptedPassword", user.getPassword());
		Mockito.verify(userRepository, Mockito.times(1)).save(user);
	}

	@Test
	void testSaveExistingUserEmail_throwsResourceAlreadyExistsException() {
		// Given an existing user.
		User existingUser = createUser();
		Mockito.when(userRepository.findByEmailIgnoreCase(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

		// When saving user with the same email Then expect ResourceAlreadyExists exception.
		Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> userService.saveUser(existingUser));
	}

	@Test
	void testSaveExistingUsername_throwsResourceAlreadyExistsException() {
		// Given an existing user.
		User existingUser = createUser();
		Mockito.when(userRepository.findByUserNameIgnoreCase(existingUser.getUserName())).thenReturn(Optional.of(existingUser));

		// When saving user with the same email Then expect ResourceAlreadyExists exception.
		Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> userService.saveUser(existingUser));
	}

	@Test
	void testGetUserByAuthentication_isAuthenticatedUser() {
		// Authenticated user
		User user = createUser();

		// Authenticated jwt.
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "RS256")
				.claim("user_name", user.getEmail())
				.build();
		Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_all");
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

		// Setup security context to return the authenticated jwt.
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(token);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

		// Assert getAuthenticatedUser returns the authenticated user.
		Assertions.assertEquals(user, userService.getAuthenticatedUser());
	}

	@Test
	void testGetUserByAuthenticationNonExistentUser_throwsBadCredentialsException() {
		// Authenticated user
		User user = createUser();

		// Authenticated jwt.
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "RS256")
				.claim("user_name", user.getEmail())
				.build();
		Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_all");
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

		// Setup security context to return the authenticated jwt.
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(token);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.empty());

		// Assert getAuthenticatedUser throws InvalidGrantException.
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.getAuthenticatedUser());
	}

	@Test
	@WithMockUser(username = "johndoe@gmail.com")
	void testGetUserByAuthenticationFaultyAuthentication_throwsAuthenticationCredentialsNotFoundException() {
		// Assert getAuthenticatedUser throws AuthenticationCredentialsNotFoundException.
		Assertions.assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getAuthenticatedUser());
	}

	@Test
	void testUpdateUser_isUpdated() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		User userToUpdate = createUser();
		userToUpdate.setEmail("newEmail@gmail.com");
		userToUpdate.setUserName("newUsername");
		userToUpdate.setFirstName("newFirstname");

		Mockito.when(passwordEncoder.matches(userToUpdate.getPassword(), authenticatedUser.getPassword())).thenReturn(true);
		Mockito.when(userRepository.save(any())).thenReturn(userToUpdate);

		// When
		User updatedUser = userService.updateUser(userToUpdate);

		// Then
		Set<ConstraintViolation<User>> constraintViolations = validator.validate(updatedUser);
		Assertions.assertEquals(0, constraintViolations.size());
		Mockito.verify(userRepository, Mockito.times(1)).save(updatedUser);
	}

	@Test
	void testUpdateUserToExistingEmail_throwsResourceAlreadyExistsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		User userToUpdate = createUser();
		userToUpdate.setEmail("janedoe@gmail.com");

		Mockito.when(passwordEncoder.matches(userToUpdate.getPassword(), authenticatedUser.getPassword())).thenReturn(true);
		Mockito.when(userRepository.findByEmailIgnoreCase(userToUpdate.getEmail())).thenReturn(Optional.of(new User()));

		// When update user Then expect ResourceAlreadyExistsException
		Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateUser(userToUpdate));
	}

	@Test
	void testUpdateUserToExistingUsername_throwsResourceAlreadyExistsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		User userToUpdate = createUser();
		userToUpdate.setUserName("janeDoe1");

		Mockito.when(passwordEncoder.matches(userToUpdate.getPassword(), authenticatedUser.getPassword())).thenReturn(true);
		Mockito.when(userRepository.findByUserNameIgnoreCase(userToUpdate.getUserName())).thenReturn(Optional.of(new User()));

		// When update user Then expect ResourceAlreadyExistsException
		Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateUser(userToUpdate));
	}

	@Test
	void testUpdateUserNotEmailOrUsername_doesNotThrowResourceAlreadyExistsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		User userToUpdate = createUser();
		userToUpdate.setFirstName("Jane");
		userToUpdate.setLastName("Doe");

		Mockito.when(passwordEncoder.matches(userToUpdate.getPassword(), authenticatedUser.getPassword())).thenReturn(true);
		Mockito.when(userRepository.save(any())).thenReturn(userToUpdate);

		// When
		userService.updateUser(userToUpdate);

		// When update user Then expect ResourceAlreadyExistsException
		Mockito.verify(userRepository, Mockito.times(0)).findByEmailIgnoreCase(userToUpdate.getEmail());
		Mockito.verify(userRepository, Mockito.times(0)).findByUserNameIgnoreCase(userToUpdate.getUserName());
	}

	@Test
	void testUpdateUserIncorrectPassword_throwsBadCredentialsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();

		Mockito.when(passwordEncoder.matches(any(), any())).thenReturn(false);

		// When update user Then expect InvalidGrantException
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.updateUser(authenticatedUser));
	}

	@Test
	void testDeleteUser_deletesUser() {
		// Given
		User authenticatedUser = setAuthenticatedUser();

		Mockito.when(passwordEncoder.matches(authenticatedUser.getPassword(), authenticatedUser.getPassword())).thenReturn(true);

		// When
		userService.deleteUser(new PasswordDto(authenticatedUser.getPassword()));

		// Then
		Mockito.verify(userRepository, Mockito.times(1)).delete(authenticatedUser);
	}

	@Test
	void testDeleteUserIncorrectPassword_throwsBadCredentialsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();

		Mockito.when(passwordEncoder.matches(any(), any())).thenReturn(false);

		// When delete user Then expect InvalidGrantException
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.deleteUser(new PasswordDto(authenticatedUser.getPassword())));
	}

	@Test
	void testChangePassword_changesPassword() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		ChangePasswordDto changePasswordDto = new ChangePasswordDto(authenticatedUser.getPassword(), "newPassword");

		Mockito.when(passwordEncoder.matches(authenticatedUser.getPassword(), authenticatedUser.getPassword())).thenReturn(true);
		Mockito.when(passwordEncoder.encode("newPassword")).thenReturn("encryptedPassword");

		// When
		userService.changePassword(changePasswordDto);

		// Then
		ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
		Mockito.verify(userRepository).save(argument.capture());
		Assertions.assertEquals("encryptedPassword", argument.getValue().getPassword());
	}

	@Test
	void testChangePasswordIncorrectPassword_throwsBadCredentialsException() {
		// Given
		User authenticatedUser = setAuthenticatedUser();
		ChangePasswordDto changePasswordDto = new ChangePasswordDto(authenticatedUser.getPassword(), "newPassword");

		Mockito.when(passwordEncoder.matches(any(), any())).thenReturn(false);

		// When delete user Then expect InvalidGrantException
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.changePassword(changePasswordDto));
	}

	@Test
	void testForgotPassword_registersTokenAndSendsResetLink() {
		// Given
		User user = createUser();
		ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto("johndoe@gmail.com");

		Mockito.when(userRepository.findByEmailIgnoreCase(forgotPasswordDto.getEmail())).thenReturn(Optional.of(user));

		Clock fixedClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.systemDefault());
		Mockito.doReturn(fixedClock.instant()).when(clock).instant();
		Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();

		// When invoke forgotPassword
		userService.forgotPassword(forgotPasswordDto);

		// Then verify a password token is saved for the user with an expiration of 7 days.
		ArgumentCaptor<PasswordToken> argument = ArgumentCaptor.forClass(PasswordToken.class);

		Mockito.verify(passwordTokenRepository).save(argument.capture());
		Assertions.assertEquals(LocalDateTime.now(fixedClock).plusDays(7), argument.getValue().getExpiration());
		Assertions.assertEquals(user.getId(), argument.getValue().getUser());
	}

	@Test
	void testForgotPasswordNonExistentUser_doesNothing() throws MessagingException {
		// Given
		ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto("johndoe@gmail.com");
		Mockito.when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

		// When
		userService.forgotPassword(forgotPasswordDto);

		// Then
		Mockito.verify(passwordTokenRepository, Mockito.times(0)).save(any());
		Mockito.verify(emailService, Mockito.times(0)).sendForgotPasswordEmail(any(), any());
	}

	@Test
	void testResetPassword_resetsPassword() {
		User user = createUser();

		Clock fixedClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.systemDefault());
		Mockito.doReturn(fixedClock.instant()).when(clock).instant();
		Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();

		PasswordToken passwordToken = new PasswordToken(new ObjectId(), UUID.randomUUID(), user.getId(), LocalDateTime.now(clock).plusDays(3));
		ResetPasswordDto resetPasswordDto = new ResetPasswordDto(passwordToken.getToken(), "newPass");

		Mockito.when(passwordEncoder.encode(resetPasswordDto.getNewPassword())).thenReturn("newEncryptedPassword");
		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		Mockito.when(passwordTokenRepository.findByToken(passwordToken.getToken())).thenReturn(Optional.of(passwordToken));

		// When
		userService.resetPassword(resetPasswordDto);

		// Then
		// Assert the reset token is removed from the repository.
		ArgumentCaptor<PasswordToken> argumentToken = ArgumentCaptor.forClass(PasswordToken.class);
		Mockito.verify(passwordTokenRepository).delete(argumentToken.capture());
		Assertions.assertEquals(passwordToken.getId(), argumentToken.getValue().getId());

		// Assert the user password is changed.
		ArgumentCaptor<User> argumentUser = ArgumentCaptor.forClass(User.class);
		Mockito.verify(userRepository).save(argumentUser.capture());
		Assertions.assertEquals(user.getId(), argumentUser.getValue().getId());
		Assertions.assertEquals("newEncryptedPassword", argumentUser.getValue().getPassword());
	}

	@Test
	void testResetPasswordNonExistent_throwsBadCredentialsException() {
		User user = createUser();

		Clock fixedClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.systemDefault());
		Mockito.doReturn(fixedClock.instant()).when(clock).instant();
		Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();

		PasswordToken passwordToken = new PasswordToken(new ObjectId(), UUID.randomUUID(), user.getId(), LocalDateTime.now(clock).plusDays(3));
		ResetPasswordDto resetPasswordDto = new ResetPasswordDto(passwordToken.getToken(), "newPass");

		Mockito.when(passwordTokenRepository.findByToken(passwordToken.getToken())).thenReturn(Optional.empty());

		// When reset password Then expect InvalidGrantException
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.resetPassword(resetPasswordDto));
	}

	@Test
	void testResetPasswordExpired_throwsBadCredentialsException() {
		User user = createUser();

		Clock fixedClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.systemDefault());
		Mockito.doReturn(fixedClock.instant()).when(clock).instant();
		Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();

		PasswordToken passwordToken = new PasswordToken(new ObjectId(), UUID.randomUUID(), user.getId(), LocalDateTime.now(clock).minusDays(3));
		ResetPasswordDto resetPasswordDto = new ResetPasswordDto(passwordToken.getToken(), "newPass");

		Mockito.when(passwordTokenRepository.findByToken(passwordToken.getToken())).thenReturn(Optional.of(passwordToken));

		// When reset password Then expect InvalidGrantException
		Assertions.assertThrows(InvalidGrantException.class, () -> userService.resetPassword(resetPasswordDto));
	}

	@Test
	void testResetPasswordNonExistentUser_throwsBadCredentialsException() {
		User user = createUser();

		Clock fixedClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.systemDefault());
		Mockito.doReturn(fixedClock.instant()).when(clock).instant();
		Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();

		PasswordToken passwordToken = new PasswordToken(new ObjectId(), UUID.randomUUID(), user.getId(), LocalDateTime.now(clock).plusDays(3));
		ResetPasswordDto resetPasswordDto = new ResetPasswordDto(passwordToken.getToken(), "newPass");

		Mockito.when(passwordTokenRepository.findByToken(passwordToken.getToken())).thenReturn(Optional.of(passwordToken));
		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

		// When reset password Then expect BadCredentialsException
		Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.resetPassword(resetPasswordDto));
	}

	User createUser() {
		return new User(
				new ObjectId(),
				"johndoe1",
				"John",
				"Doe",
				"johndoe@gmail.com",
				new ArrayList<>(),
				"pass123"
		);
	}

	User setAuthenticatedUser() {
		// Authenticated user
		User authenticatedUser = createUser();

		// Authenticated jwt.
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "RS256")
				.claim("user_name", authenticatedUser.getEmail())
				.build();
		Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_all");
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

		// Setup security context to return the authenticated jwt.
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(token);
		SecurityContextHolder.setContext(securityContext);

		Mockito.doReturn(authenticatedUser).when(userService).getAuthenticatedUser();
		return authenticatedUser;
	}

}
