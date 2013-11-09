package com.moziy.hollerback.util;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;

/**
 * Access a lot of things in the app stands for quickutil
 * 
 * @author jianchen
 * 
 */
public class QU {

    /**
     * Get DataModelManager
     */
    public static DataModelManager getDM() {
        return HollerbackApplication.getInstance().getDM();
    }

    public static ObjectMapper getObjectMapper() {
        return HollerbackApplication.getInstance().getObjectMapper();
    }

    public static ConversationModel getConv(long id) {
        ArrayList<ConversationModel> models = ((ArrayList<ConversationModel>) getDM().getObjectForToken(HashUtil.getConvHash()));
        for (ConversationModel model : models) {
            if (model.getConversation_Id() == id) {
                return model;
            }

        }
        return null;
    }

    public static void updateConversationVideo(VideoModel video) {
        ArrayList<ConversationModel> models = ((ArrayList<ConversationModel>) getDM().getObjectForToken(HashUtil.getConvHash()));
    }

    /**
     * Get String from strings file
     */
    public static String s(int id) {
        return HollerbackApplication.getInstance().getString(id);
    }

}
