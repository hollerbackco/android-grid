package com.moziy.hollerback.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;

import com.moziy.hollerback.debug.LogUtil;

import android.media.MediaRecorder.OutputFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FileUtil {

	private static String DIRECTORY_NAME = "Hollerback";

	private static final String TAG = "Hollerback FileUtil";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
				DIRECTORY_NAME);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	/** Create a File for saving an image or video */
	public static File getOutputVideoFile(String filename) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		// File mediaStorageDir = new File(Environment
		// .getExternalStorageDirectory().getAbsolutePath()
		// + "/"
		// + DIRECTORY_NAME);
		//
		// // This location works best if you want the created images to be
		// shared
		// // between applications and persist after your app has been
		// uninstalled.
		//
		// // Create the storage directory if it does not exist
		// if (!mediaStorageDir.exists()) {
		// if (!mediaStorageDir.mkdirs()) {
		// Log.d(TAG, "failed to create directory");
		// return null;
		// }
		// }

		String[] fileParts = filename.split(Matcher.quoteReplacement(System
				.getProperty("file.separator")));
		LogUtil.i("File: " + fileParts[0]);
		LogUtil.i("File: " + fileParts[1]);

		File mediaStorageDir = new File(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/"
				+ DIRECTORY_NAME + "/" + fileParts[0]);
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}
		// Create a media file name

		File mediaFile;

		mediaFile = new File(mediaStorageDir.getPath() + "/" + fileParts[1]);

		return mediaFile;
	}

	public static String getFilePath() {

		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + DIRECTORY_NAME;

		// LogUtil.i("DIR: " + filePath);

		return filePath;
	}

	public static String getLocalFile(String fileKey) {
		String path = (getFilePath() + "/" + fileKey);
		// LogUtil.i("Local File: " + path);
		return path;
	}

	public static long getFileSize(String fileKey) {
		File file = new File(getLocalFile(fileKey));
		return file.length();
	}

	public static String generateRandomHexName() {
		Random m = new Random();
		String hexString = Integer.toHexString(m.nextInt(256)).toUpperCase(
				Locale.US);
		hexString = hexString.trim().length() == 2 ? hexString : "0"
				+ hexString.trim();
		return hexString;

	}

	/**
	 * Generates a EF/dasdfadsfafafdsfafas extensionless name
	 * 
	 * @return
	 */
	public static String generateRandomFileName() {
		return generateRandomHexName() + "/" + UUID.randomUUID();
	}

	public static String getFileFormat(int fileFormat) {
		switch (fileFormat) {
		case OutputFormat.MPEG_4:
			return "mp4";
		case OutputFormat.RAW_AMR:
			return "RAW_AMR";
		case OutputFormat.THREE_GPP:
			return "3gp";
		case OutputFormat.DEFAULT:
			return "DEFAULT";
		default:
			return "unknown";
		}
	}

	public static String getImageUploadName(String filename) {
		return filename.split("\\.")[0] + "-thumb.png";
	}
}
