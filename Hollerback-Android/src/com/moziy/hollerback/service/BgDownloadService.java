package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.TaskGroup;
import com.moziy.hollerback.service.task.VideoDownloadTask;

public class BgDownloadService extends IntentService {
    private static final String TAG = BgDownloadService.class.getSimpleName();

    public BgDownloadService() {
        super(BgDownloadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // once initiated, the service will download video/images

        // search for all model whose videos are pending download
        List<VideoModel> videos = new Select().from(VideoModel.class) //
                .where(ActiveRecordFields.C_VID_STATE + "=? AND " + //
                        ActiveRecordFields.C_VID_WATCHED_STATE + "=? AND " + //
                        ActiveRecordFields.C_VID_TRANSACTING + "=?", //
                        VideoModel.ResourceState.PENDING_DOWNLOAD, VideoModel.ResourceState.UNWATCHED, 0).execute(); //

        TaskGroup downloadTasks = new TaskGroup();

        // for each video, lets create a download task and add it to the task group
        for (VideoModel video : videos) {
            downloadTasks.addTask(new VideoDownloadTask(video));
        }

        // lets run the task group
        downloadTasks.run();

        // after it's run, lets see if we had any failed tasks or not and retry them.
        if (!downloadTasks.isSuccess()) {
            Log.d(TAG, "some tasks failed.");
        }

        // TODO - sajjad: check to see if we need to shutdown the wakelock
    }
}
