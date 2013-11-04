package com.moziy.hollerbacky.connection;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.SyncPayload;
import com.moziy.hollerback.model.web.response.SyncResponse;
import com.moziy.hollerback.model.web.response.VerifyResponse;
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

	public static void postVideo(String conversation_id, String filename,
			final String customMessage) {
		RequestParams params = new RequestParams();

		LogUtil.i("PostVideo", conversation_id);

		params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
				HollerbackAppState.getValidToken());
		params.put(HollerbackAPI.PARAM_FILENAME, filename);
		//TODO - Sajjad: Insert Video model into db, and mark it as pending
		
		HollerbackAsyncClient.getInstance().post(
				String.format(HollerbackAPI.API_VIDEO_POST_FORMAT,
						conversation_id), params,
				new JsonHttpResponseHandler() {

					@Override
					public void onFailure(Throwable arg0, JSONObject arg1) {
						// TODO - Sajjad : Mark video in db as pending_upload and set uploading to false or retry
						super.onFailure(arg0, arg1);
					}

					@Override
					public void onSuccess(int arg0, JSONObject arg1) {
						// TODO Auto-generated method stub
						super.onSuccess(arg0, arg1);
						JSONUtil.processVideoPost(arg1, customMessage);
					}

					@Override
					protected Object parseResponse(String arg0)
							throws JSONException {
						// TODO Auto-generated method stub
						LogUtil.i(arg0);
						return super.parseResponse(arg0);
					}
				});

	}

	public static void postVideoRead(String videoId) {
		RequestParams params = new RequestParams();

		LogUtil.i("Post Read " + videoId);

		params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
				HollerbackAppState.getValidToken());

		HollerbackAsyncClient.getInstance().post(
				String.format(HollerbackAPI.API_VIDEO_READ_FORMAT, videoId),
				params, new JsonHttpResponseHandler() {
					@Override
					protected Object parseResponse(String arg0)
							throws JSONException {
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
						JSONUtil.processSignUp(arg1);
					}
				});

	}

	public static void postRegistration(String name, String phone, String token) {
		RequestParams params = new RequestParams();

		params.put(HollerbackAPI.PARAM_USERNAME, name);
		params.put(HollerbackAPI.PARAM_PHONE, phone);

		params.put(HollerbackAPI.PARAM_PLATFORM, HollerbackConstants.PLATFORM);

		params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, token);
		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_REGISTER,
				params, new JsonHttpResponseHandler() {
					@Override
					protected Object parseResponse(String arg0)
							throws JSONException {
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
						JSONUtil.processSignUp(arg1);
					}
				});

	}
	
	public static void postRegistration(String name, String phone, String token, JsonHttpResponseHandler handler) {
		RequestParams params = new RequestParams();

		params.put(HollerbackAPI.PARAM_USERNAME, name);
		params.put(HollerbackAPI.PARAM_PHONE, phone);

		params.put(HollerbackAPI.PARAM_PLATFORM, HollerbackConstants.PLATFORM);

		params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, token);
		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_REGISTER,
				params, handler);

	}
	
	public static void postVerification(String veroficationCode, String phone, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();

		params.put(HollerbackAPI.PARAM_CODE, veroficationCode);
		params.put(HollerbackAPI.PARAM_PHONE, phone);
		params.put(HollerbackAPI.PARAM_PLATFORM, HollerbackConstants.PLATFORM);
		
		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_VERIFY,
				params, handler);

	}

	public static void postLogin(String email, String password, String token) {

		RequestParams params = null;

		if (!email.isEmpty() && !password.isEmpty()) {
			params = new RequestParams();
			params.put(HollerbackAPI.PARAM_EMAIL, email);
			params.put(HollerbackAPI.PARAM_PASSWORD, password);
			params.put(HollerbackAPI.PARAM_PLATFORM,
					HollerbackConstants.PLATFORM);

			params.put(HollerbackAPI.PARAM_DEVICE_TOKEN, token);
		}
		
		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_SESSION, params, new HBAsyncHttpResponseHandler<VerifyResponse>(new TypeReference<VerifyResponse>(){}) {

			@Override
			public void onResponseSuccess(int statusCode,
					VerifyResponse response) {
				
				Log.d("sajjad", "access token: " + response.access_token);
				JSONUtil.processLogin(response);
				
			}
			
			@Override
			public void onApiFailure(Metadata metaData) {
				Log.d(TAG, "error code: " + metaData.code);
				
			}
		
		});
