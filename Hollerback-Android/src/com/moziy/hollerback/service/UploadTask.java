package com.moziy.hollerback.service;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.util.UploadCacheUtil;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnS3UploadListener;

public class UploadTask {
    private NotificationManager mNM;
    private Context mContext;
    
	public static String ACTION_UPLOAD = "ACTION_UPLOAD";

    public static int UPLOADINGNOTIFICATION = 10001;
	
    
    S3RequestHelper mS3RequestHelper;
    String mFileDataName;
    String mConversationId;
    String mImageUploadName;
    JSONObject mCachedData;
    
    public UploadTask(Context context, String filename, String conversationId, JSONObject cache, String imageuploadName)
    {
    	mContext = context;
        mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mS3RequestHelper = new S3RequestHelper();
		mFileDataName = filename;
		mConversationId = conversationId;
		mCachedData = cache;
		mImageUploadName = imageuploadName;
    }
    
    public void execute()
    {
    	showUploadingNotification();
		mS3RequestHelper.uploadNewVideo(mConversationId,
				mFileDataName,
				mImageUploadName, null,
				mOnS3UploadListener);
    }
    
    /**
     * Show a notification while this service is running.
     */
    private void showUploadingNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence title = "Uploading";
        CharSequence desc = "Uploading Video";
        // Set the icon, scrolling text and timestamp

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, NotifyingController.class), 0);
        
        Notification notification = new NotificationCompat.Builder(mContext)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(true)
        .setSmallIcon(android.R.drawable.stat_sys_upload)
        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
        		R.drawable.icon))
        .build();
        
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(UPLOADINGNOTIFICATION, notification);
    }
    
    
    /**
     * Show a notification while this service is running.
     */
    private void showFinishUploadingNotification() {
    	mNM.cancel(UPLOADINGNOTIFICATION);

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence title = "Finished Uploading";
        CharSequence desc = "Your Video was uploaded successfully";
        
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, NotifyingController.class), 0);
        
        
        Notification notification = new NotificationCompat.Builder(mContext)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(false)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
        		R.drawable.icon))
        .build();
        		
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(UPLOADINGNOTIFICATION, notification);
    }
    
    /**
     * Show a notification while this service is running.
     */
    private void showFailUploadingNotification() {
    	mNM.cancel(UPLOADINGNOTIFICATION);
    	CharSequence title = "Error Uploading";
    	
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence desc = "There was a problem uploading the photo, please try again later";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, NotifyingController.class), 0);
        
        Notification notification = new NotificationCompat.Builder(mContext)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(false)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
        		R.drawable.icon))
        .build();
        		
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(UPLOADINGNOTIFICATION, notification);
    }
    
    
	OnS3UploadListener mOnS3UploadListener = new OnS3UploadListener() {

		@Override
		public void onStart() {
			// dialog.show();
			// Toast.makeText(HollerbackCameraActivity.this, "Upload Started",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public int onProgress(long progress) {
			// TODO Auto-generated method stub

			// have the loading spinner execute htere
			return 0;
		}

		@Override
		public int onComplete() {
			// TODO Auto-generated method stub

			LogUtil.i("oncomplete called");
			UploadCacheUtil.removeUploadedCache(mContext, mConversationId, mCachedData);
			showFinishUploadingNotification();
			return 0;
		}

		@Override
		public void onS3Upload(boolean success) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onS3Url(String url, boolean success) {
			// TODO Auto-generated method stub

		}
	};	
}
