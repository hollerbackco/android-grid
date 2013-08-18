package com.moziy.hollerback.util;

public class HollerbackAPI {

	public static final String API_SUFFIX = "/api";

	public static final String API_ME = "/me"; // get, post
	public static final String API_SESSION = "/session";

	/**
	 * GET & POST
	 */
	public static final String API_CONVERSATION = "/me/conversations";

	/**
	 * GET video details: /me/conversations/:id
	 */
	public static final String API_CONVERSATION_DETAILS = "/me/conversations/%1$s";
	
	/**
	 * POST video details: /me/conversations/:id/leave
	 */
	public static final String API_CONVERSATION_LEAVE= "/me/conversations/%1$s/leave";

	/**
	 * POST clear conversation watched: /me/conversations/:id/watch_all
	 */
	public static final String API_CONVERSATION_WATCHALL= "/me/conversations/%1$s/watch_all";

	/**
	 * GET video details: /me/conversations/:id/videos
	 */
	public static final String API_CONVERSATION_DETAILS_VIDEOS_FORMAT = "/me/conversations/%1$s/videos";

	/**
	 * POST "/me/videos/:id/read"
	 */
	public static final String API_VIDEO_READ_FORMAT = "/me/videos/%1$s/read";

	/**
	 * POST new video '/me/conversations/:id/videos'
	 */
	public static final String API_VIDEO_POST_FORMAT = "/me/conversations/%1$s/videos";

	
	/**
	 * POST "/me/videos/:id/read"
	 */
	public static final String API_INVITE = "/me/invites";
	
	/**
	 * POST
	 */
	public static final String API_REGISTER = "/register";
	
	public static final String API_VERIFY = "/verify";

	public static final String API_CONTACTS = "/contacts/check";

	// /////////////

	public static final String PARAM_EMAIL = "email";

	public static final String PARAM_PASSWORD = "password";

	public static final String PARAM_ACCESS_TOKEN = "access_token";

	public static final String PARAM_PHONE = "phone";

	public static final String PARAM_NAME = "name";

	public static final String PARAM_FILENAME = "filename";

	public static final String PARAM_NUMBERS = "numbers[]";

	public static final String PARAM_INVITES = "invites[]";
	
	public static final String PARAM_PLATFORM = "platform";
	
	public static final String PARAM_DEVICE_TOKEN = "device_token";
			
	public static final String PARAM_USERNAME = "username";
	
	public static final String PARAM_CODE = "code";
}
