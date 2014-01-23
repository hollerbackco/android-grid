package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.helper.UploadUtility;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.util.AppSynchronization;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil.RecoveryClient;

public class PassiveUploadService extends IntentService {
    private static final String TAG = PassiveUploadService.class.getSimpleName();

    private static RecoveryClient sRecoveryClient = new RecoveryClient() {

        @Override
        public String getFullyQualifiedClassName() {

            return PassiveUploadService.class.getName();
        }
    };

    public PassiveUploadService() {
        super(PassiveUploadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        UploadUtility util = new UploadUtility(false);

        StringBuilder sb = new StringBuilder();
        sb.append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.UPLOADED_PENDING_POST).append("'").append(" OR ").append(ActiveRecordFields.C_VID_STATE).append("='")
                .append(VideoModel.ResourceState.PENDING_UPLOAD).append("'");

        // select all resources that are pending upload or uploaded and pending post
        List<VideoModel> pendingList = new Select().from(VideoModel.class).where(sb.toString()).execute();

        try {
            AppSynchronization.sVideoUploadSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {

            if (pendingList == null || pendingList.isEmpty()) {
                Log.d(TAG, "no pending videos/messages to upload, yay");
                // cancel any recurring alarm
                // ResourceRecoveryUtil.cancel();
                ResourceRecoveryUtil.removeRecoveryRequest(sRecoveryClient);
                return;
            }

            boolean requestRecovery = false;
            // lets start a transaction, the method below ensures that we don't retrieve a list of transacting videos
            pendingList = VideoHelper.getVideosForTransaction(sb.toString());

            if (pendingList == null) {
                Log.w(TAG, "txn in process");
                return;
            }

            Log.d(TAG, "attempting to upload previously pending resources: " + pendingList.size());
            for (VideoModel v : pendingList) {
                if (!v.isTransacting())
                    throw new IllegalStateException("Video " + v.getGuid() + " must be transacting!");

                Log.d(TAG, "passive upload: " + v.getGuid());
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

                if (!v.isTransacting()) {
                    throw new IllegalStateException("Video must be transacting!");
                }

                // clear the model from transacting
                VideoHelper.clearVideoTransacting(v);

                if (!requestRecovery) {
                    if (!VideoModel.ResourceState.UPLOADED.equals(v.getState())) {
                        requestRecovery = true;
                    }
                }

            }

            if (requestRecovery) {
                startRecovery();
            }

        } finally {
            ResourceRecoveryUtil.completeWakefulIntent(intent);

            AppSynchronization.sVideoUploadSemaphore.release();
        }

    }

    public static RecoveryClient getRecoveryClient() {
        return sRecoveryClient;
    }

    private void startRecovery() {
        ResourceRecoveryUtil.requestRecovery(sRecoveryClient);
    }

}
