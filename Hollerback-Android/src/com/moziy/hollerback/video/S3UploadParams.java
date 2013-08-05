package com.moziy.hollerback.video;

import android.content.Context;

import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnS3UploadListener;

public class S3UploadParams {

	private Context mContext;
	private OnS3UploadListener mOnS3UploadListener;
	private String mFilePath;
	private String mFileName;
	private String mFileType;
	public VideoModel mVideo;
	public String conversationId;

	public String customMessage;
	
	public String getFileType() {
		return mFileType;
	}

	public void setFileType(String mFileType) {
		this.mFileType = mFileType;
	}

	public static String VID_MP4 = "mp4";
	public static String IMG_PNG = "-thumb.png";

	public static String CONTENT_TYPE_MP4 = "video/mp4";
	public static String CONTENT_TYPE_PNG = "image/png";

	public Context getmContext() {
		return mContext;
	}

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}

	public OnS3UploadListener getOnS3UploadListener() {
		return mOnS3UploadListener;
	}

	public void setOnS3UploadListener(OnS3UploadListener mOnS3UploadListener) {
		this.mOnS3UploadListener = mOnS3UploadListener;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	public String getVideoName() {
		return mFileName;

	}

	public String getThumbnailName() {
		return FileUtil.getImageUploadName(mFileName);
	}
}