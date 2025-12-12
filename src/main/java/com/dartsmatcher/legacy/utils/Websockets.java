package com.dartsmatcher.legacy.utils;

public class Websockets {

	private Websockets() {
	}

	// Match Websocket topics
	public static final String X01_MATCH = "/topic/matches/{matchId}";
	public static final String X01_START_MATCH = "/topic/matches/{matchId}:start";
	public static final String X01_THROW_DART_BOT = "/topic/matches/{matchId}:throw-dart-bot";
	public static final String X01_ADD_THROW = "/topic/matches/{matchId}:add-throw";
	public static final String X01_DELETE_THROW = "/topic/matches/{matchId}:delete-throw";
	public static final String X01_DELETE_SET = "/topic/matches/{matchId}:delete-set";
	public static final String X01_DELETE_LEG = "/topic/matches/{matchId}:delete-leg";

	// User Websocket topics
	public static final String DELETE_FRIEND = "/topic/friends:delete";
	public static final String GET_FRIENDS = "/topic/friends";

	// Friend Requests Websocket topics
	public static final String GET_FRIEND_REQUESTS = "/topic/friends/requests";
	public static final String CREATE_FRIEND_REQUEST = "/topic/friends/requests:create";
	public static final String UPDATE_FRIEND_REQUEST = "/topic/friends/requests/{friendRequestId}:update";




	public static final String ERROR_QUEUE = "/queue/errors";

}
