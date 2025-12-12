package com.dartsmatcher.legacy.features.friendrequest;

import com.dartsmatcher.legacy.features.user.IUserService;
import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.utils.CustomJsonViews;
import com.dartsmatcher.legacy.utils.Websockets;
import com.fasterxml.jackson.annotation.JsonView;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.springframework.messaging.core.AbstractMessageSendingTemplate.CONVERSION_HINT_HEADER;

@Controller
public class FriendRequestController {

	private final IFriendRequestService friendRequestService;
	private final IUserService userService;
	private final SimpMessagingTemplate messagingTemplate;

	public FriendRequestController(IFriendRequestService friendRequestService, IUserService userService, SimpMessagingTemplate messagingTemplate) {
		this.friendRequestService = friendRequestService;
		this.userService = userService;
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping(Websockets.CREATE_FRIEND_REQUEST)
	@PreAuthorize("isAuthenticated()")
	public void createFriendRequest(@Validated({User.NotNullId.class}) @Payload FriendRequest friendRequest) {
		friendRequestService.createFriendRequest(friendRequest);

		sendUpdatedFriendRequests(friendRequest.getSender().getId(), friendRequest.getReceiver().getId());
	}

	@MessageMapping(Websockets.UPDATE_FRIEND_REQUEST)
	@PreAuthorize("isAuthenticated()")
	public void updateFriendRequest(@DestinationVariable ObjectId friendRequestId,
									@Valid @Payload FriendRequestStatus friendRequestStatus) {

		FriendRequest updatedFriendRequest = friendRequestService.updateFriendRequest(friendRequestId, friendRequestStatus);

		sendUpdatedFriendRequests(updatedFriendRequest.getSender().getId(), updatedFriendRequest.getReceiver().getId());
	}

	@SubscribeMapping(Websockets.GET_FRIEND_REQUESTS)
	@PreAuthorize("isAuthenticated()")
	@JsonView({CustomJsonViews.PublicView.class})
	public ArrayList<FriendRequest> subscribeFriendRequests() {
		User authenticatedUser = userService.getAuthenticatedUser();

		return friendRequestService.getFriendRequests(authenticatedUser.getId());
	}

	private void sendUpdatedFriendRequests(ObjectId senderId, ObjectId receiverId) {
		// Send updated friend requests to sender.

		Map<String, Object> conversionHints = Collections.singletonMap(
				CONVERSION_HINT_HEADER, new Class[]{CustomJsonViews.PublicView.class}
		);

		User sender = userService.getUser(senderId);
		messagingTemplate.convertAndSendToUser(
				sender.getEmail(),
				Websockets.GET_FRIEND_REQUESTS,
				friendRequestService.getFriendRequests(sender.getId()),
				conversionHints
		);

		messagingTemplate.convertAndSendToUser(
				sender.getEmail(),
				Websockets.GET_FRIENDS,
				userService.getUserFriendsDetails(sender.getId()),
				conversionHints
		);

		User receiver = userService.getUser(receiverId);

		// Send updated friend requests to receiver.
		messagingTemplate.convertAndSendToUser(
				receiver.getEmail(),
				Websockets.GET_FRIEND_REQUESTS,
				friendRequestService.getFriendRequests(receiver.getId()),
				conversionHints
		);

		messagingTemplate.convertAndSendToUser(
				receiver.getEmail(),
				Websockets.GET_FRIENDS,
				userService.getUserFriendsDetails(receiver.getId()),
				conversionHints
		);
	}
}
