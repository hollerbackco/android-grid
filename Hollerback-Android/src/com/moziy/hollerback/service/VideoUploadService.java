package com.moziy.hollerback.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.util.Log;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class VideoUploadService extends Service {
    private String TAG = "UploadPhotoService";

    private ExecutorService mPool = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        IABroadcastManager.registerForLocalBroadcast(receiver, IABIntent.INTENT_UPLOAD_VIDEO_UPLOADING);
        mPool = Executors.newFixedThreadPool(2);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // Build the widget update for today

    }

    @Override
    public void onDestroy() {
        IABroadcastManager.unregisterLocalReceiver(receiver);
        mPool.shutdown();
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IABIntent.isIntent(intent, IABIntent.INTENT_UPLOAD_VIDEO_UPLOADING)) {
                if (intent != null) {
                    Log.e("uploading", "got it");
                    JSONObject tmp = new JSONObject();
                    if (intent.hasExtra("JSONCache") && !intent.getStringExtra("JSONCache").equalsIgnoreCase("")) {
                        try {
                            tmp = new JSONObject(intent.getStringExtra("JSONCache"));
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    final UploadTask uploadTask = new UploadTask(VideoUploadService.this, intent.getStringExtra("FileDataName"), intent.getStringExtra("ConversationId"), tmp,
                            intent.getStringExtra("ImageUploadName"));

                    mPool.execute(new Runnable() {
                        public void run() {
                            uploadTask.execute();
                        }
                    });

                    /*
                     * mFileDataName = intent.getStringExtra("FileDataName"); mConversationId = intent.getStringExtra("ConversationId");
                     * 
                     * showUploadingNotification(); mS3RequestHelper.uploadNewVideo(intent.getStringExtra("ConversationId"), intent.getStringExtra("FileDataName"),
                     * intent.getStringExtra("ImageUploadName"), null, mOnS3UploadListener);
                     */
                }
            }
        }
    };
}
