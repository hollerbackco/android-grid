package com.moziy.hollerback.model.web.response;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.ResponseObject;

public class SyncResponse implements ResponseObject {

    public static interface Type {
        public static final String CONVERSATION = "conversation";
        public static final String MESSAGE = "message";
    }

    public String type;

    public SyncPayload mSync; // based on type deserialize to correct object

    @JsonSetter("sync")
    public void setSync(Object obj) {
        if (Type.CONVERSATION.equals(type)) {
            mSync = HollerbackApplication.getInstance().getObjectMapper().convertValue(obj, ConversationModel.class);
        } else {
            mSync = HollerbackApplication.getInstance().getObjectMapper().convertValue(obj, VideoModel.class);
        }
    }
}
