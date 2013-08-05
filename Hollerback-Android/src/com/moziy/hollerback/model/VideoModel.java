package com.moziy.hollerback.model;

import java.io.Serializable;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.moziy.hollerback.database.ActiveRecordFields;

@Table(name = ActiveRecordFields.T_VIDEOS)
public class VideoModel extends BaseModel implements Serializable {
	private static final long serialVersionUID = -674572541294872489L;

	@Column(name = ActiveRecordFields.C_VID_FILENAME)
	private String filename;

	@Column(name = ActiveRecordFields.C_VID_ISREAD)
	private boolean isRead;

	@Column(name = ActiveRecordFields.C_VID_ID)
	private int videoId;

	@Column(name = ActiveRecordFields.C_VID_CONV_ID)
	private String convId;

	@Column(name = ActiveRecordFields.C_VID_FILEURL)
	private String fileUrl;

	@Column(name = ActiveRecordFields.C_VID_THUMBURL)
	private String thumbUrl;

	private boolean uploaded;

	@Column(name = ActiveRecordFields.C_VID_CREATEDATE)
	private String formated_created_at;
	
	@Column(name = ActiveRecordFields.C_VID_USERNAME)
	private String username;
	
	public String getConversationId() {
		return convId;
	}

	public void setConversationId(String mConvId) {
		this.convId = mConvId;
	}
	
	public String getCreateDate()
	{
		return formated_created_at;
	}
	
	public void setCreateDate(String value)
	{
		formated_created_at = value;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getFileName() {
		return filename;
	}

	public void setFileName(String fileName) {
		this.filename = fileName;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int id) {
		videoId = id;
	}

	public static String getURLPath() {
		return null;
	}
	
	public void setUserName(String value)
	{
		username = value;
	}
	
	public String getUserName()
	{
		return username;
	}

	@Override
	public boolean equals(Object obj) {
		VideoModel video = (VideoModel) obj;
		if (videoId == video.videoId) {
			return true;
		}
		return false;
	}
}
