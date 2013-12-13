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
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.PostToConvoResponse;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.util.AppEnvironment;
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
        }
    }

    public static class UploadUtility {

        private static final String TAG = UploadUtility.class.getSimpleName();
        private boolean mRecoverOnFailure;

        public UploadUtility() {
            mRecoverOnFailure = true;
        }

        public UploadUtility(boolean recover) {
            mRecoverOnFailure = recover;
        }

        public void uploadResource(VideoModel videoModel, int partNumber, int totalParts) {

            if (!videoModel.isTransacting()) {
                throw new IllegalStateException("Model must be transacting!");
            }

            if (partNumber == -1 || totalParts == -1) {
                throw new IllegalStateException("can't upload without having proper part information! part: " + partNumber + " totalParts: " + totalParts);
            }

            // awesome, let's try to upload this resource to s3
            videoModel.setNumParts(totalParts); // update the total number of parts for this resource
            videoModel.save();

            // should we broadcast that we're uploading?
            StringBuilder sb = new StringBuilder(128).append(videoModel.getSegmentFileName()).append(".").append(partNumber).append(".").append(videoModel.getSegmentFileExtension());
            String resourceName = sb.toString();
            Log.d(TAG, "attempting to upload: " + resourceName);

            // upload to S3
            PutObjectResult result = S3RequestHelper.uploadFileToS3(resourceName, HBFileUtil.getLocalFile(resourceName));

            if (result != null) { // => Yay, we uploaded the file to s3, lets see if all parts have been uploaded

                videoModel.setPartUploadState(partNumber, true); // successfully uploaded

                if (videoModel.isUploadSuccessfull()) { // uploading resource was successfull
                    Log.d(TAG, "resource parts were successfully uploaded");

                    // check to see if all parts have been uploaded successfully
                    videoModel.setState(VideoModel.ResourceState.UPLOADED_PENDING_POST);
                }
            } else {
                // broadcast failure
                videoModel.setPartUploadState(partNumber, false); // couldn't upload
                if (videoModel.getConversationId() < 0) {
                    Log.w(TAG, "upload failed");
                    // broadcast conversation create failure
                    IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.VIDEO_UPLOAD_FAILED));

                    if (mRecoverOnFailure)
                        ResourceRecoveryUtil.schedule(); // schedule for the failed video to get uploaded
                }
            }

            videoModel.save(); // ok, let's save the state :-)
        }

        private boolean postToNewConversation(final VideoModel videoModel) {

            if (!videoModel.isTransacting()) {
                throw new IllegalStateException("Model must be transacting");
            }

            // presumption that all the files have been uploaded
            Log.d(TAG, "recipients: " + videoModel.getRecipients()[0]);
            List<String> contacts = Arrays.asList(videoModel.getRecipients());

            final boolean[] isDone = {
                false
            };

            HBRequestManager.createNewConversation(contacts, new HBSyncHttpResponseHandler<Envelope<ConversationModel>>(new TypeReference<Envelope<ConversationModel>>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<ConversationModel> response) {

                    // let's check the thread it
                    Log.d(TAG, "thread id: " + Thread.currentThread().getId());

                    // nice it succeeded, lets update the model
                    videoModel.setState(VideoModel.ResourceState.PENDING_UPLOAD);

                    ConversationModel conversationResp = response.getData();

                    // lets bind the video to the conversation
                    videoModel.setConversationId(conversationResp.getConversationId());
                    videoModel.save();

                    // if the conversation we created, is actually found in our db, then update it
                    ConversationModel dbConvo = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", conversationResp.getConversationId()).executeSingle();
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
                    Log.d(TAG, "creating new conversation succeeded: " + conversationResp.getConversationId());

                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    Log.d(TAG, "onApiFailure");
                    // ok we're no longer transacting so let's clear it, but lets not update the state
                    videoModel.save();

                    Log.d(TAG, "creating new conversation failed: " + ((metaData != null) ? ("status code: " + metaData.code) : ""));
                    IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_CREATE_FAILURE));

                }

                @Override
                public void onPostResponse() {
                    super.onPostResponse();
                    VideoHelper.clearVideoTransacting(videoModel);
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

            Log.d(TAG, "done");

            return true;
        }

        public boolean postToExistingConversation(final VideoModel videoModel) {

            final boolean[] isDone = {
                false
            };

            final List<VideoModel> watchedVideos = VideoHelper.getVideosForTransaction(ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.WATCHED_PENDING_POST + "'");

            // get all the ids corresponding to the videos
            ArrayList<String> watchedIds = VideoHelper.getWatchedIds(watchedVideos);

            ArrayList<String> partUrls = new ArrayList<String>();
            for (int i = 0; i < videoModel.getNumParts(); i++) {
                partUrls.add(new StringBuilder(128).append(AppEnvironment.getInstance().UPLOAD_BUCKET) // tmp-bucket
                        .append("/") //
                        .append(videoModel.getSegmentFileName()) // CD/afejkd-gmmjdueh-qqeermvj
                        .append(".") //
                        .append(i) // 0
                        .append(".") //
                        .append(videoModel.getSegmentFileExtension()) // mp4
                        .toString()); //

            }

            int convoId = (int) videoModel.getConversationId();
            HBRequestManager.postToConversation(convoId, videoModel.getGuid(), partUrls, watchedIds, new HBSyncHttpResponseHandler<Envelope<PostToConvoResponse>>(
                    new TypeReference<Envelope<PostToConvoResponse>>() {
                    }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<PostToConvoResponse> response) {
                    PostToConvoResponse postResponse = response.getData();
                    Log.d(TAG, "posted to conversation: " + postResponse.conversation_id);

                    // update the video such that it's state is uploaded
                    videoModel.setState(ResourceState.UPLOADED);
                    videoModel.save();

                    // update the videos as watched
                    VideoHelper.markVideosAsWatched(watchedVideos);
                    VideoHelper.clearVideoTransacting(watchedVideos); // unset transacting flag of watched videos

                    isDone[0] = true;

                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    Log.d(TAG, "post to conversation failed");

                    VideoHelper.clearVideoTransacting(watchedVideos);

                    IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.VIDEO_UPLOAD_FAILED)); // broadcast failure of posting conversation

                    if (mRecoverOnFailure)
                        ResourceRecoveryUtil.schedule();

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
            Log.d(TAG, "done");
            return true;
        }

        // TODO - Food for thought..move these methods into a utility class?
        public List<VideoModel> getWatchedVideos() {

            List<VideoModel> watchedVideos = new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.WATCHED_PENDING_POST + "'").execute();
            return watchedVideos;
        }

    }

}
