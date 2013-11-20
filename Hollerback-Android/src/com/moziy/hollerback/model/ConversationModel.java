package com.moziy.hollerback.model;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.web.response.SyncPayload;
import com.moziy.hollerback.util.TimeUtil;

@Table(name = ActiveRecordFields.T_CONVERSATION)
public class ConversationModel extends BaseModel implements Serializable, SyncPayload {

    /**
     * 
     */
    private static final long serialVersionUID = 776201028447951350L;

    @Column(name = ActiveRecordFields.C_CONV_ID)
    private long id;

    @Column(name = ActiveRecordFields.C_CONV_NAME)
    @JsonProperty("name")
    private String name;

    @Column(name = ActiveRecordFields.C_CONV_UNREAD)
    @JsonProperty("unread_count")
    private String unread_count;

    @Column(name = ActiveRecordFields.C_CONV_CREATED_AT)
    @JsonProperty("created_at")
    private String created_at;

    @Column(name = ActiveRecordFields.C_CONV_DELETED_AT)
    @JsonProperty("deleted_at")
    private String deleted_at;

    @Column(name = ActiveRecordFields.C_CONV_LAST_MESSAGE_AT)
    @JsonProperty("last_message_at")
    private String last_message_at;

    @Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_SUBTITLE)
    @JsonProperty("most_recent_subtitle")
    private String most_recent_subtitle;

    @Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_THUMB_URL)
    @JsonProperty("most_recent_thumb_url")
    private String most_recent_thumb_url;

    @Column(name = ActiveRecordFields.C_CONV_UNSEEN_COUNT)
    @JsonProperty("unseen_count")
    private String unseen_count;

    @Column(name = ActiveRecordFields.C_CONV_USER_ID)
    @JsonProperty("user_id")
    private long user_id;

    @Column(name = ActiveRecordFields.C_CONV_IS_DELETED)
    @JsonProperty("is_deleted")
    private boolean is_deleted;

    @JsonProperty("updated_at")
    private String updated_at;

    @Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_THUMB)
    private String recentThumbUrl;

    @Column(name = ActiveRecordFields.C_CONV_MOST_RECENT_VIDEO)
    private String recentVideoUrl;

    @Column(name = ActiveRecordFields.C_CONV_URL)
    private String url;

    public long getConversationId() {
        return id;
    }

    public void setConversation_id(long conversation_id) {
        this.id = conversation_id;
    }

    public String getConversationName() {
        return name;
    }

    public void setConversation_name(String name) {
        this.name = name;
    }

    public int getConversationUnreadCount() {
        return Integer.valueOf(unread_count);
    }

    public void setConversation_unread_count(int conversation_unread_count) {
        this.unread_count = String.valueOf(conversation_unread_count);
    }

    public void setCreateTime(String value) {
        created_at = value;
    }

    public String getCreateTime() {
        return this.created_at;
    }

    public String getLastMessageAt() {
        return last_message_at;

    }

    public void setLastMessageAt() {
        last_message_at = TimeUtil.SERVER_TIME_FORMAT.format(new Date());
    }

    public long getLastMessageAtInMillis() {
        try {
            Date d = TimeUtil.SERVER_TIME_FORMAT.parse(last_message_at);
            return d.getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    public void setUrl(String value) {
        this.url = value;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return "ConversationModel [id=" + id + ", name=" + name + ", unread_count=" + unread_count + ", created_at=" + created_at + ", deleted_at=" + deleted_at + ", last_message_at="
                + last_message_at + ", most_recent_subtitle=" + most_recent_subtitle + ", most_recent_thumb_url=" + most_recent_thumb_url + ", unseen_count=" + unseen_count + ", user_id=" + user_id
                + ", is_deleted=" + is_deleted + ", updated_at=" + updated_at + ", recentThumbUrl=" + recentThumbUrl + ", recentVideoUrl=" + recentVideoUrl + ", url=" + url + "]";
    }

}
