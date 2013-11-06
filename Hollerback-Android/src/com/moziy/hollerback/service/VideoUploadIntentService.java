package com.moziy.hollerback.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.NewConvoResponse;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.HBSyncHttpResponseHandler;

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

        // get the arguments
        ArrayList<String> contacts = (ArrayList<String>) intent.getStringArrayListExtra(INTENT_ARG_CONTACTS);

        Log.d(TAG, "resource id: " + resourceId + " contact(s): " + contacts.get(0).toString());

        // lets lookup the id passed in from our intent arguments
        VideoModel model = new Select().from(VideoModel.class).where("Id = ?", resourceId).executeSingle();

        if (model == null) { // TODO - Sajjad: Remove from prod version
            throw new IllegalStateException("Attempting to upload video that does not exist!");
        }

        // if it's pending upload and not transacting
        if (VideoModel.ResourceState.PENDING_UPLOAD.equals(model.getState()) && !model.isTransacting()) {
            // lets get the part info
            int partNumber = intent.getIntExtra(INTENT_ARG_PART, -1);
            int totalParts = intent.getIntExtra(INTENT_ARG_TOTAL_PARTS, -1);

            uploadResource(model, partNumber, totalParts);
        }

        // now if the model state is pending post and it's not transacting, then let's go ahead and post
        if (VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(model.getState()) && !model.isTransacting()) {
            // broadcast that we're posting?

            // lets figure out what type of posting we've got to do, new convo or existing
            if (model.getConversationId() > 0) {
                // extract the needed information, such as the watched ids
                ArrayList<String> watchedIds = (ArrayList<String>) intent.getStringArrayListExtra(INTENT_ARG_WATCHED_IDS);
                postToExistingConversation(model);

            } else {

                postToNewConversation(model, contacts);
            }
        }
    }

    private void uploadResource(VideoModel model, int partNumber, int totalParts) {

        if (partNumber == -1 || totalParts == -1) {
            throw new IllegalStateException("can't upload without having proper part information! part: " + partNumber + " totalParts: " + totalParts);
        }

        // awesome, let's try to upload this resource to s3
        model.setTransacting();
        model.setNumParts(totalParts); // update the total number of parts for this resource
        model.save();

        // should we broadcast that we're uploading?
        StringBuilder sb = new StringBuilder(128).append(model.getSegmentFileName()).append(".").append(partNumber).append(".").append(model.getSegmentFileExtension());
        String resourceName = sb.toString();
        Log.d(TAG, "attempting to upload: " + resourceName);

        // upload to S3
        PutObjectResult result = S3RequestHelper.uploadFileToS3(resourceName, HBFileUtil.getLocalFile(resourceName));

        if (result != null) { // => Yay, we uploaded the file to s3, lets see if all parts have been uploaded

            model.setPartUploadState(partNumber, true); // successfully uploaded

            if (model.isUploadSuccessfull()) { // uploading resource was successfull
                Log.d(TAG, "resource parts were successfully uploaded");

                // check to see if all parts have been uploaded successfully
                model.setState(VideoModel.ResourceState.UPLOADED_PENDING_POST);
            }
        } else {
            // broadcast failure
            model.setPartUploadState(partNumber, false); // couldn't upload
            if (model.getConversationId() < 0) {
                Log.w(TAG, "upload failed");
                // broadcast conversation create failure
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_CREATE_FAILURE));
            }
        }

        model.clearTransacting(); // ok, we're no longer transacting
        model.save(); // ok, let's save the state :-)
    }

    private boolean postToNewConversation(final VideoModel model, final ArrayList<String> contacts) {

        // presumption that all the files have been uploaded

        // mark the model as transacting
        model.setTransacting();
        model.save();

        ArrayList<String> parts = new ArrayList<String>();

        for (int i = 0; i < model.getNumParts(); i++) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(AppEnvironment.getInstance().UPLOAD_BUCKET).append("/").append(model.getSegmentFileName()).append(".").append(i).append(".").append(model.getSegmentFileExtension());
            parts.add(sb.toString());
        }

        HBRequestManager.postConversations(contacts, parts, new HBSyncHttpResponseHandler<Envelope<NewConvoResponse>>(new TypeReference<Envelope<NewConvoResponse>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<NewConvoResponse> response) {

                // let's check the thread it
                Log.d(TAG, "thread id: " + Thread.currentThread().getId());

                // nice it succeeded, lets update the model
                model.setState(VideoModel.ResourceState.UPLOADED);
                model.clearTransacting();

                NewConvoResponse conversationResp = response.getData();

                // lets bind the video to the conversation
                model.setConversationId(conversationResp.id);
                model.save();

                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_CREATED));

                // lets create a new conversation from the response
                Log.d(TAG, "creating new conversation succeeded: " + conversationResp.id);
            }

            @Override
            public void onApiFailure(Metadata metaData) {
                Log.d(TAG, "onApiFailure");
                // ok we're no longer transacting so let's clear it, but lets not update the state
                model.clearTransacting();
                model.save();

                Log.d(TAG, "creating new conversation failed: " + ((metaData != null) ? ("status code: " + metaData.code) : ""));
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_CREATE_FAILURE));

            }

        });

        return true;
    }

    private boolean postToExistingConversation(final VideoModel model) {
        // TODO - Fill In

        return true;
    }

}
