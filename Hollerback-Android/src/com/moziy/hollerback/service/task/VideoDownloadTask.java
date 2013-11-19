package com.moziy.hollerback.service.task;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.HBFileUtil;

public class VideoDownloadTask extends HttpDownloadTask {
    private static final String TAG = VideoDownloadTask.class.getSimpleName();

    private String mVideoId;

    public VideoDownloadTask(VideoModel model) {
        super(model.getFileUrl(), HBFileUtil.getOutputVideoFile(model));
        mVideoId = model.getVideoId(); // don't hold on to the model

        // mark video as downloading
        model.setState(VideoModel.ResourceState.DOWNLOADING);
        model.setTransacting();
        model.save();

    }

    @Override
    public void run() {

        super.run(); // download it

        // depending on the status, now, mark the resource state correctly
        VideoModel model = new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_GUID + " = ?", mVideoId).executeSingle();
        if (isSuccess()) {
            Log.d(TAG, "marking video as ondisk");
            model.setState(VideoModel.ResourceState.ON_DISK);
        } else {
            Log.d(TAG, "marking video as pending download");
            model.setState(VideoModel.ResourceState.PENDING_DOWNLOAD);
        }
        model.clearTransacting();
        model.save();

    }

    public String getVideoId() {
        return mVideoId;
    }
}
