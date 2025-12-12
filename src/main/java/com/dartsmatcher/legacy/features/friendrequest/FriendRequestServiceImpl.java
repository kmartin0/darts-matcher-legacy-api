package com.dartsmatcher.legacy.features.friendrequest;

import com.dartsmatcher.legacy.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.legacy.exceptionhandler.exception.InvalidArgumentsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.exceptionhandler.response.TargetError;
import com.dartsmatcher.legacy.features.user.IUserService;
import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.user.UserRepository;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class FriendRequestServiceImpl implements IFriendRequestService {

	private final IUserService userService;
	private final FriendRequestRepository friendRequestRepository;
	private final MessageResolver messageResolver;
	private final UserRepository userRepository;

	public FriendRequestServiceImpl(IUserService userService, FriendRequestRepository friendRequestRepository, MessageResolver messageResolver, UserRepository userRepository) {
		this.userService = userService;
		this.friendRequestRepository = friendRequestRepository;
		this.messageResolver = messageResolver;
		this.userRepository = userRepository;
	}

	public FriendRequest createFriendRequest(@Valid FriendRequest friendRequest) {
		// Check if sender and receiver exist.
		User sender = userService.getUser(friendRequest.getSender().getId());
		userService.getUser(friendRequest.getReceiver().getId());

		// Check if the sender is the authenticated user.
		User authenticatedUser = userService.getAuthenticatedUser();

		if (!authenticatedUser.getId().equals(sender.getId())) {
			throw new ForbiddenException(messageResolver.getMessage("exception.forbidden"));
		}

		// Check that the user isn't trying to befriend himself.
		if (friendRequest.getSender().getId().equals(friendRequest.getReceiver().getId())) {
			throw new InvalidArgumentsException(new TargetError("receiver", messageResolver.getMessage("message.befriend.yourself")));
		}

		// Check if the friend request doesn't exist already. And if the users aren't friends already.
		if (!friendRequestRepository.findBySenderAndReceiver(friendRequest.getSender().getId(), friendRequest.getReceiver().getId()).isEmpty() ||
				(userRepository.hasFriend(friendRequest.getSender().getId(), friendRequest.getReceiver().getId()) &&
						userRepository.hasFriend(friendRequest.getReceiver().getId(), friendRequest.getSender().getId()))) {
			throw new ResourceAlreadyExistsException(FriendRequest.class, "receiver", friendRequest.getReceiver());
		}

		// Only store the user id's for sender and receiver.
		friendRequest.setSender(User.builder().id(friendRequest.getSender().getId()).build());
		friendRequest.setReceiver(User.builder().id(friendRequest.getReceiver().getId()).build());

		// Add the date.
		friendRequest.setDate(LocalDateTime.now());

		return friendRequestRepository.save(friendRequest);
	}

	public ArrayList<FriendRequest> getFriendRequests(ObjectId userId) {

		return friendRequestRepository.findBySenderOrReceiver(userId, userId).orElse(new ArrayList<>());
	}

	public FriendRequest updateFriendRequest(ObjectId friendRequestId, @Valid FriendRequestStatus status) {
		// Get the friend request
		FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
				.orElseThrow(() -> new ResourceNotFoundException(FriendRequest.class, friendRequestId));

		// Check if the user is allowed to update the friend request.
		User authenticatedUser = userService.getAuthenticatedUser();

		if (!(authenticatedUser.getId().equals(friendRequest.getSender().getId())) &&
				!(authenticatedUser.getId().equals(friendRequest.getReceiver().getId()))) {
			throw new ForbiddenException(messageResolver.getMessage("exception.forbidden"));
		}

		// Accept the friend request when it's accepted. Otherwise simply delete it.
		if (status == FriendRequestStatus.ACCEPTED) acceptFriendRequest(friendRequest);

		// Delete the friend request.
		friendRequestRepository.delete(friendRequest);

		return friendRequest;
	}

	private void acceptFriendRequest(FriendRequest friendRequest) {
		User sender = userService.getUser(friendRequest.getSender().getId());
		User receiver = userService.getUser(friendRequest.getReceiver().getId());

		// Only add the friend to the sender if the receiver isn't already a friend.
		if (!userRepository.hasFriend(sender.getId(), receiver.getId())) {
			sender.getFriends().add(receiver.getId());
			userService.updateFriends(sender.getId(), sender.getFriends());
		}

		// Only add the friend to the receiver if the sender isn't already a friend.
		if (!userRepository.hasFriend(receiver.getId(), sender.getId())) {
			receiver.getFriends().add(sender.getId());
			userService.updateFriends(receiver.getId(), receiver.getFriends());
		}
	}
}
