package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.HBSyncHttpResponseHandler;

public class TTYLService extends IntentService {

    public static final String CONVO_ID_INTENT_ARG_KEY = "CONVO_ID";
    private static String WHERE = ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.WATCHED_PENDING_POST + "'";

    public TTYLService() {
        super(TTYLService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        long conversationId = intent.getLongExtra(CONVO_ID_INTENT_ARG_KEY, -1);

        if (conversationId == -1) {
            throw new IllegalStateException("Conversation Id not set!");
        }

        final boolean[] isDone = {
            false
        };

        // lets get all the videos that are pending to be watched
        List<VideoModel> videos = VideoHelper.getVideosForTransaction(WHERE);
        HBRequestManager.postTTYL(conversationId, VideoHelper.getWatchedIds(videos), new HBSyncHttpResponseHandler<Envelope<?>>(new TypeReference<Envelope<?>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<?> response) {

                isDone[0] = true;

            }

            @Override
            public void onApiFailure(Metadata metaData) {
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
