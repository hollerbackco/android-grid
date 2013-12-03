package com.moziy.hollerback.service;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;

public class CleanupService extends IntentService {
    private static final String TAG = CleanupService.class.getSimpleName();
    private static final long FIVE_DAYS = 5 * 24 * 3600; // change this back

    public CleanupService() {
        super(CleanupService.class.getSimpleName());
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        List<ConversationModel> convos = new Select().from(ConversationModel.class).execute();

        for (ConversationModel convo : convos) {
            StringBuilder sb = new StringBuilder().append(ActiveRecordFields.C_VID_CONV_ID).append("=").append(convo.getConversationId());
            //
            // .append(" AND (").append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.UPLOADED).append("'")
            //
            // .append(" OR ").append(ActiveRecordFields.C_VID_STATE).append("='").append(VideoModel.ResourceState.WATCHED_AND_POSTED).append("'").append(") AND ")
            // .append(" strftime('%s', 'now') - strftime('%s', created_at) > ").append(FIVE_DAYS); //

            List<VideoModel> videos = new Select().from(VideoModel.class).where(sb.toString()).execute(); //

            for (VideoModel v : videos) {
                Log.d(TAG, v.toString());
            }

        }

        // query all read videos/sent videos older than 3 days and remove them
        // new Select().from(VideoModel.class).where(where)

    }
}
