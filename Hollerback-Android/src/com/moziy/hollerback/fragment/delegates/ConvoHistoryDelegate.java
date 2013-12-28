package com.moziy.hollerback.fragment.delegates;

import java.util.List;

import android.util.Log;

import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.ConversationFragment;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.Task;

/**
 * This class acts as a delegate to the conversation fragment and manages many history related operations
 * @author sajjad
 *
 */
public class ConvoHistoryDelegate implements Task.Listener {
    private static final String TAG = ConvoHistoryDelegate.class.getSimpleName();
    private static final int HISTORY_LIMIT = 5;
    private ConversationFragment mConvoFragment;

    public void onAttach(ConversationFragment fragment) {
        mConvoFragment = fragment;
    }

    public void onDetach() {
        mConvoFragment = null;
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetHistoryModelTask) {
            // create a download worker if necessary
            Log.d(TAG, "history dl complete - thread id: " + Thread.currentThread().getId());
            for (VideoModel v : ((GetHistoryModelTask) t).getAllConvoVideos()) {
                Log.d(TAG, v.toString());
                mConvoFragment.addHistoryVideo(v);
            }

            if (!mConvoFragment.hasNewVideos()) {
                mConvoFragment.startHistoryPlayback();
            }

        }

    }

    @Override
    public void onTaskError(Task t) {

    }

    public static class GetHistoryModelTask extends AbsTask {

        private long mConvoId;
        private List<VideoModel> mAllConvoVideos;
        private String mWhere;

        public GetHistoryModelTask(long convoId) {
            mConvoId = convoId;
            mWhere = ActiveRecordFields.C_VID_CONV_ID + "=" + mConvoId + " AND " + ActiveRecordFields.C_VID_ISREAD + "=1";
        }

        @Override
        public void run() {

            mAllConvoVideos = new Select()//
                    .from(VideoModel.class) //
                    .where(mWhere).orderBy("strftime('%s'," + ActiveRecordFields.C_VID_CREATED_AT + ") DESC").limit(HISTORY_LIMIT).execute();

        }

        public List<VideoModel> getAllConvoVideos() {
            return mAllConvoVideos;
        }

        // public List<VideoModel> getVideosForDownload() {
        // return mVideosForDownload;
        // }
    }

}
