package com.moziy.hollerback.service.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.ConversationModel.TimeStamp;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.PostToConvoResponse;
import com.moziy.hollerback.service.PassiveUploadService;
import com.moziy.hollerback.service.task.ConvoThumbTask;
import com.moziy.hollerback.service.task.GenerateVideoThumbTask;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil.RecoveryClient;

public class UploadUtility implements RecoveryClient {

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

            }
        }

        videoModel.save(); // ok, let's save the state :-)
    }

    public boolean postToNewConversation(final VideoModel videoModel, String title) {

        if (!videoModel.isTransacting()) {
            throw new IllegalStateException("Model must be transacting");
        }

        // presumption that all the files have been uploaded
        Log.d(TAG, "recipients: " + videoModel.getRecipients()[0]);
        List<String> contacts = Arrays.asList(videoModel.getRecipients());

        final boolean[] isDone = {
            false
        };

        final TimeStamp convoUpdateTime = ConversationModel.getConvoTimeStamp();

        HBRequestManager.createNewConversation(contacts, title, new HBSyncHttpResponseHandler<Envelope<ConversationModel>>(new TypeReference<Envelope<ConversationModel>>() {
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
                boolean isNew = true;
                if (dbConvo != null) {
                    conversationResp.setLastMessageAt(convoUpdateTime);
                    Log.d(TAG, "deleting record: " + dbConvo.toString());
                    // delete record
                    dbConvo.delete();
                    isNew = false;
                }

                // inserting
                Log.d(TAG, "inserting: " + conversationResp.toString());
                conversationResp.save();

                // generate the thumb
                ConvoThumbTask t = new ConvoThumbTask(conversationResp.getConversationId(), new GenerateVideoThumbTask(HBFileUtil.getLocalVideoFile(0, videoModel.getGuid(), "mp4"), HBFileUtil
                        .getLocalThumbFile(videoModel.getGuid())));
                t.run();

                // fire off conversation intent
                Intent intent = new Intent(IABIntent.CONVERSATION_CREATED);
                intent.putExtra(IABIntent.PARAM_IS_NEW_CONVERSATION, isNew);
                IABroadcastManager.sendLocalBroadcast(intent);

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

        try {
            HollerbackAppState.sSyncSemaphore.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            Log.w(TAG, "Interrupted and need to reschedule");

            return false;

        }

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

            }

            @Override
            public void onApiFailure(Metadata metaData) {
                Log.d(TAG, "post to conversation failed");

                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.VIDEO_UPLOAD_FAILED)); // broadcast failure of posting conversation

            }

            @Override
            public void onPostResponse() {
                super.onPostResponse();
                VideoHelper.clearVideoTransacting(watchedVideos); // unset transacting flag of watched videos
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

        HollerbackAppState.sSyncSemaphore.release(); // release the semaphore

        Log.d(TAG, "done");
        return true;
    }

    // TODO - Food for thought..move these methods into a utility class?
    public List<VideoModel> getWatchedVideos() {

        List<VideoModel> watchedVideos = new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.WATCHED_PENDING_POST + "'").execute();
        return watchedVideos;
    }

    @Override
    public String getFullyQualifiedClassName() {
        return PassiveUploadService.class.getName();
    }

}
