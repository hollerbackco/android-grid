package com.moziy.hollerback.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.fasterxml.jackson.core.type.TypeReference;
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

    public SyncService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // TODO: add logic for retrying
        int retryCount = intent.getIntExtra(INTENT_ARG_RETRY_COUNT, 0);

        sync();

    }

    private void sync() {
        String lastSynctime = PreferenceManagerUtil.getPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, null);
        HBRequestManager.sync(lastSynctime, new HBSyncHttpResponseHandler<Envelope<ArrayList<SyncResponse>>>(new TypeReference<Envelope<ArrayList<SyncResponse>>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<ArrayList<SyncResponse>> response) {

                Log.d(TAG, "sync response succeeded: " + response.meta.last_sync_at);

                // lets save the sync time
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_SERVICE_SYNC_TIME, response.meta.last_sync_at);
                updateModel(response.data);

            }

            @Override
            public void onApiFailure(Metadata metaData) {
                if (metaData != null) {
                    Log.w(TAG, "metaData code: " + metaData.code);
                }

                // TODO - Sajjad: Implement retry logic here

            }

        });

    }

    private void updateModel(ArrayList<SyncResponse> data) {

        // if updating fails for any reason, then clear the preference that saves the last sync time
        ActiveAndroid.beginTransaction();
        // 1. for all the conversations, get the id of all conversations

        // 2. search for all the ids in the database

        // 3. remove them/update them

        // 4. insert the rest into the database

        // 5. for all the videoss, get the id of all of them

        // 6. search for all the ids in the database

        // 7. remove/update the videos

        // 8. insert the rest
        ActiveAndroid.endTransaction();

    }

}
