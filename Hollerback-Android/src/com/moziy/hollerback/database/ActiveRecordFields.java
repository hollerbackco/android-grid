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
	public static final String C_USER_ID = "UserId";
	public static final String C_USER_NAME = "UserName";
	public static final String C_USER_PHOTO = "UserPhoto";
	public static final String C_USER_USERNAME = "UserUserName";
	public static final String C_USER_EMAIL = "UserEmail";
	public static final String C_USER_PHONE = "UserPhone";
	public static final String C_USER_PHONE_NORMALIZED = "UserPhoneNormalized";
	public static final String C_USER_IS_VERIFIED = "UserIsVerified";
	public static final String C_USER_HOLLERBACK_USER = "UserHollerbackUser";

	// User Conversation Relationship

	// Tables
	public static final String T_CONVERSATION = "Conversation";
	public static final String T_USERS = "Users";
	public static final String T_VIDEOS = "Videos";

}