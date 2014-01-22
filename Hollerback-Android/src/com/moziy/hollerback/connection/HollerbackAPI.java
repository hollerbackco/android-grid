package com.moziy.hollerback.connection;

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
     * GET sync details: /me/sync
     */
    public static final String API_SYNC = "/me/sync";

    /**
     * POST video details: /me/conversations/:id/leave
     */
    public static final String API_CONVERSATION_LEAVE = "/me/conversations/%1$s/leave";

    /**
     * POST Video to conversation with multi parts
     */
    public static final String API_CONVERSATION_POST_SEGMENTED = "/me/conversations/%d/videos/parts";

    /**
     * POST clear conversation watched: /me/conversations/:id/watch_all
     */
    @Deprecated
    public static final String API_CONVERSATION_WATCHALL = "/me/conversations/%1$s/watch_all";

    /**
     * GET video details: /me/conversations/:id/videos
     */
    public static final String API_CONVERSATION_DETAILS_VIDEOS_FORMAT = "/me/conversations/%1$s/videos";

    /**
     * POST "/me/videos/:id/read"
     */
    @Deprecated
    public static final String API_VIDEO_READ_FORMAT = "/me/videos/%1$s/read";

    /**
     * POST new video '/me/conversations/:id/videos'
     */
    @Deprecated
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

    public static final String API_GOODBYE = "/me/conversations/%1$s/goodbye";

    /**
     * History
     */

    // public static final String API_HISTORY = "/me/conversations/%1$s/history";
    public static final String API_HISTORY = "/me/conversations/%1$s/videos";

    /**
     * Friends
     */

    public static final String API_FRIENDS = "/me/friends";

    public static final String API_ADD_FRIENDS = API_FRIENDS + "/add";

    public static final String API_REMOVE_FRIENDS = API_FRIENDS + "/remove";

    public static final String API_FIND_FRIEND = "/me/users/search";

    // /////////////

    public static final String PARAM_EMAIL = "email";

    public static final String PARAM_PASSWORD = "password";

    public static final String PARAM_ACCESS_TOKEN = "access_token";

    public static final String PARAM_PHONE = "phone";

    public static final String PARAM_NAME = "name";

    public static final String PARAM_FILENAME = "filename";

    public static final String PARAM_NUMBERS = "numbers[]";

    public static final String PARAM_INVITES = "invites";

    public static final String PARAM_PLATFORM = "platform";

    public static final String PARAM_DEVICE_ID = "device_id";

    public static final String PARAM_DEVICE_TOKEN = "device_token";

    public static final String PARAM_USERNAME = "username";

    public static final String PARAM_CODE = "code";

    public static final String PARAM_PART_URLS = "part_urls";

    public static final String PARAM_WATCHED_IDS = "watched_ids";

    public static final String PARAM_SUBTITLE = "subtitle";

    public static final String PARAM_UPDATED_AT = "updated_at";

    public static final String PARAM_GUID = "guid";

    public static final String PARAM_CONTACTS = "c";

    public static final String PARAM_CONTACTS_NAME = "n";

    public static final String PARAM_CONTACTS_PHONE = "p";

    public static final String PARAM_PAGE = "page";

    public static final String PARAM_PER_PAGE = "perPage";

    public static interface ErrorCodes {
        public static final int ERROR_403 = 403;
    }
}
