package com.moziy.hollerback.service;

import java.io.File;
import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.query.Select;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.model.web.response.NewConvoResponse;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerbacky.connection.HBHttpResponseHandler;
import com.moziy.hollerbacky.connection.HBRequestManager;
/**
 * This class is responsible for uploading a video resource to S3, and then issuing a post to the appropriate api 
 * @author sajjad
 *
 */
public class VideoUploadIntentService extends IntentService{
	
	private static final String TAG = VideoUploadIntentService.class.getSimpleName();
	private final Handler mHandler = new Handler();
	
	//type: Long
	public static final String INTENT_ARG_RESOURCE_ID = "resource_id";
	
	//type: ArrayList<String>
	public static final String INTENT_ARG_CONTACTS = "contacts";	 
	
	public interface Type{
		public static final String NEW_CONVERSATION = "new_conversation";
		public static final String EXISTING_CONVERSATION = "existing_conversation";
	}

	public VideoUploadIntentService(){
		super(TAG);
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		long resourceId = intent.getLongExtra(INTENT_ARG_RESOURCE_ID, -1);
		ArrayList<String> contacts = (ArrayList<String>)intent.getStringArrayListExtra(INTENT_ARG_CONTACTS); 

		Log.d(TAG, "resource id: " + resourceId + " contact(s): " + contacts.get(0).toString());
		
		//lets lookup the id passed in from our intent arguments
		VideoModel model = new Select().from(VideoModel.class).where("Id = ?", resourceId).executeSingle();
		
		if(model == null){ //TODO - Sajjad: Remove from prod version
			throw new IllegalStateException("Attempting to upload video that does not exist!");
		}


		//if it's pending upload and not transacting 
		if(VideoModel.ResourceState.PENDING_UPLOAD.equals(model.getState()) && !model.isTransacting()){
			
			//awesome, let's try to upload this resource to s3
			model.setTransacting();
			model.save();
			
			//should we broadcast that we're uploading?
//			String fileurl = Uri.fromFile(FileUtil.getOutputVideoFile(model.getLocalFileName())).toString();
			
			PutObjectResult result = S3RequestHelper.uploadFileToS3(model.getLocalFileName(), FileUtil.getLocalFile(model.getLocalFileName()));
			
			if(result != null){ //=> Yay, we uploaded the file to s3, lets mark it as uploaded pending post!
				model.setState(VideoModel.ResourceState.UPLOADED_PENDING_POST);
			}
			
			model.clearTransacting();	//ok, we're no longer transacting
			model.save();				//ok, let's save the state :-)
			
		}
		
		//now if the model state is pending post and it's not transacting, then let's go ahead and post 
		if(VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(model.getState()) && !model.isTransacting()){
			//broadcast that we're posting?
			
			//lets figure out what type of posting we've got to do, new convo or existing
			if(model.getConversationId() > 0){
				
				postToExistingConversation(model);
				
				
			}else{
				
				postToNewConversation(model, contacts);
			}
		}
	}
	
	private boolean postToNewConversation(final VideoModel model, final ArrayList<String> contacts){
		
	
				HBRequestManager.postConversations(contacts, new HBHttpResponseHandler<Envelope<NewConvoResponse>>(new TypeReference<Envelope<NewConvoResponse>>() {
				}, true) {

					@Override
					public void onResponseSuccess(int statusCode, Envelope<NewConvoResponse> response) {
						
						//let's check the thread it
						Log.d(TAG, "thread id: " + Thread.currentThread().getId());
						
						//nice it succeeded, lets update the model
						model.setState(VideoModel.ResourceState.UPLOADED);
						model.clearTransacting();
						
						NewConvoResponse conversationResp = response.getData();
						
						//lets bind the video to the conversation
						model.setConversationId(conversationResp.id);
						model.save();
						
						//lets create a new conversation from the response
						Log.d(TAG, "creating new conversation succeeded: " + conversationResp.id);
					}

					@Override
					public void onApiFailure(Metadata metaData) {
						
						//ok we're no longer transacting so let's clear it, but lets not update the state
						model.clearTransacting();
						model.save();
						
						Log.d(TAG, "creating new conversation failed: " + ((metaData != null ) ? ("status code: " + metaData.code) : ""));
						
					}
					
				});
				
		
		
		return true;
	}
	
	private boolean postToExistingConversation(final VideoModel model){
		//TODO - Fill In
		
		return true;
	}
	
	

}
