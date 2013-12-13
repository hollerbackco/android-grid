package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.helper.VideoHelper;

public class TTYLService extends IntentService {
    private static final String TAG = TTYLService.class.getSimpleName();
    public static final String CONVO_ID_INTENT_ARG_KEY = "CONVO_ID";
    private static String WHERE = ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.WATCHED_PENDING_POST + "'";

    public TTYLService() {
        super(TTYLService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        long conversationId = intent.getLongExtra(CONVO_ID_INTENT_ARG_KEY, -1);

        // lets get all the videos that are pending to be watched
        final List<VideoModel> videos = VideoHelper.getVideosForTransaction(WHERE);

        if (videos == null || videos.isEmpty()) { // no videos to post watched, foget abou it
            Log.d(TAG, "no videos pending");
            return;
        }

        // if one of the videos belongs to this conversation then post ttyl, otherwise, refrain
        boolean proceed = false;
        for (VideoModel v : videos) {
            if (v.getConversationId() == conversationId) {
                proceed = true;
                break;
            }

        }

        if (!proceed) {
            VideoHelper.clearVideoTransacting(videos);
            return;
        }

        final boolean[] isDone = {
            false
        };

        HBRequestManager.postTTYL(conversationId, VideoHelper.getWatchedIds(videos), new HBSyncHttpResponseHandler<Envelope<?>>(new TypeReference<Envelope<?>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<?> response) {

                VideoHelper.markVideosAsWatched(videos);

            }

            @Override
            public void onApiFailure(Metadata metaData) {

                Log.w(TAG, "ttyl failed");
            }

            @Override
            public void onPostResponse() {
                super.onPostResponse();
                Log.d(TAG, "clearing pending transacting videos");
                VideoHelper.clearVideoTransacting(videos);
                isDone[0] = true;
            }
        });

        while (!isDone[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
