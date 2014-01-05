package com.moziy.hollerback.service.task;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

import com.moziy.hollerback.util.ImageUtil;

/**
 * This class generates a thumbnail for a given video file
 * @author sajjad
 *
 */
public class GenerateVideoThumbTask extends AbsTask {

    private String mSrcFilePath;
    private String mDstFilePath;
    private Bitmap mThumb;

    /**
     * Only generates the thumb as a bitmap and returns
     * @param absoluteVideoPath
     */
    public GenerateVideoThumbTask(String absoluteVideoPath) {
        mSrcFilePath = absoluteVideoPath;
    }

    public GenerateVideoThumbTask(String sourceVideoPath, String destPngPath) {
        mSrcFilePath = sourceVideoPath;
        mDstFilePath = destPngPath;
    }

    public Bitmap getThumb() {
        return mThumb;
    }

    public String getDstPath() {
        return mDstFilePath;
    }

    @Override
    public void run() {

        mThumb = ThumbnailUtils.createVideoThumbnail(mSrcFilePath, Thumbnails.MINI_KIND);
        if (mThumb != null) {

            // lets save the thumb
            if (mDstFilePath != null) {
                if (ImageUtil.writeBitmapToExternal(mDstFilePath, mThumb) != null) {
                    mIsSuccess = true;
                }
            } else
                mIsSuccess = true;
        } else {
            mIsSuccess = false;
        }

        mIsFinished = true;
    }
}
