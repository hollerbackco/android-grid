package com.moziy.hollerback.fragment.delegates;

import java.util.ArrayList;
import java.util.Iterator;
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
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.Task;

/**
 * This class acts as a delegate to the conversation fragment and manages many history related operations
 * 
 * @author sajjad
 *
 */
public class ConvoHistoryDelegate extends AbsFragmentLifecylce implements Task.Listener {
    private static final String TAG = ConvoHistoryDelegate.class.getSimpleName();
    private static final int HISTORY_LIMIT = 5;
    private ConversationFragment mConvoFragment;
    private List<VideoModel> mLocalHistory;
    private ArrayList<VideoModel> mRemoteHistory;
    private long mConvoId;
    private OnHistoryUpdateListener mOnHistoryModelLoaded;
    private ConvoLoaderDelegate mLoaderDelegate;

    private interface Worker {
        public static final String LOCAL_HISTORY = "local_history";
        public static final String REMOTE_HISTORY = "remote_history";
    }

    public ConvoHistoryDelegate(long convoId, ConvoLoaderDelegate loaderDelegate) {
        mConvoId = convoId;
        mLoaderDelegate = loaderDelegate;
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

        mConvoFragment.addTaskToQueue(new GetLocalHistoryTask(mConvoId), Worker.LOCAL_HISTORY);
        mConvoFragment.addTaskToQueue(new GetRemoteHistoryTask(mConvoId), Worker.REMOTE_HISTORY);
    }

    public void setOnHistoryVideoDownloadListener(OnHistoryUpdateListener listener) {
        mOnHistoryModelLoaded = listener;
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetLocalHistoryTask) {
            // create a download worker if necessary
            Log.d(TAG, "local history load complete - thread id: " + Thread.currentThread().getId());
            mLocalHistory = ((GetLocalHistoryTask) t).getAllConvoVideos();
            for (VideoModel v : mLocalHistory) {
                if (v.getVideoId() == null) {
                    v.setVideoId(v.getGuid());
                }
                Log.d(TAG, "Local video: " + v.toString());
                if (mOnHistoryModelLoaded != null) {
                    mOnHistoryModelLoaded.onHistoryModelLoaded(v);
                } else {
                    throw new IllegalStateException("must set listener for history");
                }
            }

            mOnHistoryModelLoaded.onLocalHistoryLoaded(mLocalHistory);

        }

        if (t instanceof GetRemoteHistoryTask) {
            Log.d(TAG, "got remote history complete: " + ((GetRemoteHistoryTask) t).getRemoteVideos().size());
            mRemoteHistory = ((GetRemoteHistoryTask) t).getRemoteVideos();
            // mRemoteHistory.removeAll(mLocalHistory); // remove duplicates

            for (VideoModel localVideo : mLocalHistory) {
                Iterator<VideoModel> itr = mRemoteHistory.iterator();
                while (itr.hasNext()) {
                    if (localVideo.getVideoId().equals(itr.next().getVideoId())) {
                        itr.remove();
                        break;
                    }
                }
            }
            Log.d(TAG, "new remote history: " + mRemoteHistory.size());

            // now for all the remote history that is not on disk download them
            for (VideoModel v : mRemoteHistory) {
                if (v.getGuid() == null)
                    v.setGuid(v.getVideoId());
                v.setWatchedState(VideoModel.ResourceState.WATCHED_AND_POSTED);
                v.setState(ResourceState.PENDING_DOWNLOAD);
                v.setTransacting(); // set transacting because we are about to request a download
                v.save();
                Log.d(TAG, "remote video: " + v.toString());
                if (mOnHistoryModelLoaded != null)
                    mOnHistoryModelLoaded.onHistoryModelLoaded(v);
                mLoaderDelegate.requestDownload(v);
            }

            mOnHistoryModelLoaded.onRemoteHistoryLoaded(mRemoteHistory);
        }

    }

    @Override
    public void onTaskError(Task t) {
        if (t instanceof GetLocalHistoryTask) {
            mOnHistoryModelLoaded.onLocalHistoryFailed();
        }
        if (t instanceof GetRemoteHistoryTask) {
            mOnHistoryModelLoaded.onRemoteHistoryFailed();
        }

    }

    private static class GetLocalHistoryTask extends AbsTask {

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

    }

    private static class GetRemoteHistoryTask extends AbsTask {
        private static final String TAG = ConvoHistoryDelegate.GetRemoteHistoryTask.class.getSimpleName();
        private long mConvoId;
        boolean isDone = false;
        private ArrayList<VideoModel> mRemoteVideos;

        public GetRemoteHistoryTask(long convoId) {
            mConvoId = convoId;
        }

        public ArrayList<VideoModel> getRemoteVideos() {
            return mRemoteVideos;
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
                    mRemoteVideos = response.data;
                    mIsSuccess = true;
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

            // we have remote videos:

            // now see if we have the remote videos in our database
            if (mIsSuccess && mRemoteVideos != null) {
                Iterator<VideoModel> itr = mRemoteVideos.iterator();
                while (itr.hasNext()) {
                    VideoModel remote = itr.next();
                    VideoModel local = new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_GUID + "=?", remote.getVideoId()).executeSingle();
                    if (local != null) {
                        Log.d(TAG, "removing video already in local db: " + remote.toString());
                        itr.remove();
                    }
                }

            }

            mIsFinished = true;

        }
    }

    public static interface OnHistoryUpdateListener {
        // TODO: put no local history and no remote history listenrs
        // public void

        public void onHistoryModelLoaded(VideoModel video);

        public void onLocalHistoryLoaded(List<VideoModel> videos);

        public void onLocalHistoryFailed();

        public void onRemoteHistoryLoaded(List<VideoModel> videos);

        public void onRemoteHistoryFailed();
    }

}