//				

//		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_SESSION,
//				params, new JsonHttpResponseHandler() {
//
//					@Override
//					protected Object parseResponse(String arg0)
//							throws JSONException {
//						LogUtil.i(arg0);
//						return super.parseResponse(arg0);
//
//					}
//
//					@Override
//					public void onFailure(Throwable arg0, JSONObject arg1) {
//						// TODO Auto-generated method stub
//						super.onFailure(arg0, arg1);
//						LogUtil.i("LOGIN FAILURE");
//					}
//
//					@Override
//					public void onSuccess(int arg0, JSONObject arg1) {
//						// TODO Auto-generated method stub
//						super.onSuccess(arg0, arg1);
//						JSONUtil.processSignIn(arg1);
//					}
//
//				});

	}
	
	public static void postLogin(String phone, AsyncHttpResponseHandler handler) {
		RequestParams params = null;

		if (!phone.isEmpty()) {
			params = new RequestParams();
			params.put(HollerbackAPI.PARAM_PHONE, phone);
		}

		HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_SESSION, params,handler);
	}

	public static void getConversations() {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();
			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			LogUtil.i("Sending token: " + HollerbackAppState.getValidToken());

			HollerbackAsyncClient.getInstance().get(
					HollerbackAPI.API_CONVERSATION, params,
					new JsonHttpResponseHandler() {

						@Override
						protected Object parseResponse(String arg0)
								throws JSONException {
							LogUtil.i(arg0);
							return super.parseResponse(arg0);

						}

						@Override
						public void onFailure(Throwable arg0, JSONObject arg1) {
							// TODO Auto-generated method stub
							super.onFailure(arg0, arg1);
							LogUtil.e(HollerbackAPI.API_CONVERSATION
									+ "FAILURE");
						}

						@Override
						public void onSuccess(int arg0, JSONObject arg1) {
							// TODO Auto-generated method stub
							super.onSuccess(arg0, arg1);
							LogUtil.i("ON SUCCESS API CONVO");
							JSONUtil.processGetConversations(arg1);
						}

					});

		}

	}
	
	public static void sync(){
		if(HollerbackAppState.isValidSession()){
			RequestParams params = new RequestParams();
			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());
			
			HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_SYNC, params, new HBAsyncHttpResponseHandler<Envelope<ArrayList<SyncResponse>>>(new TypeReference<Envelope<ArrayList<SyncResponse>>>() {}) {

				@Override
				public void onResponseSuccess(int statusCode, Envelope<ArrayList<SyncResponse>> response) {
					
					
					
					//TEST CODE: - Verified that data is deserialized properly
//					SyncPayload sync = response.data.get(0).mSync;
//					
//					if(sync instanceof VideoModel){
//						Log.d(TAG, "id:" + ((VideoModel) sync).getId());
//					}else if(sync instanceof ConversationModel){
//						Log.d(TAG, "id:" + ((ConversationModel) sync).getConversation_Id());
//					}
					
				}

				@Override
				public void onApiFailure(Metadata metaData) {
					Log.d(TAG, "metaData code: " + metaData.code);
					
				}

				
			});
		}
	}
	
	
	//TODO - Sajjad : Remove as it's not used
	public static void postConversations(ArrayList<String> contacts) {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();
			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_INVITES, contacts);

			HollerbackAsyncClient.getInstance().post(
					HollerbackAPI.API_CONVERSATION, params,
					new JsonHttpResponseHandler() {

						@Override
						protected Object parseResponse(String arg0)
								throws JSONException {
							LogUtil.i(arg0);
							return super.parseResponse(arg0);

						}

						@Override
						public void onFailure(Throwable arg0, JSONObject arg1) {
							// TODO Auto-generated method stub
							super.onFailure(arg0, arg1);
							LogUtil.e(HollerbackAPI.API_CONVERSATION
									+ "FAILURE");
						}

						@Override
						public void onSuccess(int arg0, JSONObject arg1) {
							// TODO Auto-generated method stub
							super.onSuccess(arg0, arg1);
							LogUtil.i("ON SUCCESS API CONVO");
							JSONUtil.processPostConversations(arg1);
						}

					});

		}
	}
	
	public static void postConversations(ArrayList<String> contacts, AsyncHttpResponseHandler handler) {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();
			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_INVITES, contacts);

			HollerbackAsyncClient.getInstance().post(
					HollerbackAPI.API_CONVERSATION, params,
					handler);

		}else{
			//TODO - Sajjad: Remove for prod
			throw new IllegalStateException("Invalid Session");
		}
	}
	
	
	public static void conversationInvite(ArrayList<String> contacts, JsonHttpResponseHandler handler) {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();
			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_INVITES, contacts);

			HollerbackAsyncClient.getInstance().post(
					HollerbackAPI.API_INVITE, params,
					handler);

		}
	}

	public static void getContacts(ArrayList<UserModel> contacts) {

		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			LogUtil.i("Token: " + HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_NUMBERS,
					HBRequestUtil.generateStringArray(contacts));

			HollerbackAsyncClient.getInstance().get(HollerbackAPI.API_CONTACTS,
					params, new JsonHttpResponseHandler() {

						@Override
						protected Object parseResponse(String arg0)
								throws JSONException {
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
	
	public static void getContacts(ArrayList<UserModel> contacts, JsonHttpResponseHandler handler) {

		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			LogUtil.i("Token: " + HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			params.put(HollerbackAPI.PARAM_NUMBERS,
					HBRequestUtil.generateStringArray(contacts));

			HollerbackAsyncClient.getInstance().post(HollerbackAPI.API_CONTACTS,
					params, handler);
		}
	}

	public static void getConversationVideos(final String conversationId) {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			HollerbackAsyncClient
					.getInstance()
					.get(String.format(
							HollerbackAPI.API_CONVERSATION_DETAILS_VIDEOS_FORMAT,
							conversationId), params,
							new JsonHttpResponseHandler() {

								@Override
								protected Object parseResponse(String arg0)
										throws JSONException {
									LogUtil.i("RESPONSE: " + arg0);
									return super.parseResponse(arg0);

								}

								@Override
								public void onFailure(Throwable arg0,
										JSONObject arg1) {
									// TODO Auto-generated method stub
									super.onFailure(arg0, arg1);
									LogUtil.e(HollerbackAPI.API_CONTACTS
											+ "FAILURE");
								}

								@Override
								public void onSuccess(int arg0, JSONObject arg1) {
									// TODO Auto-generated method stub
									super.onSuccess(arg0, arg1);
									LogUtil.i("ON SUCCESS API CONTACTS");
									JSONUtil.processConversationVideos(
											conversationId, arg1);
								}

							});
		}
	}
	

	public static void getConversation(final String conversationId, JsonHttpResponseHandler handler) {
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			HollerbackAsyncClient
					.getInstance()
					.get(String.format(
							HollerbackAPI.API_CONVERSATION_DETAILS,
							conversationId), params, handler);
		}
	}
	
	public static void leaveConversation(final String conversationId, JsonHttpResponseHandler handler){
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			HollerbackAsyncClient
					.getInstance()
					.post(String.format(
							HollerbackAPI.API_CONVERSATION_LEAVE,
							conversationId), params, handler);
		}
	}
	
	public static void clearNewConversationWatchedStatus(final String conversationId, JsonHttpResponseHandler handler)
	{
		if (HollerbackAppState.isValidSession()) {
			RequestParams params = new RequestParams();

			params.put(HollerbackAPI.PARAM_ACCESS_TOKEN,
					HollerbackAppState.getValidToken());

			HollerbackAsyncClient
					.getInstance()
					.post(String.format(
							HollerbackAPI.API_CONVERSATION_WATCHALL,
							conversationId), params, handler);
		}
	}
}