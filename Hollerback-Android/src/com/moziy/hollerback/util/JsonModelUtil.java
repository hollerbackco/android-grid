package com.moziy.hollerback.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.moziy.hollerback.model.VideoModel;

public class JsonModelUtil {

    public static VideoModel createVideo(JSONObject videoItem) {
        try {
            VideoModel video = new VideoModel();
            video.setLocalFileName(videoItem.getString("filename"));
            video.setVideoId(videoItem.getString("id"));
            video.setConversationId(videoItem.getLong("conversation_id"));
            video.setRead(videoItem.getBoolean("isRead"));
            video.setFileUrl(videoItem.getString("url"));
            video.setThumbUrl(videoItem.getString("thumb_url"));
            video.setCreateDate(videoItem.getString("created_at"));

            // changed to sender_name since username was somehow deprecated: 9/1/2013 - PM
            video.setSenderName(videoItem.getString("sender_name"));

            if (videoItem.has("isUploading")) {
                video.setUploading(true);
            }
            return video;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}
