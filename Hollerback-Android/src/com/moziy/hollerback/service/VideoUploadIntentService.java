package com.moziy.hollerback.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.helper.UploadUtility;
import com.moziy.hollerback.service.helper.VideoHelper;
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

    public interface Type {
        public static final String NEW_CONVERSATION = "new_conversation";
        public static final String EXISTING_CONVERSATION = "existing_conversation";
    }

    public VideoUploadIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long resourceId = intent.getLongExtra(INTENT_ARG_RESOURCE_ID, -1);

        // lets lookup the id passed in from our intent arguments
        final VideoModel model = VideoHelper.getVideoForTransaction("Id = " + resourceId); // new Select().from(VideoModel.class).where("Id = ?", resourceId).executeSingle();

        UploadUtility uploadUtility = new UploadUtility();

        if (model == null) { // TODO - Sajjad: Remove from prod version
            throw new IllegalStateException("Attempting to upload video that does not exist!");
        }

        // if the model has no conversation id associated with it, then post new conversation
        final int partNumber = intent.getIntExtra(INTENT_ARG_PART, -1);
        final int totalParts = intent.getIntExtra(INTENT_ARG_TOTAL_PARTS, -1);

        if (model.getConversationId() < 0) { // there is no conversation, so lets post this conversation

            uploadUtility.postToNewConversation(model);
            Log.d(TAG, "returning..");

        } else { // a conversation exists

            if (!model.isTransacting()) {
                throw new IllegalStateException("Video must be transacting!");
            }
            // if it's pending upload and not transacting
            if (VideoModel.ResourceState.PENDING_UPLOAD.equals(model.getState())) {
                // lets get the part info

                // for each part lets upload the resource
                for (int i = 0; i < totalParts; i++) {
                    uploadUtility.uploadResource(model, i, totalParts);
                }
            }

            // now if the model state is pending post and it's not transacting, then let's go ahead and post
            if (VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(model.getState())) {

                uploadUtility.postToExistingConversation(model);

            }

            if (!model.isTransacting()) {
                throw new IllegalStateException("Video must be transacting");
            }

            // clear the model from transacting
            Log.d(TAG, "clearing transacting");
            VideoHelper.clearVideoTransacting(model);

            if (!VideoModel.ResourceState.UPLOADED.equals(model.getState())) { // if we're not in the uploading state, then request recovery
                Log.d(TAG, "requesting recovery");
                ResourceRecoveryUtil.requestRecovery(PassiveUploadService.getRecoveryClient());
            }
        }
    }

}
