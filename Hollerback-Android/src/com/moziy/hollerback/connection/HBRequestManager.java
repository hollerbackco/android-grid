package com.moziy.hollerback.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.HollerbackConstants;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.web.response.LoginResponse;
import com.moziy.hollerback.model.web.response.RegisterResponse;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HBRequestUtil;

/**
 * Manage all Requests here so other classes can make request agnostically
 * 
 * @author jianchen
 * 
 */
public class HBRequestManager {

    private static final String TAG = HBRequestManager.class.getSimpleName();

    static boolean isS3Upload;

    @Deprecated
    public static void postVideoRead(String videoId) {
        RequestParams params = new RequestParams();

        LogUtil.i("Post Read " + videoId);

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().post(String.format(HollerbackAPI.API_VIDEO_READ_FORMAT, videoId), params, new JsonHttpResponseHandler() {
            @Override
            protected Object parseResponse(String arg0) throws JSONException {
                LogUtil.i(arg0);
                return super.parseResponse(arg0);

            }

            @Override
            public void onFailure(Throwable arg0, JSONObject arg1) {
                // TODO Auto-generated method stub
                super.onFailure(arg0, arg1);
                LogUtil.i("LOGIN FAILURE");
            }

            @Override
            public void onSuccess(int arg0, JSONObject arg1) {
                // TODO Auto-generated method stub
                super.onSuccess(arg0, arg1);
                // JSONUtil.processSignUp(arg1);
            }
        });

    }

