package com.moziy.hollerbacky.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.web.response.LoginResponse;
import com.moziy.hollerback.model.web.response.RegisterResponse;
import com.moziy.hollerback.util.HBRequestUtil;
import com.moziy.hollerback.util.HollerbackAPI;
import com.moziy.hollerback.util.HollerbackAppState;
import com.moziy.hollerback.util.HollerbackConstants;
import com.moziy.hollerback.util.JSONUtil;

/**
 * Manage all Requests here so other classes can make request agnostically
 * 
 * @author jianchen
 * 
 */
public class HBRequestManager {

    private static final String TAG = HBRequestManager.class.getSimpleName();

    static boolean isS3Upload;

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
        }

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_SESSION, params, responseHandler);

    }

    public static void sync(String updatedAt, AsyncHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();
            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
            if (updatedAt != null)
                params.put(HollerbackAPI.PARAM_UPDATED_AT, updatedAt);

            HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_SYNC, params, handler);

        }
    }

    public static void createNewConversation(List<String> contacts, AsyncHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();
            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
            params.put(HollerbackAPI.PARAM_INVITES, contacts);
            // params.put(HollerbackAPI.PARAM_PART_URLS, partUrls);

            HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONVERSATION, params, handler);

        } else {
            // TODO - Sajjad: Remove for prod
            throw new IllegalStateException("Invalid Session");
        }
    }

    public static void postToConversation(int convoId, String guid, ArrayList<String> partUrls, ArrayList<String> watchedIds, AsyncHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();
            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
            params.put(HollerbackAPI.PARAM_GUID, guid);
            params.put(HollerbackAPI.PARAM_PART_URLS, partUrls);
            params.put(HollerbackAPI.PARAM_WATCHED_IDS, watchedIds);
            params.put(HollerbackAPI.PARAM_SUBTITLE, "");
            HollerbackAsyncClient.getInstance().post(String.format(Locale.US, HollerbackAPI.API_CONVERSATION_POST_SEGMENTED, convoId), params, handler);

        } else {
            throw new IllegalStateException("Invalid Session");
        }

    }

    public static void conversationInvite(ArrayList<String> contacts, JsonHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();
            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_INVITES, contacts);

            HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_INVITE, params, handler);

        }
    }

    public static void getContacts(ArrayList<UserModel> contacts) {

        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();

            LogUtil.i("Token: " + HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_NUMBERS, HBRequestUtil.generateStringArray(contacts));

            HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_CONTACTS, params, new JsonHttpResponseHandler() {

                @Override
                protected Object parseResponse(String arg0) throws JSONException {
                    LogUtil.i("RESPONSE: " + arg0);
                    return super.parseResponse(arg0);

                }

                @Override
                public void onFailure(Throwable arg0, JSONObject arg1) {
                    // TODO Auto-generated method stub
                    super.onFailure(arg0, arg1);
                    LogUtil.e(HollerbackAPI.API_CONTACTS + "FAILURE");
                }

                @Override
                public void onSuccess(int arg0, JSONObject arg1) {
                    // TODO Auto-generated method stub
                    super.onSuccess(arg0, arg1);
                    LogUtil.i("ON SUCCESS API CONTACTS");
                    JSONUtil.processGetContacts(arg1);
                }

            });

        }
    }

    public static void getContacts(ArrayList<Map<String, String>> contacts, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        if (HollerbackAppState.isValidSession()) {

            LogUtil.i("Token: " + HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());
            params.put(HollerbackAPI.PARAM_CONTACTS, contacts);

        } else {
            params.put(HollerbackAPI.PARAM_CONTACTS, contacts);
        }

        HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONTACTS, params, handler);

    }

    public static void getContacts(ArrayList<UserModel> contacts, JsonHttpResponseHandler handler) {

        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();

            LogUtil.i("Token: " + HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            params.put(HollerbackAPI.PARAM_NUMBERS, HBRequestUtil.generateStringArray(contacts));

            HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONTACTS, params, handler);
        }
    }

    public static void getConversation(final String conversationId, JsonHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            HollerbackAsyncClient.getInstance().get(String.format(HollerbackAPI.API_CONVERSATION_DETAILS, conversationId), params, handler);
        }
    }

    public static void leaveConversation(final String conversationId, JsonHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            HollerbackAsyncClient.getInstance().post(String.format(HollerbackAPI.API_CONVERSATION_LEAVE, conversationId), params, handler);
        }
    }

    public static void clearNewConversationWatchedStatus(final String conversationId, JsonHttpResponseHandler handler) {
        if (HollerbackAppState.isValidSession()) {
            RequestParams params = new RequestParams();

            params.put(HollerbackAPI.PARAM_ACCESS_TOKEN, HollerbackAppState.getValidToken());

            HollerbackAsyncClient.getInstance().post(String.format(HollerbackAPI.API_CONVERSATION_WATCHALL, conversationId), params, handler);
        }
    }
}
