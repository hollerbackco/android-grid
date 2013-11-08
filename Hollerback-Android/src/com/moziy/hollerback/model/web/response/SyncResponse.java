package com.moziy.hollerback.model.web.response;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
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

    public Object sync; // based on type deserialize to correct object

    public void convert() {
        Log.d("sync", "object: " + sync);
        if (Type.CONVERSATION.equals(type)) {
            sync = HollerbackApplication.getInstance().getObjectMapper().convertValue(sync, new TypeReference<ConversationModel>() {
            });
        } else if (Type.MESSAGE.equals(type)) {
            sync = HollerbackApplication.getInstance().getObjectMapper().convertValue(sync, VideoModel.class);
        } else {
            Log.d("sync response", "oh no" + type);
        }
    }

    // public void setSync(Object data) {

    // for (String key : obj.keySet()) {
    // Log.d("sync response", key + " , " + obj.toString());
    // }
    // Log.d("sync response", "setSync()" + data.toString());
    // if (Type.CONVERSATION.equals(type)) {
    // mPayload = HollerbackApplication.getInstance().getObjectMapper().convertValue(data, ConversationModel.class);
    // Log.d("convo: ", ((ConversationModel) mPayload).toString());
    // } else if (Type.CONVERSATION.equals(type)) {
    // mPayload = HollerbackApplication.getInstance().getObjectMapper().convertValue(data, VideoModel.class);
    // } else {
    // Log.d("sync response", "oh no");
    // }
    // }

    // @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
    // @JsonSubTypes({ //
    // @JsonSubTypes.Type(value = ConversationModel.class, name = "conversation"), //
    // @JsonSubTypes.Type(value = VideoModel.class, name = "message")
    // })
}
