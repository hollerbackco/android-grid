package com.moziy.hollerback.fragment.delegates;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.activeandroid.query.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.AbsFragmentLifecylce;
import com.moziy.hollerback.fragment.ConversationFragment;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.Task;

/**
 * This class acts as a delegate to the conversation fragment and manages many history related operations
 * @author sajjad
 *
 */
public class ConvoHistoryDelegate extends AbsFragmentLifecylce implements Task.Listener {
    private static final String TAG = ConvoHistoryDelegate.class.getSimpleName();
    private static final int HISTORY_LIMIT = 5;
    private ConversationFragment mConvoFragment;
    private ArrayList<VideoModel> mLocalHistory;
    private ArrayList<VideoModel> mRemoteHistory;
    private long mConvoId;

    private interface Worker {
        public static final String REMOTE_HISTORY = "remote_history";
    }

    public ConvoHistoryDelegate(long convoId) {
        mConvoId = convoId;
    }

    @Override
    public void onPreSuperAttach(Fragment fragment) {
        mConvoFragment = (ConversationFragment) fragment;
    }

    @Override
    public void onPreSuperDetach(Fragment fragment) {
        mConvoFragment = null;
    }

    @Override
    public void init(Bundle savedInstance) {

        mConvoFragment.addTaskToQueue(new GetRemoteHistoryTask(mConvoId), Worker.REMOTE_HISTORY);
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetLocalHistoryTask) {
            // create a download worker if necessary
            Log.d(TAG, "history dl complete - thread id: " + Thread.currentThread().getId());
            for (VideoModel v : ((GetLocalHistoryTask) t).getAllConvoVideos()) {
                Log.d(TAG, v.toString());
                // mConvoFragment.addHistoryVideo(v);
            }

            if (!mConvoFragment.hasNewVideos()) {
                // mConvoFragment.startHistoryPlayback();
            }

        }

        if (t instanceof GetRemoteHistoryTask) {
            Log.d(TAG, "got remote history complete");
        }

    }

    @Override
    public void onTaskError(Task t) {

    }

    public static class GetLocalHistoryTask extends AbsTask {

        private long mConvoId;
        private List<VideoModel> mAllConvoVideos;
        private String mWhere;

        public GetLocalHistoryTask(long convoId) {
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

    public static class GetRemoteHistoryTask extends AbsTask {
        private static final String TAG = ConvoHistoryDelegate.GetRemoteHistoryTask.class.getSimpleName();
        private long mConvoId;
        boolean isDone = false;
        private ArrayList<VideoModel> mRemoteVideos;

        public GetRemoteHistoryTask(long convoId) {
            mConvoId = convoId;
        }

        @Override
        public void run() {
            // get the last 5 videos
            HBRequestManager.getHistory(mConvoId, 1, ConversationFragment.HISTORY_LIMIT, new HBSyncHttpResponseHandler<Envelope<ArrayList<VideoModel>>>(
                    new TypeReference<Envelope<ArrayList<VideoModel>>>() {
                    }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<ArrayList<VideoModel>> response) {
                    Log.d(TAG, "got remote history");
                    mIsSuccess = true;
                    mRemoteVideos = response.data;
                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    Log.d(TAG, "failed to get history");
                    mIsSuccess = false;
                }

                @Override
                public void onPostResponse() {
                    isDone = true;
                }
            });

            while (!isDone) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mIsFinished = true;

        }

    }

}
