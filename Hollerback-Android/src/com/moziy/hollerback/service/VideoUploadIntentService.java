package com.moziy.hollerback.service;

import java.io.File;

import com.activeandroid.query.Select;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.video.S3UploadParams;
import com.moziy.hollerbacky.connection.HBRequestManager;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
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
		
		if(model == null){ //TODO - Sajjad: Remove from prod version
			throw new IllegalStateException("Attempting to upload video that does not exist!");
		}

		//the resource shouldn't be transacting
//		if(model.isTransacting()){	//TODO - Sajjad: Remove from prod version
//			throw new IllegalStateException("This resource shouldn't be transacting!");
//		}
		
		//if it's pending upload and not transacting 
		if(VideoModel.ResourceState.PENDING_UPLOAD.equals(model.getState()) && !model.isTransacting()){
			
			//awesome, let's try to upload this resource to s3
			model.setTransacting();
			model.save();
			
			//should we broadcast that we're uploading?
			
			File tmp = new File(FileUtil.getLocalFile(FileUtil.getImageUploadName(model.getLocalFileName())));
			String fileurl = Uri.fromFile(tmp).toString();
			
			PutObjectResult result = S3RequestHelper.uploadFileToS3(model.getLocalFileName(), fileurl);
			
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
			if(model.getConversationId() != null && "".equals(model.getConversationId())){
				
				postToExistingConversation(model);
				
			}else{
				
				postToNewConversation(model);
			}
			
			
		}
		
	}
	
	private boolean postToNewConversation(final VideoModel model){
		//TODO - Fill In
		//HBRequestManager.post
		
		return true;
	}
	
	private boolean postToExistingConversation(final VideoModel model){
		//TODO - Fill In
		
		return true;
	}
	
	

}
