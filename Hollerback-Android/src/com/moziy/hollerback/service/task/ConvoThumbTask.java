package com.moziy.hollerback.service.task;

import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.ConversationModel;

public class ConvoThumbTask extends AbsTask {

    private GenerateVideoThumbTask mThumbTask;
    private long mConvoId;

    public ConvoThumbTask(long convoId, GenerateVideoThumbTask thumbTask) {
        mConvoId = convoId;
        mThumbTask = thumbTask;
    }

    @Override
    public void run() {

        mThumbTask.run(); // run the thumbtask

        if (mThumbTask.isSuccess()) {

            // lets save the bitmap

            // lookup the conversation up and update the thumb if it's not set already
            ConversationModel c = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId).executeSingle();
            if (c.getMostRecentThumbUrl() == null || "".equals(c.getMostRecentThumbUrl())) {

                c.save();
            }
        }

    }

}
