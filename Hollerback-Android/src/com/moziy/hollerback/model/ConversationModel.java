package com.moziy.hollerback.model;

import java.util.ArrayList;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.web.response.SyncPayload;

@Table(name = ActiveRecordFields.T_CONVERSATION)
public class ConversationModel extends BaseModel implements SyncPayload {

	@Column(name = ActiveRecordFields.C_CONV_ID)
	private long id;

	@Column(name = ActiveRecordFields.C_CONV_NAME)
	private String name;

	@Column(name = ActiveRecordFields.C_CONV_UNREAD)
	private int unread_count;

	@Column(name = ActiveRecordFields.C_CONV_CREATED_AT)
	private String created_at;
	
	@Column(name = ActiveRecordFields.C_CONV_DELETED_AT)
	private String deleted_at;
	
	@Column(name = ActiveRecordFields.C_CONV_LAST_MESSAGE_AT)
	private String last_message_at;
	
	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_SUBTITLE)
	private String most_recent_subtitle;
	
	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_THUMB_URL)
	private String most_recent_thumb_url;
	
	@Column(name = ActiveRecordFields.C_CONV_UNSEEN_COUNT)
	private String unseen_count;
	
	@Column(name = ActiveRecordFields.C_CONV_USER_ID)
	private long user_id;
	
	@Column(name = ActiveRecordFields.C_CONV_IS_DELETED)
	private boolean is_deleted;
	
	
	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_THUMB)
	private String recentThumbUrl;

	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_VIDEO)
	private String recentVideoUrl;

	

	@Column(name = ActiveRecordFields.C_CONV_URL)
	private String url;

	

	public long getConversation_Id() {
		return id;
	}

	public void setConversation_id(long conversation_id) {
		this.id = conversation_id;
	}

	public String getConversationName() {
		return name;
	}

	public void setConversation_name(String conversation_name) {
		this.name = conversation_name;
	}

	public int getConversationUnreadCount() {
		return unread_count;
	}

	public void setConversation_unread_count(int conversation_unread_count) {
		this.unread_count = conversation_unread_count;
	}
	
	public void setCreateTime(String value)
	{
		created_at = value;
	}
	
	public String getCreateTime()
	{
		return this.created_at;
	}
	
	public void setUrl(String value)
	{
		this.url = value;
	}
	
	public String getUrl()
	{
		return this.url;
	}

}
