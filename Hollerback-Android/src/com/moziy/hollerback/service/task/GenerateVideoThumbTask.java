package com.moziy.hollerback.service.task;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

/**
 * This class generates a thumbnail for a given video file
 * @author sajjad
 *
 */
public class GenerateVideoThumbTask extends AbsTask {

    private String mFilePath;
    private Bitmap mThumb;

    public GenerateVideoThumbTask(String absoluteVideoPath) {
        mFilePath = absoluteVideoPath;
    }

    public Bitmap getThumb() {
        return mThumb;
    }

    @Override
    public void run() {

        mThumb = ThumbnailUtils.createVideoThumbnail(mFilePath, Thumbnails.MINI_KIND);
        if (mThumb == null) {
            mIsSuccess = false;
        } else {
            mIsSuccess = true;
        }

        mIsFinished = true;
    }
}
