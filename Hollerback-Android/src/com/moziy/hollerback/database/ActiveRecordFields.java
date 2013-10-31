package com.moziy.hollerback.database;

public class ActiveRecordFields {

	// Conversation
	public static final String C_CONV_ID = "ConvId";
	public static final String C_CONV_NAME = "ConvName";
	public static final String C_CONV_UNREAD = "ConvUnread";
	public static final String C_CONV_MOST_RECENT_VIDEO = "ConvRecentVideo";
	public static final String C_CONV_MOST_RECENT_THUMB = "ConvRecentUrl";
	public static final String C_CONV_CREATE_TIME = "ConvCreatTime";
	public static final String C_CONV_URL = "ConvUrl";
	
	// Videos
	public static final String C_VID_ID = "VidId";
	public static final String C_VID_CONV_ID = "VidConvId";
	public static final String C_VID_FILENAME = "VidFilename";
	public static final String C_VID_ISUPLOADING = "VidIsUploading";
	public static final String C_VID_ISREAD = "VidIsRead";
	public static final String C_VID_ISSENT = "VidIsSent";
	public static final String C_VID_FILEURL = "VidFileUrl";
	public static final String C_VID_THUMBURL = "VidThumbUrl";
	public static final String C_VID_USERNAME = "VidUserName";
	public static final String C_VID_CREATEDATE = "VidCreateDate";

	// Users
	public static final String C_USER_ID = "id";
	public static final String C_USER_USERNAME = "username";
	public static final String C_USER_PHONE = "phone";
	public static final String C_USER_NAME = "name";
	public static final String C_USER_EMAIL = "email";
	public static final String C_USER_PHONE_HASHED = "phone_hashed";
	public static final String C_USER_CREATED_AT = "created_at";
	public static final String C_USER_ISNEW = "is_new";
	
	public static final String C_USER_PHOTO = "UserPhoto"; //TODO: still needed?
	
	public static final String C_USER_PHONE_NORMALIZED = "UserPhoneNormalized";
	public static final String C_USER_IS_VERIFIED = "UserIsVerified";
	public static final String C_USER_HOLLERBACK_USER = "UserHollerbackUser";

	// User Conversation Relationship

	// Tables
	public static final String T_CONVERSATION = "Conversation";
	public static final String T_USERS = "Users";
	public static final String T_VIDEOS = "Videos";

}