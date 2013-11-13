package com.moziy.hollerback.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.PostToConvoResponse;
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

        // lets lookup the id passed in from our intent arguments
        final VideoModel model = new Select().from(VideoModel.class).where("Id = ?", resourceId).executeSingle();

        if (model == null) { // TODO - Sajjad: Remove from prod version
            throw new IllegalStateException("Attempting to upload video that does not exist!");
        }

        // if the model has no conversation id associated with it, then post new conversation
        final int partNumber = intent.getIntExtra(INTENT_ARG_PART, -1);
        final int totalParts = intent.getIntExtra(INTENT_ARG_TOTAL_PARTS, -1);

        if (model.getConversationId() < 0) { // there is no conversation, so lets post this conversation

            postToNewConversation(model);
            Log.d(TAG, "returning..");

        } else { // a conversation exists

            // if it's pending upload and not transacting
            if (VideoModel.ResourceState.PENDING_UPLOAD.equals(model.getState()) && !model.isTransacting()) {
                // lets get the part info

                uploadResource(model, partNumber, totalParts);
            }

            // now if the model state is pending post and it's not transacting, then let's go ahead and post
            if (VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(model.getState()) && !model.isTransacting()) {
                // broadcast that we're posting?
                // a conversation must definitely be present
                // extract the needed information, such as the watched ids
                final ArrayList<String> watchedIds = (ArrayList<String>) intent.getStringArrayListExtra(INTENT_ARG_WATCHED_IDS); // TODO: store this in another table?
                postToExistingConversation(model, watchedIds);

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

    private boolean postToNewConversation(final VideoModel model) {

        // presumption that all the files have been uploaded
        Log.d(TAG, "recipients: " + model.getRecipients()[0]);
        List<String> contacts = Arrays.asList(model.getRecipients());
        // mark the model as transacting
        model.setTransacting();
        model.save();

        // ArrayList<String> parts = new ArrayList<String>();
        //
        // for (int i = 0; i < model.getNumParts(); i++) {
        // StringBuilder sb = new StringBuilder(128);
        // sb.append(AppEnvironment.getInstance().UPLOAD_BUCKET).append("/").append(model.getSegmentFileName()).append(".").append(i).append(".").append(model.getSegmentFileExtension());
        // parts.add(sb.toString());
        // }

        HBRequestManager.postConversations(contacts, new HBSyncHttpResponseHandler<Envelope<ConversationModel>>(new TypeReference<Envelope<ConversationModel>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<ConversationModel> response) {

                // let's check the thread it
                Log.d(TAG, "thread id: " + Thread.currentThread().getId());

                // nice it succeeded, lets update the model
                model.setState(VideoModel.ResourceState.PENDING_UPLOAD);
                model.clearTransacting();

                ConversationModel conversationResp = response.getData();

                // lets bind the video to the conversation
                model.setConversationId(conversationResp.getConversation_Id());
                model.save();

                // if the conversation we created, is actually found in our db, then update it
                ConversationModel dbConvo = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", conversationResp.getConversation_Id()).executeSingle();
                if (dbConvo != null) {
                    Log.d(TAG, "deleting record: " + dbConvo.toString());
                    // delete record
                    dbConvo.delete();
                }

                // inserting
                Log.d(TAG, "inserting: " + conversationResp.toString());
                conversationResp.save();

                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_CREATED));

                // launch the

                // lets create a new conversation from the response
                Log.d(TAG, "creating new conversation succeeded: " + conversationResp.getConversation_Id());
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

    private boolean postToExistingConversation(final VideoModel model, ArrayList<String> watchedIds) {

        ArrayList<String> partUrls = new ArrayList<String>();
        for (int i = 0; i < model.getNumParts(); i++) {
            partUrls.add(new StringBuilder(128).append(AppEnvironment.getInstance().UPLOAD_BUCKET) // tmp-bucket
                    .append("/") //
                    .append(model.getSegmentFileName()) // CD/afejkd-gmmjdueh-qqeermvj
                    .append(".") //
                    .append(i) // 0
                    .append(".") //
                    .append(model.getSegmentFileExtension()) // mp4
                    .toString()); //

        }

        int convoId = (int) model.getConversationId();
        HBRequestManager.postToConversation(convoId, model.getGuid(), partUrls, watchedIds, new HBSyncHttpResponseHandler<Envelope<PostToConvoResponse>>(
                new TypeReference<Envelope<PostToConvoResponse>>() {
                }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<PostToConvoResponse> response) {
                PostToConvoResponse postResponse = response.getData();
                Log.d(TAG, "posted to conversation: " + postResponse.conversation_id);

                // update model with the post repsonse

            }

            @Override
            public void onApiFailure(Metadata metaData) {
                Log.d(TAG, "post to conversation failed");

                // broadcast failure of posting conversation

            }
        });

        return true;
    }
}
