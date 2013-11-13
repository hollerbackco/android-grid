package com.moziy.hollerback.model;

import java.io.Serializable;
import java.util.Arrays;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.web.response.SyncPayload;

@Table(name = ActiveRecordFields.T_VIDEOS)
public class VideoModel extends BaseModel implements Serializable, SyncPayload {
    private static final long serialVersionUID = -674572541294872489L;

    public interface ResourceState {
        public static final String PENDING_UPLOAD = "pending_upload";
        public static final String UPLOADED_PENDING_POST = "uploaded_pending_post";
        public static final String UPLOADED = "uploaded";
        public static final String PENDING_DOWNLOAD = "pending_download";
        public static final String DOWNLOADING = "downloading";
        public static final String ON_DISK = "on_disk";
        public static final String UNWATCHED = "unwatched"; // => equivalent to isRead=false
        public static final String WATCHED_PENDING_POST = "watched_pending_post";
        public static final String WATCHED_AND_POSTED = "watched_and_posted";

    }

    @Column(name = ActiveRecordFields.C_VID_CREATED_AT)
    @JsonProperty("created_at")
    private String created_at;

    @Column(name = ActiveRecordFields.C_VID_NEEDS_REPLY)
    @JsonProperty("needs_reply")
    private boolean needs_reply;

    @Column(name = ActiveRecordFields.C_VID_SENDER_NAME)
    @JsonProperty("sender_name")
    private String sender_name;

    @Column(name = ActiveRecordFields.C_VID_SENT_AT)
    @JsonProperty("sent_at")
    private String sent_at;

    @Column(name = ActiveRecordFields.C_VID_GUID)
    @JsonProperty("guid")
    private String guid;

    @Column(name = ActiveRecordFields.C_VID_URL)
    @JsonProperty("url")
    private String url;

    private String local_url;

    @Column(name = ActiveRecordFields.C_VID_THUMBURL)
    @JsonProperty("thumb_url")
    private String thumb_url;

    @Column(name = ActiveRecordFields.C_VID_CONV_ID)
    @JsonProperty("conversation_id")
    private long conversation_id = -1;

    @Column(name = ActiveRecordFields.C_VID_IS_DELETED)
    @JsonProperty("is_deleted")
    private boolean is_deleted;

    @Column(name = ActiveRecordFields.C_VID_SUBTITLE)
    @JsonProperty("subtitle")
    private String subtitle;

    @Column(name = ActiveRecordFields.C_VID_ISREAD)
    @JsonProperty("isRead")
    private boolean isRead;

    @Column(name = ActiveRecordFields.C_VID_WATCHED_STATE)
    private String watched_state;

    @Column(name = ActiveRecordFields.C_VID_FILENAME)
    private String local_filename; // TODO - Sajjad: double check that this is in fact the local file name

    @Column(name = ActiveRecordFields.C_VID_IS_SEGMENTED)
    private boolean is_segmented;

    @Column(name = ActiveRecordFields.C_VID_SEGMENTED_FILENAME)
    private String segment_filename;

    @Column(name = ActiveRecordFields.C_VID_SEGMENTED_FILE_EXT)
    private String segment_file_extension; // the file extension "mp4", "amr", "3gpp", etc: conveys the container info

    @Column(name = ActiveRecordFields.C_VID_ID)
    private String id;

    @Column(name = ActiveRecordFields.C_VID_STATE)
    private String state; // REST state of this resource: more of networking state

    @Column(name = ActiveRecordFields.C_VID_TRANSACTING)
    private boolean transacting; // Whether this resource is being actively transitioned from one state to the next

    @Column(name = ActiveRecordFields.C_VID_NUM_PARTS)
    private int num_parts = 1; // default of 100: This will get adjusted once the actual num_parts is known. We do this to ensure that the video doesn't get posted

    @Column(name = ActiveRecordFields.C_VID_PART_UPLOAD_STATE)
    private boolean[] part_upload_state = new boolean[num_parts]; // the size of this will always be equal to num_parts

    // add a list of phone numbers/contacts that this is supposed to get sent to in case there's a failure
    @Column(name = ActiveRecordFields.C_VID_RECIPIENTS)
    private String[] recipients;

    @Deprecated
    @Column(name = ActiveRecordFields.C_VID_ISUPLOADING)
    private boolean isUploading;

    @Deprecated
    @Column(name = ActiveRecordFields.C_VID_ISSENT)
    private boolean isSent;

    public long getConversationId() {
        return conversation_id;
    }

    public void setConversationId(long mConvId) {
        this.conversation_id = mConvId;
    }

    public String getCreateDate() {
        return created_at;
    }

    public void setCreateDate(String value) {
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

    public String getLocalFileName() {
        return local_filename;
    }

    public void setLocalFileName(String fileName) {
        this.local_filename = fileName;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getWatchedState() {
        return watched_state;
    }

    public void setWatchedState(String watchedState) {
        watched_state = watchedState;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public void setTransacting() {
        transacting = true;
    }

    public void clearTransacting() {
        transacting = false;
    }

    public boolean isTransacting() {
        return transacting;
    }

    @JsonSetter("id")
    public void setVideoId(String videoId) {
        this.id = videoId;
    }

    public String getVideoId() {
        return this.id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getNumParts() {
        return num_parts;
    }

    public void setNumParts(int parts) {
        this.num_parts = parts;
        this.part_upload_state = Arrays.copyOf(this.part_upload_state, parts);

    }

    public void setPartUploadState(int part, boolean state) {
        if (part_upload_state.length > part) {
            part_upload_state[part] = state;
        }
    }

    public boolean getPartUploadState(int part) {
        return part_upload_state[part];
    }

    public boolean isUploadSuccessfull() {
        for (int i = 0; i < part_upload_state.length; i++) {
            if (part_upload_state[i] == false)
                return false;
        }
        return true;
    }

    public boolean isSegmented() {
        return is_segmented;
    }

    public void setSegmented(boolean segmented) {
        is_segmented = true;
    }

    public String getSegmentFileName() {
        return segment_filename;
    }

    public void setSegmentFileName(String name) {
        segment_filename = name;
    }

    public String getSegmentFileExtension() {
        return segment_file_extension;
    }

    public void setSegmentFileExtension(String extension) {
        this.segment_file_extension = extension;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
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

    // the video id is no longer an integer
    @Deprecated
    public void setVideoId(int id) {
    }

    public static String getURLPath() {
        return null;
    }

    public void setSenderName(String value) {
        sender_name = value;
    }

    public String getSenderName() {
        return sender_name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VideoModel other = (VideoModel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VideoModel [created_at=" + created_at + ", needs_reply=" + needs_reply + ", sender_name=" + sender_name + ", sent_at=" + sent_at + ", guid=" + guid + ", url=" + url + ", local_url="
                + local_url + ", thumb_url=" + thumb_url + ", conversation_id=" + conversation_id + ", is_deleted=" + is_deleted + ", subtitle=" + subtitle + ", isRead=" + isRead
                + ", local_filename=" + local_filename + ", is_segmented=" + is_segmented + ", segment_filename=" + segment_filename + ", segment_file_extension=" + segment_file_extension + ", id="
                + id + ", state=" + state + ", transacting=" + transacting + ", num_parts=" + num_parts + ", part_upload_state=" + Arrays.toString(part_upload_state) + ", recipients=" + recipients
                + ", isUploading=" + isUploading + ", isSent=" + isSent + "]";
    }

}
