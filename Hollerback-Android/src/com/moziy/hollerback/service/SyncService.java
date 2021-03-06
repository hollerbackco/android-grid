package com.moziy.hollerback.service;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.connection.HollerbackAPI;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.gcm.GCMBroadcastReceiver;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.Sender;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.SyncResponse;
import com.moziy.hollerback.util.NotificationUtil;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil.RecoveryClient;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class SyncService extends IntentService implements RecoveryClient {

    public static final String INTENT_ARG_REQUEST_TIME = "request_time"; // the request time for a sync: System.currentTimeMillis() (now)
    public static final String INTENT_ARG_RETRY_COUNT = "retry_count"; // TODO: put that retry logic in
    public static final String INTENT_ARG_LAST_RETRY_TIME = "last_retry_time";
    private static final int DEFAULT_RETRY_COUNT = 2;

    private static final String TAG = SyncService.class.getSimpleName();

    private final Handler mHandler = new Handler(); // use a handler to post back and start the service in case we weren't able to sync

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean fromGcm = intent.hasExtra(GCMBroadcastReceiver.FROM_GCM_INTENT_ARG);

        // TODO: add logic for retrying
        int retryCount = intent.getIntExtra(INTENT_ARG_RETRY_COUNT, 0);

        try {
            HollerbackAppState.sSyncSemaphore.acquire(); // make sure that we can progress
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: reschedule?
            return;
        }

        sync(intent, fromGcm);

        HollerbackAppState.sSyncSemaphore.release();

    }

    private void sync(final Intent intent, final boolean fromGCM) {
        final String lastSynctime = PreferenceManagerUtil.getPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, null);

        final boolean[] isDone = {
            false
        };

        HBRequestManager.sync(lastSynctime, new HBSyncHttpResponseHandler<Envelope<ArrayList<SyncResponse>>>(new TypeReference<Envelope<ArrayList<SyncResponse>>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<ArrayList<SyncResponse>> response) {

                ResourceRecoveryUtil.removeRecoveryRequest(SyncService.this);

                Set<Sender> sendersInfo = new HashSet<Sender>();

                // update the model with the response
                boolean modelUpdated = updateModel(response.data, sendersInfo);

                // lets save the sync time
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, response.meta.last_sync_at);

                // now that we've synced lets see whether we should launch the background loader or not
                boolean launchedBgLoader = false;

                if (fromGCM && modelUpdated) { // launch only if model was udpated

                    launchedBgLoader = launchDownloadService(intent); // the download service will manage completing the wakeful intent

                    if (!launchedBgLoader) {
                        GCMBroadcastReceiver.completeWakefulIntent(intent);
                    }

                } else if (fromGCM) {

                    GCMBroadcastReceiver.completeWakefulIntent(intent);

                }

                if (modelUpdated && !launchedBgLoader) { // launch a notification that there was a successful sync
                    // launch a notification
                    Log.d(TAG, "launching notification");
                    for (Sender sender : sendersInfo) {
                        String message = NotificationUtil.generateNewVideoMessage(getApplicationContext(), sender);
                        NotificationUtil.launchNotification(getApplicationContext(), NotificationUtil.generateNotification(getString(R.string.app_name), message), (int) sender.getConversationId());
                    }

                }

            }

            @Override
            public void onApiFailure(Metadata metaData) {

                // if there was an error, lets update the sync time to the last one we had
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, lastSynctime);

                Log.w(TAG, "connection failure during sync");

                if (metaData != null) {

                    Log.w(TAG, "metaData code: " + metaData.code);

                    if (metaData.code == HollerbackAPI.ErrorCodes.ERROR_403) {
                        IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.AUTH_EXCEPTION));
                        ResourceRecoveryUtil.removeRecoveryRequest(SyncService.this);
                    } else {
                        // request recovery, if we're not in recovery mode
                        ResourceRecoveryUtil.requestRecovery(SyncService.this);
                    }

                } else {
                    // request recovery, if we're not in recovery mode
                    ResourceRecoveryUtil.requestRecovery(SyncService.this);
                }

                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.SYNC_FAILED));

                if (fromGCM) { // clear the wakeful intent since there was a failure
                    GCMBroadcastReceiver.completeWakefulIntent(intent);
                }
            }

            @Override
            public void onPostResponse() {
                super.onPostResponse();
                isDone[0] = true;
            }

        });

        while (!isDone[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // XXX: handle the case where server.last_msg_at < client.last_msg_at, but a video has been sent down that doesn't exist in the client db
    /**
     * 
     * @param data
     * @param senders - out parameter, gives the unique set of senders
     * @return
     */
    private boolean updateModel(ArrayList<SyncResponse> data, Set<Sender> senders) {

        if (data == null || data.isEmpty()) {
            Intent intent = new Intent(IABIntent.NOTIFY_SYNC);
            intent.putExtra(IABIntent.NOTIFY_SYNC, false);
            IABroadcastManager.sendLocalBroadcast(intent);
            return false; // there was no change
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
                for (VideoModel existingVideo : existingVideos) { // a video's state should not change on the server once we recieve it on the client : WRONG: you can watch it from another device

                    // lets lookup the new video, and make sure that we don't update it unless the update time of the video is different than our update time
                    VideoModel newVideo = newVideoMap.get(existingVideo.getGuid());
                    if (newVideo != null && newVideo.isRead()) {

                        Log.d(TAG, "UPDATING EXISTING:\n" + existingVideo.toString());

                        existingVideo.setRead(true);
                        existingVideo.setWatchedState(VideoModel.ResourceState.WATCHED_AND_POSTED);

                    } else {
                        Log.d(TAG, "KEEPING EXISTING AS IS:\n" + existingVideo.toString());
                    }

                    // update the file and thumb
                    existingVideo.setFileUrl(newVideo.getFileUrl());
                    existingVideo.setThumbUrl(newVideo.getThumbUrl());
                    existingVideo.save();

                    newVideoMap.remove(existingVideo.getGuid());
                }
            }

            if (!newVideoMap.isEmpty()) {
                // insert the remaining videos into the database
                for (VideoModel newVideo : newVideoMap.values()) {
                    if (!newVideo.isRead()) { // if the new video hasn't been read
                        Log.d(TAG, "ADDING NEW UNREAD:\n " + newVideo.toString());
                        newVideo.setState(VideoModel.ResourceState.PENDING_DOWNLOAD);
                        newVideo.setWatchedState(VideoModel.ResourceState.UNWATCHED);
                        newVideo.save();
                        senders.add(new Sender(newVideo)); // get a set of all senders

                    } else { // otherwise, this video has been read
                        Log.d(TAG, "ADDING NEW READ:\n" + newVideo.toString());
                        newVideo.setWatchedState(VideoModel.ResourceState.WATCHED_AND_POSTED);
                        newVideo.setState(VideoModel.ResourceState.PENDING_DOWNLOAD); // we haven't downloaded it
                        newVideo.save();

                    }

                }

            }

            synchronized (ConversationModel.class) { // important stuff happening, ensure the utmost caution

                if (!existingConvos.isEmpty()) {

                    for (ConversationModel existingConversation : existingConvos) {

                        // lookup the new conversation and look at the last message time

                        ConversationModel newConversation = newConvoMap.get(existingConversation.getConversationId());

                        // lets see if this existing conversation had any watched videos but pending post that aren't transacting
                        List<VideoModel> watchedVideos = new Select()
                                .from(VideoModel.class)
                                .where(ActiveRecordFields.C_VID_CONV_ID + "=? AND " + ActiveRecordFields.C_VID_WATCHED_STATE + "=? AND " + ActiveRecordFields.C_VID_TRANSACTING + "=?",
                                        existingConversation.getConversationId(), VideoModel.ResourceState.WATCHED_PENDING_POST, 0).execute();

                        // unread count:
                        int unreadCount = newConversation.getUnreadCount() - ((watchedVideos != null && !watchedVideos.isEmpty()) ? watchedVideos.size() : 0);

                        if (newConversation.getLastMessageAtInMillis() <= existingConversation.getLastMessageAtInMillis()) {

                            Log.d(TAG, "SKIPPING:\n" + newConversation.toString());

                            Log.w(TAG, "old unread: " + existingConversation.getUnreadCount());
                            existingConversation.setUnreadCount(unreadCount);
                            existingConversation.save();
                            Log.w(TAG, "new unread: " + existingConversation.getUnreadCount());
                            newConvoMap.remove(newConversation.getConversationId());

                        } else {
                            // NOTE: save happens later for new convos
                            Log.d(TAG, "DELETING:\n" + existingConversation.toString());

                            Log.w(TAG, "old unread: " + newConversation.getUnreadCount());
                            newConversation.setUnreadCount(unreadCount);
                            Log.w(TAG, "new unread: " + newConversation.getUnreadCount());

                            // some cleanup, delete existing convo and also it's most recent thumb...
                            String recentThumbUri = existingConversation.getMostRecentThumbUrl();
                            deleteLocalThumb(recentThumbUri);

                            //delete the convo
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
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        Intent intent = new Intent(IABIntent.NOTIFY_SYNC);
        intent.putExtra(IABIntent.NOTIFY_SYNC, true);
        IABroadcastManager.sendLocalBroadcast(intent);
        Log.d("performance", "time to insert to db: " + (System.currentTimeMillis() - start));

        return true;
    }

    private void deleteLocalThumb(String recentThumbUri) {
        // delete the local thumb, no need to keep it
        File f = null;
        if (recentThumbUri != null && recentThumbUri.contains("file:///")) {
            try {
                f = new File(URI.create(recentThumbUri));
            } catch (Exception e) { // extra caution
                f = null;
                e.printStackTrace();
            }
        }

        if (f != null) {
            Log.d(TAG, "deleted png");
            f.delete();
        }
    }

    private void updateVideoModel() {

    }

    /**
     *  
     * @param intent
     * @return true if launched, false otherwise
     */
    private boolean launchDownloadService(Intent intent) {

        if (HollerbackApplication.getInstance().getAppLifecycle().isIdle()) {

            Log.d(TAG, "app is idle, lets launch the background downloader.");

            intent.setClass(this, BgDownloadService.class);
            startService(intent);
            return true;
            // the wakeful is delegated to the bgdownloadservice

        }

        Log.d(TAG, "app is not idle");
        return false;
    }

    @Override
    public String getFullyQualifiedClassName() {
        return SyncService.class.getName();
    }
}
