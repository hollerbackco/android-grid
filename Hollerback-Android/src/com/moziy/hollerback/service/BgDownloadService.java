package com.moziy.hollerback.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.gcm.GCMBroadcastReceiver;
import com.moziy.hollerback.model.Sender;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskGroup;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.service.task.VideoDownloadTask.POST_TXN_OPS;
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

        String where = ActiveRecordFields.C_VID_STATE + "='" + VideoModel.ResourceState.PENDING_DOWNLOAD + "' AND " + //
                ActiveRecordFields.C_VID_WATCHED_STATE + "='" + VideoModel.ResourceState.UNWATCHED + "'";

        // query the videos
        List<VideoModel> videos = VideoHelper.getVideosForTransaction(where); // clearing the transacting flag is done within the videodownloadtask

        if (videos.isEmpty()) {
            Log.w(TAG, TAG + " was initiated but no videos were found.");
            return;
        }

        // download them
        boolean success = downloadVideos(videos);

        if (success) { // lets notify the user that new videos have been downloaded

            Set<Sender> senders = new HashSet<Sender>();
            for (VideoModel v : videos) {
                senders.add(new Sender(v));
            }

            for (Sender s : senders) {
                String message = NotificationUtil.generateNewVideoMessage(HollerbackApplication.getInstance(), s);
                NotificationUtil.launchNotification(getApplicationContext(), NotificationUtil.generateNotification(getString(R.string.app_name), message), (int) s.getConversationId());
            }

        }

        // lets release the wakelock if any
        releaseWakeLock(intent);

    }

    private void releaseWakeLock(Intent intent) {
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public boolean downloadVideos(List<VideoModel> videos) {

        TaskGroup downloadTasks = new TaskGroup();

        // for each video, lets create a download task and add it to the task group
        for (final VideoModel video : videos) {
            VideoDownloadTask t = new VideoDownloadTask(video);
            t.setPostTxnOps(POST_TXN_OPS.CLEAR_ON_SUCCESS); // clears the transacting flag if the operation was successful
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

        // for all the failed tasks, clear the transacting flag
        for (Task t : downloadTasks.getFailedTasks()) {
            for (VideoModel v : videos) {
                if (v.getVideoId().equals(((VideoDownloadTask) t).getVideoId())) {
                    if (!v.isTransacting()) {
                        throw new IllegalStateException("Video must be transacting!");
                    }
                    VideoHelper.clearVideoTransacting(v);
                }
            }

        }

        // ok, lets see the overall status
        Log.d(TAG, "overall status: " + taskGroupStatus);

        // TODO - sajjad: check to see if we need to shutdown the wakelock
        return taskGroupStatus;
    }
}
