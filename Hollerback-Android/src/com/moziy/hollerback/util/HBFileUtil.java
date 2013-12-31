package com.moziy.hollerback.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;

import android.media.MediaRecorder.OutputFormat;
import android.os.Environment;
import android.util.Log;

import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.VideoModel;

public class HBFileUtil {

    private static String DIRECTORY_NAME = "Hollerback";

    private static final String TAG = "Hollerback FileUtil";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static File getOutputVideoFile(VideoModel video) {
        String filename = video.getGuid();
        if (filename == null) {
            filename = video.getVideoId();
        }

        String subDir = filename.substring(0, 2); // this is the hex portion to use

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + subDir);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }

            File nomedia = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + DIRECTORY_NAME + "/.nomedia");
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    Log.w(TAG, "couldn't create global no media file");
                    e.printStackTrace();
                }
            }

            // create a no media file and place it
            nomedia = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + subDir + "/.nomedia");
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                Log.w(TAG, "couldn't create .nomedia file in dir: " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + subDir);
                e.printStackTrace();
            }
        }

        // XXX: parse the filename rather than assuming it's mp4
        File media = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + subDir + "/" + filename + ".mp4");
        return media;
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

        String[] fileParts = filename.split(Matcher.quoteReplacement(System.getProperty("file.separator")));
        LogUtil.i("File: " + fileParts[0]);
        LogUtil.i("File: " + fileParts[1]);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + fileParts[0]);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }

            File nomedia = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + DIRECTORY_NAME + "/.nomedia");
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    Log.w(TAG, "couldn't create global no media file");
                    e.printStackTrace();
                }
            }

            // create a no media file and place it
            nomedia = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + fileParts[0] + "/.nomedia");
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                Log.w(TAG, "couldn't create .nomedia file in dir: " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + fileParts[0]);
                e.printStackTrace();
            }
        }
        // Create a media file name

        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + "/" + fileParts[1]);

        return mediaFile;
    }

    public static String getFilePath() {

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRECTORY_NAME;

        // LogUtil.i("DIR: " + filePath);

        return filePath;
    }

    public static String getLocalFile(String fileKey) {
        String path = (getFilePath() + "/" + fileKey);
        // LogUtil.i("Local File: " + path);
        return path;
    }

    /**
     * Assumes that the file is on disk
     * @param segmentNum
     * @param guid
     * @param extension
     * @return
     */
    public static File getSegmentedFile(int segmentNum, String guid, String extension) {
        String fileName = getLocalFile(segmentNum, guid, extension);
        File f = new File(fileName);

        return f;
    }

    /**
     * 
     * @param partNum
     * @param guid
     * @return The full path of the local file given a part number and a guid
     */
    public static String getLocalFile(int partNum, String guid, String extension) {

        StringBuilder sb = new StringBuilder();
        sb.append(getFilePath()).append("/").append(guid.substring(0, 2)).append("/").append(guid).append(".").append(partNum).append(".").append(extension);

        return sb.toString();
    }

    public static long getFileSize(String fileKey) {
        File file = new File(getLocalFile(fileKey));
        return file.length();
    }

    public static String generateRandomHexName() {
        Random m = new Random();
        String hexString = Integer.toHexString(m.nextInt(256)).toUpperCase(Locale.US);
        hexString = hexString.trim().length() == 2 ? hexString : "0" + hexString.trim();
        return hexString;

    }

    /**
     * Generates a EF/dasdfadsfafafdsfafas extensionless name
     * 
     * @return
     */
    public static String generateRandomFileName() {
        String name = UUID.randomUUID().toString();
        return name.substring(0, 2) + "/" + name;
    }

    public static String generateFileNameFromGUID(String guid) {
        return guid.substring(0, 2) + "/" + guid;
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
        return filename + "-thumb.png";
    }
}
