package com.moziy.hollerback.util;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

import com.moziy.hollerback.debug.LogUtil;

public class ImageUtil {

    // Do this in background thread or AsyncTask later
    public static Bitmap generateThumbnail(String videoFileNameSource) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(HBFileUtil.getLocalFile(videoFileNameSource), Thumbnails.MINI_KIND);
        LogUtil.d("Bitmap null:  " + Boolean.toString(bitmap == null));
        writeBitmapToExternal(HBFileUtil.getImageUploadName(videoFileNameSource), bitmap);
        return bitmap;
    }

    public static String writeBitmapToExternal(String name, Bitmap bitmap) {
        // File file = HBFileUtil.getOutputVideoFile(name);
        Log.d("imageutil", name);
        File file = new File(name);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            LogUtil.i("Saved thumb to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap generatePngThumbnailFromVideo(int partNum, String videoGuid) {
        return generatePngThumbnailFromVideo(partNum, videoGuid, Thumbnails.MICRO_KIND);
    }

    public static Bitmap generatePngThumbnailFromVideo(int partNum, String videoGuid, int kind) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(HBFileUtil.getLocalVideoFile(partNum, videoGuid, "mp4"), kind);
        LogUtil.d("Bitmap null:  " + Boolean.toString(bitmap == null));
        writeBitmapToExternal(HBFileUtil.getLocalVideoFile(partNum, videoGuid, "png"), bitmap);

        return bitmap;
    }

}
