package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.VideoUploadIntentService.UploadUtility;
import com.moziy.hollerback.util.ResourceRecoveryUtil;

public class ResourceRecoveryService extends IntentService {
    private static final String TAG = ResourceRecoveryService.class.getSimpleName();

    public ResourceRecoveryService() {
        super(ResourceRecoveryService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        UploadUtility util = new UploadUtility();

        StringBuilder sb = new StringBuilder();
        sb.append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.UPLOADED_PENDING_POST).append("'").append(" OR ").append(ActiveRecordFields.C_VID_STATE).append("='")
                .append(VideoModel.ResourceState.PENDING_UPLOAD).append("'");

        // select all resources that are pending upload or uploaded and pending post
        List<VideoModel> pendingList = new Select().from(VideoModel.class).where(sb.toString()).execute();

        try {
            if (pendingList == null || pendingList.isEmpty()) {
                Log.d(TAG, "no pending videos/messages to upload, yay");
                // cancel any recurring alarm
                ResourceRecoveryUtil.cancel();
                return;
            }

            Log.d(TAG, "attempting to upload previously pending resources");
            for (VideoModel v : pendingList) {
                if (v.isTransacting())
                    continue;

                if (VideoModel.ResourceState.PENDING_UPLOAD.equals(v.getState())) {
                    int totalParts = v.getNumParts();
                    // for each part lets upload the resource
                    for (int i = 0; i < totalParts; i++) {
                        util.uploadResource(v, i, totalParts);
                    }
                }

                // now if the model state is pending post and it's not transacting, then let's go ahead and post
                if (VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(v.getState())) {

                    util.postToExistingConversation(v);

                }

            }
        } finally {
            ResourceRecoveryUtil.completeWakefulIntent(intent);
        }

    }
}
