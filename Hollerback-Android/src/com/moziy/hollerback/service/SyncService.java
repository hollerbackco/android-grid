package com.moziy.hollerback.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.SyncResponse;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.HBSyncHttpResponseHandler;

public class SyncService extends IntentService {

    public static final String INTENT_ARG_REQUEST_TIME = "request_time"; // the request time for a sync: System.currentTimeMillis() (now)
    public static final String INTENT_ARG_RETRY_COUNT = "retry_count"; // TODO: put that retry logic in
    public static final String INTENT_ARG_LAST_RETRY_TIME = "last_retry_time";

    private static final String TAG = SyncService.class.getSimpleName();

    private final Handler mHandler = new Handler(); // use a handler to post back and start the service in case we weren't able to sync

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // TODO: add logic for retrying
        int retryCount = intent.getIntExtra(INTENT_ARG_RETRY_COUNT, 0);
        sync();

    }

    private void sync() {
        String lastSynctime = PreferenceManagerUtil.getPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, null);

        // // TEST
        // lastSynctime = null;
        final long start = System.currentTimeMillis();
        HBRequestManager.sync(lastSynctime, new HBSyncHttpResponseHandler<Envelope<ArrayList<SyncResponse>>>(new TypeReference<Envelope<ArrayList<SyncResponse>>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<ArrayList<SyncResponse>> response) {

                Log.d("performance", "time to execute and deserialize: " + (System.currentTimeMillis() - start));
                Log.d(TAG, "sync response succeeded: " + response.meta.last_sync_at);

                // lets save the sync time
                updateModel(response.data);
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, response.meta.last_sync_at);

            }

            @Override
            public void onApiFailure(Metadata metaData) {
                Log.w(TAG, "connection failure during sync");
                if (metaData != null) {
                    Log.w(TAG, "metaData code: " + metaData.code);
                }
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.SYNC_FAILED));
            }

        });

    }

    // XXX: handle the case where server.last_msg_at < client.last_msg_at, but a video has been sent down that doesn't exist in the client db
    /**
     *
     * @param data
     */
    private void updateModel(ArrayList<SyncResponse> data) {

        if (data == null || data.isEmpty()) {
            Intent intent = new Intent(IABIntent.NOTIFY_SYNC);
            intent.putExtra(IABIntent.NOTIFY_SYNC, false);
            IABroadcastManager.sendLocalBroadcast(intent);
            return;
        }

        long start = System.currentTimeMillis();

        Map<Long, ConversationModel> newConvoMap = new HashMap<Long, ConversationModel>();
        Map<String, VideoModel> newVideoMap = new HashMap<String, VideoModel>();

        StringBuilder convoWhereClauseBuilder = new StringBuilder(128);
        StringBuilder videoWhereClauseBuilder = new StringBuilder(128);

        int convoCount = 0;
        int videoCount = 0;

        // lots of duplication?
        for (SyncResponse syncResponse : data) {
            // syncResponse.convert();
            if (SyncResponse.Type.CONVERSATION.equals(syncResponse.type)) {

                ConversationModel convo = (ConversationModel) syncResponse.sync;
                newConvoMap.put(convo.getConversationId(), convo);
                ++convoCount;

                if (convoCount > 1) {
                    convoWhereClauseBuilder.append(" OR ");
                }

                convoWhereClauseBuilder.append(ActiveRecordFields.C_CONV_ID).append("=").append(convo.getConversationId());

            } else if (SyncResponse.Type.MESSAGE.equals(syncResponse.type)) {

                VideoModel video = (VideoModel) syncResponse.sync;
                newVideoMap.put(video.getGuid(), video);
                ++videoCount;

                if (videoCount > 1) {
                    videoWhereClauseBuilder.append(" OR ");
                }

                videoWhereClauseBuilder.append(ActiveRecordFields.C_VID_GUID).append("=").append("'").append(video.getGuid()).append("'");
            }
        }

        List<VideoModel> existingVideos = new ArrayList<VideoModel>();
        if (!newVideoMap.isEmpty()) {
            existingVideos = new Select().from(VideoModel.class).where(videoWhereClauseBuilder.toString()).execute();
        }

        List<ConversationModel> existingConvos = new ArrayList<ConversationModel>();
        if (!newConvoMap.isEmpty()) {
            existingConvos = new Select().from(ConversationModel.class).where(convoWhereClauseBuilder.toString()).execute();
        }

        Log.d(TAG, "existing videos: " + (existingVideos != null ? existingVideos.size() : 0));
        Log.d(TAG, "existing convos: " + (existingConvos != null ? existingConvos.size() : 0));

        // remove existing model for our list
        // conversations.removeAll(existingConvos);
        // videos.removeAll(existingVideos);
        ActiveAndroid.beginTransaction();
        try {

            // if updating fails for any reason, then clear the preference that saves the last sync time
            if (!existingVideos.isEmpty()) { // TODO - look into updating vs removing for performance improvement
                for (VideoModel existingVideo : existingVideos) { // a video's state should not change on the server once we recieve it on the client
                    Log.d(TAG, "KEEPING:\n" + existingVideo.toString());
                    // lets lookup the new video, and make sure that we don't update it unless the update time of the video is different than our update time
                    newVideoMap.remove(existingVideo.getGuid());
                }
            }

            if (!newVideoMap.isEmpty()) {
                // insert the remaining videos into the database
                for (VideoModel newVideo : newVideoMap.values()) {
                    newVideo.setState(VideoModel.ResourceState.PENDING_DOWNLOAD);
                    newVideo.setWatchedState(VideoModel.ResourceState.UNWATCHED);
                    newVideo.save();
                    Log.d(TAG, "ADDING:\n " + newVideo.toString());
                }

            }

            if (!existingConvos.isEmpty()) {

                for (ConversationModel existingConversation : existingConvos) {
                    // lookup the new conversation and look at the last message time
                    ConversationModel newConversation = newConvoMap.get(existingConversation.getConversationId());
                    if (newConversation.getLastMessageAtInMillis() <= existingConversation.getLastMessageAtInMillis()) {
                        newConvoMap.remove(newConversation.getConversationId());
                        Log.d(TAG, "KEEPING:\n" + newConversation.toString());
                    } else {
                        Log.d(TAG, "DELETING:\n" + existingConversation.toString());
                        existingConversation.delete(); // delete it so that it gets updated
                    }
                }

            }

            if (!newConvoMap.isEmpty()) {

                for (ConversationModel newConversation : newConvoMap.values()) {
                    newConversation.save();
                    Log.d(TAG, "ADDING:\n" + newConversation.toString());
                }

            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        Intent intent = new Intent(IABIntent.NOTIFY_SYNC);
        intent.putExtra(IABIntent.NOTIFY_SYNC, true);
        IABroadcastManager.sendLocalBroadcast(intent);
        Log.d("performance", "time to insert to db: " + (System.currentTimeMillis() - start));

    }
}
