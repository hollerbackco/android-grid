package com.moziy.hollerback.model;

import java.util.ArrayList;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.moziy.hollerback.database.ActiveRecordFields;

@Table(name = ActiveRecordFields.T_CONVERSATION)
public class ConversationModel extends BaseModel {

	@Column(name = ActiveRecordFields.C_CONV_ID)
	private int conversation_id;

	@Column(name = ActiveRecordFields.C_CONV_NAME)
	private String conversation_name;

	@Column(name = ActiveRecordFields.C_CONV_UNREAD)
	private int conversation_unread_count;

	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_THUMB)
	private String recentThumbUrl;

	@Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_VIDEO)
	private String recentVideoUrl;

	private ArrayList<VideoModel> mVideos;

	// public ArrayList<VideoModel> getVideos() {
	// return mVideos;
	// }

	// public void setVideos(ArrayList<VideoModel> mVideos) {
	// this.mVideos = mVideos;
	// }

	public int getConversation_Id() {
		return conversation_id;
	}

	public void setConversation_id(int conversation_id) {
		this.conversation_id = conversation_id;
	}

	public String getConversationName() {
		return conversation_name;
	}

	public void setConversation_name(String conversation_name) {
		this.conversation_name = conversation_name;
	}

	public int getConversationUnreadCount() {
		return conversation_unread_count;
	}

	public void setConversation_unread_count(int conversation_unread_count) {
		this.conversation_unread_count = conversation_unread_count;
	}

}
