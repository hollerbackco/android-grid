package com.moziy.hollerback.service;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.service.helper.UploadUtility;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.service.task.GenerateVideoThumbTask;
import com.moziy.hollerback.util.AppSynchronization;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil;

/**
 * This class is responsible for uploading a video resource to S3, and then issuing a post to the appropriate api 
 * @author sajjad
 *
 */
public class VideoUploadIntentService extends IntentService {

    private static final String TAG = VideoUploadIntentService.class.getSimpleName();

    // type: Long
    public static final String INTENT_ARG_RESOURCE_ID = "resource_id";
    // type: ArrayList<String>
    public static final String INTENT_ARG_CONTACTS = "contacts";
    // type: ArrayList<String>
    public static final String INTENT_ARG_WATCHED_IDS = "watched_ids";
    // type: int
    public static final String INTENT_ARG_PART = "part_number"; // optional if the intent is to just post
    // type: int
    public static final String INTENT_ARG_TOTAL_PARTS = "total_parts";
    // type: long
    public static final String INTENT_ARG_TIMESTAMP = "timestamp";
    // type: string
    public static final String TITLE_INTENT_ARG_KEY = "title_arg";

    public interface Type {
        public static final String NEW_CONVERSATION = "new_conversation";
        public static final String EXISTING_CONVERSATION = "existing_conversation";
    }

    public VideoUploadIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            AppSynchronization.sVideoUploadSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            long resourceId = intent.getLongExtra(INTENT_ARG_RESOURCE_ID, -1);

            // lets lookup the id passed in from our intent arguments
            final VideoModel model = VideoHelper.getVideoForTransaction("Id = " + resourceId); // new Select().from(VideoModel.class).where("Id = ?", resourceId).executeSingle();

            UploadUtility uploadUtility = new UploadUtility();

            if (model.getConversationId() < 0) { // there is no conversation, so lets post this conversation

                String title = intent.getStringExtra(TITLE_INTENT_ARG_KEY);
                uploadUtility.postToNewConversation(model, title);
                Log.d(TAG, "returning..");

            } else { // a conversation exists

                // get all the previously pending videos for this conversation, and ensure that we send them off
                if (!model.isTransacting()) {
                    throw new IllegalStateException("Video must be transacting!");
                }

                // get all the videos that are waiting to be uploaded/pending upload for this conversation
                StringBuilder sb = new StringBuilder();
                sb.append("(").append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.UPLOADED_PENDING_POST).append("'").append(" OR ")
                        .append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.PENDING_UPLOAD).append("') AND ").append(ActiveRecordFields.C_CONV_ID).append("=")
                        .append(model.getConversationId());

                Log.d(TAG, sb.toString());

                List<VideoModel> mVideos = VideoHelper.getVideosForTransaction(sb.toString());

                if (mVideos == null) {
                    mVideos = new ArrayList<VideoModel>();
                }

                mVideos.add(model);

                boolean requestRecovery = false;

                for (VideoModel v : mVideos) {

                    if (v.getThumbUrl() == null) { // generate the thumb
                        GenerateVideoThumbTask t = new GenerateVideoThumbTask(HBFileUtil.getLocalVideoFile(0, v.getGuid(), "mp4"), HBFileUtil.getLocalThumbFile(v.getGuid()));
                        t.run();
                        v.setThumbUrl("file:///" + t.getDstPath());
                    }

                    // if it's pending upload and not transacting
                    if (VideoModel.ResourceState.PENDING_UPLOAD.equals(v.getState())) {

                        // lets get the part info
                        int totalParts = v.getNumParts();

                        // for each part lets upload the resource
                        for (int i = 0; i < totalParts; i++) {
                            uploadUtility.uploadResource(v, i, totalParts);
                        }
                    }

                    // now if the model state is pending post and it's not transacting, then let's go ahead and post
                    if (VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(v.getState())) {

                        uploadUtility.postToExistingConversation(v);

                    }

                    if (!ResourceState.UPLOADED.equals(v.getState())) {
                        requestRecovery = true;
                    }

                    if (!v.isTransacting()) {
                        throw new IllegalStateException("Video must be transacting");
                    }
                }

                // clear the model from transacting
                Log.d(TAG, "clearing transacting");
                VideoHelper.clearVideoTransacting(mVideos);

                if (requestRecovery) { // if we're not in the uploading state, then request recovery
                    Log.d(TAG, "requesting recovery");
                    ResourceRecoveryUtil.requestRecovery(PassiveUploadService.getRecoveryClient());
                }
            }
        } finally {
            AppSynchronization.sVideoUploadSemaphore.release();
        }
    }
}
