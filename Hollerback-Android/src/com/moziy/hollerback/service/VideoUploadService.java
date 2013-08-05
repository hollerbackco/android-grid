package com.moziy.hollerback.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.util.QU;
import com.moziy.hollerback.util.UploadCacheUtil;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnS3UploadListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class VideoUploadService extends Service{
	private String TAG = "UploadPhotoService";
    private NotificationManager mNM;
        
	public static String ACTION_UPLOAD = "ACTION_UPLOAD";

    public static int UPLOADINGNOTIFICATION = 10001;
    public static int FINISHUPLOADINGNOTIFICATION = 10002;
	
    /**
     * Uploading portion, parameters
     */
    S3RequestHelper mS3RequestHelper;
    
    String mFileDataName;
    String mConversationId;
    JSONObject mCachedData;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);
        
        Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(true)
        .setSmallIcon(android.R.drawable.stat_sys_upload)
        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
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
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);
        
        
        Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(false)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
        		R.drawable.icon))
        .build();
        		
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(FINISHUPLOADINGNOTIFICATION, notification);
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);
        
        Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle(title)
        .setContentText(desc)
        .setOngoing(false)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
        		R.drawable.icon))
        .build();
        		
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(FINISHUPLOADINGNOTIFICATION, notification);
    }

    
    @Override
    public void onStart(Intent intent, int startId) {
        // Build the widget update for today
		mS3RequestHelper = new S3RequestHelper();
    	if(intent != null)
    	{
    	    mFileDataName = intent.getStringExtra("FileDataName");
    	    mConversationId = intent.getStringExtra("ConversationId");
    	    if(intent.hasExtra("JSONCache") && !intent.getStringExtra("JSONCache").equalsIgnoreCase(""))
    	    {
    	    	try {
					mCachedData = new JSONObject(intent.getStringExtra("JSONCache"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
	        showUploadingNotification();
			mS3RequestHelper.uploadNewVideo(intent.getStringExtra("ConversationId"),
					intent.getStringExtra("FileDataName"),
					intent.getStringExtra("ImageUploadName"), null,
					mOnS3UploadListener);
    	}
	}
    
    @Override
    public void onDestroy() {
    	mNM.cancel(UPLOADINGNOTIFICATION);
    	super.onDestroy();
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
			UploadCacheUtil.removeUploadedCache(VideoUploadService.this, mConversationId, mCachedData);
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
