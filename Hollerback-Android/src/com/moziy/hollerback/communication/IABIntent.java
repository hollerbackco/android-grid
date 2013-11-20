package com.moziy.hollerback.communication;

import android.content.Intent;

public class IABIntent {

    public static final String SESSION_REQUEST = "SessionRequest";
    public static final String REGISTER_REQUEST = "RegisterRequest";
    public static final String GET_CONVERSATIONS = "GetConvo";
    public static final String REQUEST_VIDEO = "VideoRequest";
    public static final String UPLOAD_VIDEO = "UploadVideo";
    public static final String UPLOAD_VIDEO_UPDATE = "UploadVideoUpdate";
    public static final String UPLOAD_VIDEO_UPLOADING = "UploadVideoUploading";
    public static final String GET_CONTACTS = "GetContacts";
    public static final String POST_CONVERSATIONS = "PostConversation";
    public static final String GET_URLS = "GetUrls";
    public static final String GET_CONVERSATION_VIDEOS = "GetConvVideos";
    public static final String POST_READ_VIDEO = "PostReadVideo";

    public static final String GCM_REGISTERED = "gcmregistered";
    public static final String SERVICE_UPLOADVIDEO = "uploadvideo";

    public static final String GCM_MESSAGE = "gcmmsg";

    // Recording Intent
    public static final String RECORDING_FAILED = "RecordingFailed";
    public static final String RECORDING_CANCELLED = "RecordingCancelled";

    // Conversation Intents
    public static final String CONVERSATION_CREATED = "ConvoCreated";
    public static final String CONVERSATION_CREATE_FAILURE = "ConvoCreateFailure";
    public static final String CONVERSATION_UPDATED = "CovoUpdated";

    // Sync Intent
    public static final String NOTIFY_SYNC = "NotifySync";
    public static final String SYNC_FAILED = "SyncFailed";

    public static final String PARAM_SUCCESS = "200";
    public static final String PARAM_FAILURE = "500";
    public static final String PARAM_AUTHENTICATED = "AUTH";
    public static final String PARAM_AUTHENTICATE_REQUIRED = "REQUIRED";
    public static final String PARAM_URI = "URI";
    public static final String PARAM_ID = "ID";
    public static final String PARAM_DATA_TYPE = "DT";
    public static final String PARAM_INTENT_DATA = "idata";
    public static final String PARAM_INTENT_MSG = "imsg";
    public static final String PARAM_GCM_REGISTRATION_ID = "gcmregid";
    public static final String PARAM_CONVERSATION_ID = "converstionId";
    public static final String PARAM_VIDEO_PATH = "videopath";
    public static final String PARAM_VIDEO_STATUS_AS_PERCENT = "statusaspercent";

    // SYNC PARAMS
    public static final String PARAM_SYNC_RESULT = "sync_result"; // type: boolean

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
