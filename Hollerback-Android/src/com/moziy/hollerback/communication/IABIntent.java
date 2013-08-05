package com.moziy.hollerback.communication;

import android.content.Intent;

public class IABIntent {

	public static final String INTENT_SESSION_REQUEST = "SessionRequest";
	public static final String INTENT_REGISTER_REQUEST = "RegisterRequest";
	public static final String INTENT_GET_CONVERSATIONS = "GetConvo";
	public static final String INTENT_REQUEST_VIDEO = "VideoRequest";
	public static final String INTENT_UPLOAD_VIDEO = "UploadVideo";
	public static final String INTENT_UPLOAD_VIDEO_UPDATE = "UploadVideoUpdate";
	public static final String INTENT_GET_CONTACTS = "GetContacts";
	public static final String INTENT_POST_CONVERSATIONS = "PostConversation";
	public static final String INTENT_GET_URLS = "GetUrls";
	public static final String INTENT_GET_CONVERSATION_VIDEOS = "GetConvVideos";
	public static final String INTENT_POST_READ_VIDEO = "PostReadVideo";

	public static final String INTENT_GCM_REGISTERED = "gcmregistered";

	public static final String INTENT_SERVICE_UPLOADVIDEO = "uploadvideo";

	public static final String GCM_MESSAGE = "gcmmsg";

	public static final String PARAM_SUCCESS = "200";
	public static final String PARAM_FAILURE = "500";
	public static final String PARAM_AUTHENTICATED = "AUTH";
	public static final String PARAM_URI = "URI";
	public static final String PARAM_ID = "ID";
	public static final String PARAM_DATA_TYPE = "DT";
	public static final String PARAM_INTENT_DATA = "idata";
	public static final String PARAM_INTENT_MSG = "imsg";
	public static final String PARAM_GCM_REGISTRATION_ID = "gcmregid";
	public static final String PARAM_CONVERSATION_ID = "converstionId";
	public static final String PARAM_VIDEO_PATH = "videopath";
	public static final String PARAM_VIDEO_STATUS_AS_PERCENT = "statusaspercent";

	public static final String ASYNC_REQ_VIDEOS = "vvideos";

	public static final String MSG_CONVERSATION_ID = "msgconvid";

	public static final boolean VALUE_TRUE = true;
	public static final boolean VALUE_FALSE = false;
	public static final int VALUE_MEM = 1;
	public static final int VALUE_DB = 2;
	public static final int VALUE_API = 3;
	public static final String VALUE_CONV_HASH = "ConvHash";

	public static final int REQUEST_NEW_CONVERSATION = 6728;

	public static boolean isIntent(Intent intent, String action) {
		return intent.getAction().equals(action);
	}

}
