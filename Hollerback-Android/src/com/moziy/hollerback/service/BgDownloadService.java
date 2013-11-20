package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.gcm.GCMBroadcastReceiver;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskGroup;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.util.NotificationUtil;

public class BgDownloadService extends IntentService {
    private static final String TAG = BgDownloadService.class.getSimpleName();
    public static final String RETRY_COUNT_INTENT_ARG_KEY = "RETRY_COUNT";
    public static final String RELEASE_WAKE_LOCK_INTENT_ARG_KEY = "RELEASE_WAKE_LOCK";

    private static final int DEFAULT_RETRY_COUNT = 2;

    public BgDownloadService() {
        super(BgDownloadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // TODO - sajjad: Add download of thumbs!
        Log.d(TAG, "bg download service launched!");
        // query the videos
        List<VideoModel> videos = getVideos();

        if (videos.isEmpty()) {
            Log.w(TAG, TAG + " was initiated but no videos were found.");
            return;
        }

        // download them
        boolean success = downloadVideos(videos);

        if (success) { // lets notify the user that new videos have been downloaded

            String message = NotificationUtil.generateNewVideoMessage(this, videos);
            NotificationUtil.launchNotification(this, NotificationUtil.generateNotification(this, getString(R.string.app_name), message), NotificationUtil.Ids.SYNC_NOTIFICATION);

        }

        // lets release the wakelock if any
        releaseWakeLock(intent);

    }

    private void releaseWakeLock(Intent intent) {
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public List<VideoModel> getVideos() {
        // search for all model whose videos are pending download
        List<VideoModel> videos = new Select().from(VideoModel.class) //
                .where(ActiveRecordFields.C_VID_STATE + "=? AND " + //
                        ActiveRecordFields.C_VID_WATCHED_STATE + "=? AND " + //
                        ActiveRecordFields.C_VID_TRANSACTING + "=?", //
                        VideoModel.ResourceState.PENDING_DOWNLOAD, VideoModel.ResourceState.UNWATCHED, 0).execute(); //
        return videos;
    }

    public boolean downloadVideos(List<VideoModel> videos) {

        TaskGroup downloadTasks = new TaskGroup();

        // for each video, lets create a download task and add it to the task group
        for (VideoModel video : videos) {
            VideoDownloadTask t = new VideoDownloadTask(video);
            t.setTaskListener(new Task.Listener() {

                @Override
                public void onTaskError(Task t) {
                    Log.w(TAG, "video with id: " + ((VideoDownloadTask) t).getVideoId() + " failed to download");
                    // notify of failure in case anyone is listening
                    Intent intent = new Intent(IABIntent.VIDEO_DOWNLOAD_FAILED);
                    intent.putExtra(IABIntent.PARAM_ID, ((VideoDownloadTask) t).getVideoId());
                    IABroadcastManager.sendLocalBroadcast(intent);
                }

                @Override
                public void onTaskComplete(Task t) {
                    Log.d(TAG, "video with id: " + ((VideoDownloadTask) t).getVideoId() + " downloaded.");
                    Intent intent = new Intent(IABIntent.VIDEO_DOWNLOADED);
                    intent.putExtra(IABIntent.PARAM_ID, ((VideoDownloadTask) t).getVideoId());
                    IABroadcastManager.sendLocalBroadcast(intent);
                }
            });
            downloadTasks.addTask(t);
        }

        // lets run the task group
        downloadTasks.run();

        boolean taskGroupStatus = downloadTasks.isSuccess();

        // if it failed, lets try again
        if (!taskGroupStatus) {
            Log.d(TAG, "some tasks failed.");
            int retrycount = 0;

            do {
                downloadTasks = new TaskGroup(downloadTasks.getFailedTasks());
                downloadTasks.run();
                taskGroupStatus = downloadTasks.isSuccess();
                ++retrycount;
            } while (retrycount < DEFAULT_RETRY_COUNT && !taskGroupStatus);

        }

        // ok, lets see the overall status
        Log.d(TAG, "overall status: " + taskGroupStatus);

        // TODO - sajjad: check to see if we need to shutdown the wakelock
        return taskGroupStatus;
    }
}
