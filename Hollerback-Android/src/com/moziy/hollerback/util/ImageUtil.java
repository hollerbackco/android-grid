package com.moziy.hollerback.util;

import java.io.File;
import java.io.FileOutputStream;

import com.moziy.hollerback.debug.LogUtil;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

public class ImageUtil {

	//Do this in background thread or AsyncTask later
	public static Bitmap generateThumbnail(String videoFileNameSource) {
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(
				FileUtil.getLocalFile(videoFileNameSource),
				Thumbnails.MICRO_KIND);
		LogUtil.d("Bitmap null:  " + Boolean.toString(bitmap == null));
		writeBitmapToExternal(FileUtil.getImageUploadName(videoFileNameSource),
				bitmap);
		return bitmap;
	}

	public static String writeBitmapToExternal(String name, Bitmap bitmap) {
		File file = FileUtil.getOutputVideoFile(name);
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

}
