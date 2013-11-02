package com.moziy.hollerback.service;

import com.activeandroid.query.Select;
import com.moziy.hollerback.model.VideoModel;

import android.app.IntentService;
import android.content.Intent;
/**
 * This class is responsible for uploading a video resource to S3, and then issuing a post to the appropriate api 
 * @author sajjad
 *
 */
public class VideoUploadIntentService extends IntentService{
	
	public static final String INTENT_ARG_RESOURCE_ID = "resource_id";
	
	public interface Type{
		public static final String NEW_CONVERSATION = "new_conversation";
		public static final String EXISTING_CONVERSATION = "existing_conversation";
	}

	public VideoUploadIntentService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		int resourceId = intent.getIntExtra(INTENT_ARG_RESOURCE_ID, -1);

		//lets lookup the id passed in from our intent arguments
		VideoModel model = new Select().from(VideoModel.class).where("id = ?", resourceId).executeSingle();
		
		if(model == null){
			throw new IllegalStateException("Attempting to upload video that does not exist!");
		}
		//lets check to see what's going on with it
		String resourceState = model.getState();
		boolean transacting = model.isTransacting();
		
		
		if(VideoModel.ResourceState.PENDING_UPLOAD.equals(resourceState)){
			
		}else if(VideoModel.ResourceState.UPLOADED_PENDING_POST.equals(resourceState)){
			
		}else{
			//nothing to do
		}
		
		
		
//		model.setFileName(fileName);
//		model.setState(VideoModel.ResourceState.PENDING_UPLOAD);
//		model.setTransacting();
//		
//		
//		if(Type.NEW_CONVERSATION.equals(type)){
//			
//			handlePostToNewConversation(intent, model);
//			
//		}else if(Type.EXISTING_CONVERSATION.equals(type)){
//			
//			handlePostToExistingConversation(intent, model);
//			
//		}else{
//			
//			model.clearTransacting();
//			model.save();
//			throw new IllegalStateException("DEV: Unknown upload type. Please specify a type as argument");
//		}
		
	}
	
	private boolean handlePostToNewConversation(Intent intent, VideoModel model){
		
		return true;
	}
	
	private boolean handlePostToExistingConversation(Intent intent, VideoModel model){
		
		
		return true;
	}
	
	

}
