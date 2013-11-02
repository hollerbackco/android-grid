package com.moziy.hollerback.model;

import java.io.Serializable;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.web.response.SyncPayload;

@Table(name = ActiveRecordFields.T_VIDEOS)
public class VideoModel extends BaseModel implements Serializable, SyncPayload {
	private static final long serialVersionUID = -674572541294872489L;

	public interface ResourceState{
		public static final String PENDING_UPLOAD = "pending_upload";
		public static final String UPLOADED_PENDING_POST = "uploaded_pending_post";
		public static final String UPLOADED = "uploaded";
		public static final String PENDING_DOWNLOAD = "pending_download";
		public static final String DOWNLOADING = "downloading";
		public static final String ON_DISK = "on_disk";
	}
	
	@Column(name = ActiveRecordFields.C_VID_CREATED_AT)
	private String created_at;
	
	@Column(name = ActiveRecordFields.C_VID_NEEDS_REPLY)
	private boolean needs_reply;
	
	@Column(name = ActiveRecordFields.C_VID_SENDER_NAME)
	private String sender_name;
	
	@Column(name = ActiveRecordFields.C_VID_SENT_AT)
	private String sent_at;
	
	@Column(name = ActiveRecordFields.C_VID_GUID)
	private String guid;
	
	@Column(name = ActiveRecordFields.C_VID_URL)
	private String url;

	@Column(name = ActiveRecordFields.C_VID_THUMBURL)
	private String thumb_url;
	
	@Column(name = ActiveRecordFields.C_VID_CONV_ID)
	private String conversation_id;

	@Column(name = ActiveRecordFields.C_VID_IS_DELETED)
	private boolean is_deleted;
	
	@Column(name = ActiveRecordFields.C_VID_SUBTITLE)
	private String subtitle;
	
	@Column(name = ActiveRecordFields.C_VID_ISREAD)
	private boolean isRead;
	
	@Column(name = ActiveRecordFields.C_VID_FILENAME)
	private String local_filename;	//TODO - Sajjad: double check that this is in fact the local file name

	@Column(name = ActiveRecordFields.C_VID_ID)
	private String id;
	
	@Column(name = ActiveRecordFields.C_VID_STATE)
	private String state; //REST state of this resource
	
	@Column(name = ActiveRecordFields.C_VID_TRANSACTING)
	private boolean transacting; //Whether this resource is being actively transitioned from one state to the next
	
	
	

	@Deprecated
	@Column(name = ActiveRecordFields.C_VID_ISUPLOADING)
	private boolean isUploading;
	
	@Deprecated
	@Column(name = ActiveRecordFields.C_VID_ISSENT)
	private boolean isSent;

	
	public String getConversationId() {
		return conversation_id;
	}

	public void setConversationId(String mConvId) {
		this.conversation_id = mConvId;
	}
	
	public String getCreateDate()
	{
		return created_at;
	}
	
	public void setCreateDate(String value)
	{
		created_at = value;
	}

	public String getThumbUrl() {
		return thumb_url;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumb_url = thumbUrl;
	}

	public String getFileUrl() {
		return url;
	}

	public void setFileUrl(String fileUrl) {
		this.url = fileUrl;
	}

	public String getFileName() {
		return local_filename;
	}

	public void setFileName(String fileName) {
		this.local_filename = fileName;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean issent) {
		this.isSent = issent;
	}
	
	public boolean isUploading() {
		return isUploading;
	}

	public void setUploading(boolean isuploading) {
		this.isUploading = isuploading;
	}

	//The video id is no longer an integer
	@Deprecated
	public int getVideoId() {
		return 0;
	}

	//the video id is no longer an integer
	@Deprecated
	public void setVideoId(int id) {
	}

	public static String getURLPath() {
		return null;
	}
	
	public void setUserName(String value)
	{
		sender_name = value;
	}
	
	public String getUserName()
	{
		return sender_name;
	}

	//XXX: BROKEN EQUALS, MUST FIX
	@Override
	public boolean equals(Object obj) {
		VideoModel video = (VideoModel) obj;
		if (id == video.id) {
			return true;
		}
		return false;
	}
}