    public static void postMe(String accessToken, String deviceToken, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, accessToken);
        params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, deviceToken);
        params.put(HollerbackAPI.PARAM_DEVICE_ID, AppEnvironment.ANDROID_ID);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_ME, params, responseHandler);

    }

    public static void postRegistration(String email, String password, String userName, String phone, HBHttpResponseHandler<RegisterResponse> responseHandler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_EMAIL, email);
        params.put(HollerbackAPI.PARAM_PASSWORD, password);
        params.put(HollerbackAPI.PARAM_USERNAME, userName);
        params.put(HollerbackAPI.PARAM_PHONE, phone);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_REGISTER, params, responseHandler);

    }

    public static void postVerification(String veroficationCode, String phone, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_CODE, veroficationCode);
        params.put(HollerbackAPI.PARAM_PHONE, phone);
        params.put(HollerbackAPI.PARAM_PLATFORM, HollerbackConstants.PLATFORM);
        params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, GCMUtils.getRegistrationId(HollerbackApplication.getInstance()));
        params.put(HollerbackAPI.PARAM_DEVICE_ID, AppEnvironment.ANDROID_ID);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_VERIFY, params, handler);

    }

    public static void postLogin(String email, String password, String token, HBHttpResponseHandler<LoginResponse> responseHandler) {

        RequestParams params = null;

        if (!email.isEmpty() && !password.isEmpty()) {
            params = new RequestParams();
            params.put(HollerbackAPI.PARAM_EMAIL, email);
            params.put(HollerbackAPI.PARAM_PASSWORD, password);
            params.put(HollerbackAPI.PARAM_PLATFORM, HollerbackConstants.PLATFORM);
            params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, token);
            params.put(HollerbackAPI.PARAM_DEVICE_ID, AppEnvironment.ANDROID_ID);

        }

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_SESSION, params, responseHandler);

    }

    public static void sync(String updatedAt, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        if (updatedAt != null)
            params.put(HollerbackAPI.PARAM_UPDATED_AT, updatedAt);

        HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_SYNC, params, handler);

    }

    public static void createNewConversation(List<String> contacts, AsyncHttpResponseHandler handler) {
        createNewConversation(contacts, null, handler);
    }

    public static void createNewConversation(List<String> phones, String title, AsyncHttpResponseHandler handler) {
        createNewConversation(phones, null, title, handler);
    }

    public static void createNewConversation(List<String> phones, List<String> usernames, String title, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        if (phones != null) {
            params.put(HollerbackAPI.PARAM_INVITES, phones);
        }

        if (usernames != null) {
            params.put(HollerbackAPI.PARAM_USERNAME, usernames);
        }

        if (title != null && !"".equals(title)) {
            params.put(HollerbackAPI.PARAM_NAME, title);
        }

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONVERSATION, params, handler);
    }

    public static void postToConversation(int convoId, String guid, ArrayList<String> partUrls, ArrayList<String> watchedIds, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        params.put(HollerbackAPI.PARAM_GUID, guid);
        params.put(HollerbackAPI.PARAM_PART_URLS, partUrls);
        params.put(HollerbackAPI.PARAM_WATCHED_IDS, watchedIds);
        params.put(HollerbackAPI.PARAM_SUBTITLE, "");
        HollerbackAsyncClient.getInstance().post(String.format(Locale.US, HollerbackAPI.API_CONVERSATION_POST_SEGMENTED, convoId), params, handler);

    }

    public static void conversationInvite(ArrayList<String> contacts, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        params.put(HollerbackAPI.PARAM_INVITES, contacts);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_INVITE, params, handler);
    }

    public static void getContacts(ArrayList<Map<String, String>> contacts, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        if (HollerbackAppState.isValidSession()) {

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
            params.put(HollerbackAPI.PARAM_CONTACTS, contacts);

        } else {
            params.put(HollerbackAPI.PARAM_CONTACTS, contacts);
        }

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONTACTS, params, handler);

    }

    public static void getContacts(ArrayList<UserModel> contacts, JsonHttpResponseHandler handler) {

        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        params.put(HollerbackAPI.PARAM_NUMBERS, HBRequestUtil.generateStringArray(contacts));

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONTACTS, params, handler);

    }

    public static void getConversation(final String conversationId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().get(String.format(HollerbackAPI.API_CONVERSATION_DETAILS, conversationId), params, handler);

    }

    public static void getHistory(long conversationId, AsyncHttpResponseHandler handler) {
        getHistory(conversationId, -1, -1, handler);
    }

    public static void getHistory(long conversationId, int pageNum, int perPage, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        if (pageNum > 0 && perPage > 0) {
            Log.d(TAG, "setting page params");
            params.put(HollerbackAPI.PARAM_PAGE, String.valueOf(pageNum));
            params.put(HollerbackAPI.PARAM_PER_PAGE, String.valueOf(perPage));
        }

        HollerbackAsyncClient.getInstance().get(String.format(HollerbackAPI.API_HISTORY, conversationId), params, handler);
    }

    public static void postTTYL(long conversationId, ArrayList<String> watchedIds, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        params.put(HollerbackAPI.PARAM_WATCHED_IDS, watchedIds);
        HollerbackAsyncClient.getInstance().post(String.format(Locale.US, HollerbackAPI.API_GOODBYE, conversationId), params, handler);

    }

    public static void leaveConversation(final String conversationId, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().post(String.format(HollerbackAPI.API_CONVERSATION_LEAVE, conversationId), params, handler);

    }

    public static void clearNewConversationWatchedStatus(final String conversationId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().post(String.format(HollerbackAPI.API_CONVERSATION_WATCHALL, conversationId), params, handler);

    }

    public static void getFriends(AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_FRIENDS, params, handler);
    }

    public static void addFriends(ArrayList<String> friends, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();
        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        params.put(HollerbackAPI.PARAM_USERNAME, friends);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_ADD_FRIENDS, params, handler);

    }

    public static void removeFriends(ArrayList<String> friends, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        params.put(HollerbackAPI.PARAM_USERNAME, friends);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_REMOVE_FRIENDS, params, handler);
    }

    public static void findFriend(String username, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
        params.put(HollerbackAPI.PARAM_USERNAME, username);

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_FIND_FRIEND, params, handler);

    }

    public static void getUnaddedFriends(AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();

        params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

        HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_UNADDED_FRIENDS, params, handler);
    }
}
