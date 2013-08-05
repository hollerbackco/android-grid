package com.moziy.hollerback.helper;

import java.util.List;

import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;

public class ActiveRecordHelper {

	public static List<ConversationModel> getAllConversations() {
		try {

			return new Select().from(ConversationModel.class).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<VideoModel> getVideosForConversation(
			String conversationId) {
		try {
			return new Select()
					.from(VideoModel.class)
					.where(ActiveRecordFields.C_VID_CONV_ID + " = ?",
							conversationId).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
