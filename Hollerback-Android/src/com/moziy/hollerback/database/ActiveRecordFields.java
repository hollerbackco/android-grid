package com.moziy.hollerback.database;

public class ActiveRecordFields {

    // Conversation
    public static final String C_CONV_ID = "conversation_id";
    public static final String C_CONV_NAME = "name";
    public static final String C_CONV_UNREAD = "unread_count";
    public static final String C_CONV_MOST_RECENT_VIDEO = "ConvRecentVideo";
    public static final String C_CONV_MOST_RECENT_THUMB = "ConvRecentUrl";
    public static final String C_CONV_CREATED_AT = "created_at";
    public static final String C_CONV_DELETED_AT = "deleted_at";
    public static final String C_CONV_LAST_MESSAGE_AT = "last_message_at";
    public static final String C_CONV_MOST_RECENT_SUBTITLE = "most_recent_subtitle";
    public static final String C_CONV_MOST_RECENT_THUMB_URL = "most_recent_thumb_url";
    public static final String C_CONV_UNSEEN_COUNT = "unseen_count";
    public static final String C_CONV_USER_ID = "user_id";
    public static final String C_CONV_IS_DELETED = "is_deleted";
    public static final String C_CONV_URL = "ConvUrl";
    public static final String C_CONV_STATE = "state";

    // Videos
    public static final String C_VID_CREATED_AT = "created_at";
    public static final String C_VID_NEEDS_REPLY = "needs_reply";
    public static final String C_VID_SENDER_NAME = "sender_name";
    public static final String C_VID_SENT_AT = "sent_at";
    public static final String C_VID_GUID = "guid";
    public static final String C_VID_ID = "video_id";
    public static final String C_VID_URL = "url";
    public static final String C_VID_THUMBURL = "thumb_url";
    public static final String C_VID_CONV_ID = "conversation_id";
    public static final String C_VID_IS_DELETED = "is_deleted";
    public static final String C_VID_SUBTITLE = "subtitle";
    public static final String C_VID_STATE = "state";
    public static final String C_VID_TRANSACTING = "transacting";
    public static final String C_VID_FILENAME = "local_filename";
    public static final String C_VID_IS_SEGMENTED = "is_segmented";
    public static final String C_VID_SEGMENTED_FILENAME = "segment_filename";
    public static final String C_VID_SEGMENTED_FILE_EXT = "segment_file_extension";
    public static final String C_VID_ISREAD = "isRead";
    public static final String C_VID_NUM_PARTS = "num_parts";
    public static final String C_VID_PART_UPLOAD_STATE = "part_upload_state";
    public static final String C_VID_RECIPIENTS = "recipients";
    public static final String C_VID_WATCHED_STATE = "watched_state";
    public static final String C_VID_GIF_URL = "gif_url";

    public static final String C_VID_ISUPLOADING = "VidIsUploading";
    public static final String C_VID_ISSENT = "VidIsSent";

    // Users
    public static final String C_USER_ID = "user_id";
    public static final String C_USER_USERNAME = "username";
    public static final String C_USER_PHONE = "phone";
    public static final String C_USER_NAME = "name";
    public static final String C_USER_EMAIL = "email";
    public static final String C_USER_PHONE_HASHED = "phone_hashed";
    public static final String C_USER_CREATED_AT = "created_at";
    public static final String C_USER_ISNEW = "is_new";

    public static final String C_USER_PHOTO = "UserPhoto"; // TODO: still needed?

    public static final String C_USER_PHONE_NORMALIZED = "UserPhoneNormalized";
    public static final String C_USER_IS_VERIFIED = "UserIsVerified";
    public static final String C_USER_HOLLERBACK_USER = "UserHollerbackUser";

    // Friends/Contacts
    public static final String C_FRIENDS_NAME = "name";
    public static final String C_FRIENDS_PHONE_LABEL = "phone_label";
    public static final String C_FRIENDS_IS_ON_HOLLERBACK = "is_hb_user";
    public static final String C_FRIENDS_USERNAME = "username";
    public static final String C_FRIENDS_LAST_CONTACT_TIME = "last_contact_time";
    public static final String C_FRIENDS_PHONES = "phones";
    public static final String C_FRIENDS_PHONE_HASHES = "phone_hashes";

    // Tables
    public static final String T_CONVERSATION = "Conversation";
    public static final String T_USERS = "Users";
    public static final String T_VIDEOS = "Videos";
    public static final String T_FRIENDS = "Friends";

}
