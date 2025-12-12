package com.dartsmatcher.legacy.utils;

public class Endpoints {

	private Endpoints() {
	}

	// User Endpoints
	public static final String SAVE_USER = "/users";
	public static final String GET_USER = "/users";
	public static final String GET_USER_BY_ID = "/users/{id}";
	public static final String SEARCH_USER_BY_QUERY = "/users:search";
	public static final String UPDATE_USER = "/users";
	public static final String DELETE_USER = "/users";
	public static final String CHANGE_PASSWORD = "/users/change-password";
	public static final String FORGOT_PASSWORD = "/users/forgot-password";
	public static final String RESET_PASSWORD = "/users/reset-password";

	// Security Endpoints
	public static final String JWKS = "/.well-known/jwks.json";

	// Match Endpoints
	public static final String SAVE_MATCH = "/matches";
	public static final String GET_MATCH = "/matches/{matchId}";
	public static final String GET_ALL_USER_MATCHES = "/matches";
	public static final String UPDATE_MATCH = "/matches/{matchId}";
	public static final String DELETE_MATCH = "/matches/{matchId}";
	public static final String GET_CHECKOUTS = "/checkouts";
}
