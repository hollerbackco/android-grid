package com.moziy.hollerback.fragment.delegates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.activeandroid.query.Select;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.AbsFragmentLifecylce;
import com.moziy.hollerback.fragment.ConvoHistoryTwo;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.VideoDownloadTask;

public class ConvoLoaderDelegate extends AbsFragmentLifecylce implements Task.Listener {
    private static final String TAG = ConvoLoaderDelegate.class.getSimpleName();
    public static final String VIDEO_MODEL_INSTANCE_STATE = "VIDEO_MODEL_INSTANCE_STATE";
    public static final String CONVO_VIDEOS_INSTANCE_STATE = "CONVO_VIDEOS_INSTANCE_STATE";
    public static final String VIDEO_DL_LIST_INSTANCE_STATE = "VIDEO_DL_LIST_INSTANCE_STATE";
    private ArrayList<VideoModel> mUnwatchedVideos; // contains all new videos (ONLY NEW VIDEOS - NO HISTORY)
    private Map<String, VideoModel> mConvoVideoMap; // contains all new videos plus any video that was requested to be downloaded (NEW + HISTORY)
    private ConvoHistoryTwo mConvoFragment;
    private OnVideoModelLoaded mOnModelLoadedListener;
    private BgDownloadReceiver mReceiver;
    private ArrayList<String> mWaitingDownloadList;

    private long mConvoId;

    private interface Worker {
        public static final String MODEL = TAG + "_model_worker";
    }

    public ConvoLoaderDelegate(long convoId) {
        mConvoId = convoId;
    }

    @Override
    public void onPreSuperAttach(Fragment fragment) {
        mConvoFragment = (ConvoHistoryTwo) fragment;
    }

    @Override
    public void onPostSuperDetach(Fragment fragment) {
        mConvoFragment = null;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mReceiver = new BgDownloadReceiver();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(VIDEO_MODEL_INSTANCE_STATE)) {

                mUnwatchedVideos = (ArrayList<VideoModel>) savedInstanceState.getSerializable(VIDEO_MODEL_INSTANCE_STATE);

            }

            if (savedInstanceState.containsKey(CONVO_VIDEOS_INSTANCE_STATE)) {
                mConvoVideoMap = (HashMap<String, VideoModel>) savedInstanceState.getSerializable(CONVO_VIDEOS_INSTANCE_STATE);
            }

            if (savedInstanceState.containsKey(VIDEO_DL_LIST_INSTANCE_STATE)) {
                mWaitingDownloadList = (ArrayList<String>) savedInstanceState.getSerializable(VIDEO_DL_LIST_INSTANCE_STATE);
            }
        }

        if (mUnwatchedVideos == null) {
            mConvoFragment.addTaskToQueue(new GetUnwatchedVideoModelTask(mConvoId), Worker.MODEL);
        }

    }

    @Override
    public void onPostSuperResume(Fragment fragment) {
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.VIDEO_DOWNLOADED);
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.VIDEO_DOWNLOAD_FAILED);
    }

    @Override
    public void onPreSuperPause(Fragment fragment) {
        super.onPreSuperPause(fragment);
        if (fragment.isRemoving()) { // remove all workers

            FragmentManager fm = fragment.getFragmentManager();
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

            boolean runTxn = false;

            Fragment f = fm.findFragmentByTag(Worker.MODEL);
            // remove the model worker
            if (f != null) {
                runTxn = true;
                ft.remove(f);
            }

            if (mConvoVideoMap != null) {

                for (VideoModel v : mConvoVideoMap.values()) {
                    Log.d(TAG, "find: " + ((v.getGuid() != null) ? v.getGuid() : v.getVideoId()));
                    f = fm.findFragmentByTag((v.getGuid() != null) ? v.getGuid() : v.getVideoId());
                    if (f != null) {
                        runTxn = true;
                        Log.d(TAG, "remove it");
                        ft.remove(f);
                    }

                }
            }

            if (runTxn) {
                ft.commit();
            }

        }
    }

    @Override
    public void onPostSuperPause(Fragment fragment) {
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mUnwatchedVideos != null) {
            outState.putSerializable(VIDEO_MODEL_INSTANCE_STATE, mUnwatchedVideos);
        }

        if (mConvoVideoMap != null) {
            outState.putSerializable(CONVO_VIDEOS_INSTANCE_STATE, new HashMap<String, VideoModel>(mConvoVideoMap));
        }

        if (mWaitingDownloadList != null) {
            outState.putSerializable(VIDEO_DL_LIST_INSTANCE_STATE, mWaitingDownloadList);
        }
    }

    private boolean isAdded() {
        return (mConvoFragment == null ? false : true);
    }

    public void setOnModelLoadedListener(OnVideoModelLoaded onModelLoadedListener) {
        this.mOnModelLoadedListener = onModelLoadedListener;
    }

    @Override
    public void onTaskComplete(Task t) {

        if (t instanceof GetUnwatchedVideoModelTask && mUnwatchedVideos == null) {
            handleModelTaskComplete(t);
        }

        else if (t instanceof VideoDownloadTask) {
            // if we're waiting for the video task then handle the download, otherwise skip
            boolean allow = false;
            for (String id : mWaitingDownloadList) {
                if (id.equals(((VideoDownloadTask) t).getVideoId())) {
                    allow = true;
                    mWaitingDownloadList.remove(id);
                    break;
                }
            }

            if (allow) {
                Log.d(TAG, "handle new download " + ((VideoDownloadTask) t).getVideoId());
                handleVideoDownload((VideoDownloadTask) t);
            } else {
                Log.d(TAG, "video already handled: " + ((VideoDownloadTask) t).getVideoId());
            }
        }

    }

    @Override
    public void onTaskError(Task t) {

        if (t instanceof GetUnwatchedVideoModelTask) {
            mOnModelLoadedListener.onVideoModelLoaded(new ArrayList<VideoModel>()); // add an empty array list
        }

        if (t instanceof VideoDownloadTask) {
            Log.w(TAG, "there was an error downloading the video");
            mOnModelLoadedListener.onVideoDownloadFailed(mConvoVideoMap.get(((VideoDownloadTask) t).getVideoId()));
        }
    }

    private void handleModelTaskComplete(Task t) {

        Log.d(TAG, "" + this);

        mUnwatchedVideos = new ArrayList<VideoModel>(((GetUnwatchedVideoModelTask) t).getAllConvoVideos());
        Log.d(TAG, "total unread videos found: " + mUnwatchedVideos.size());

        mConvoVideoMap = new HashMap<String, VideoModel>();

        for (VideoModel video : mUnwatchedVideos) {

            mConvoVideoMap.put(video.getGuid(), video);

        }

        mOnModelLoadedListener.onVideoModelLoaded(mUnwatchedVideos); // notify

        mWaitingDownloadList = new ArrayList<String>();

        // add the workers to download our videos
        for (VideoModel video : ((GetUnwatchedVideoModelTask) t).getVideosForDownload()) {
            if (addDownloadWorkerFor(video)) {
                Log.d(TAG, "add to dl list: " + video.getVideoId());
                mWaitingDownloadList.add(video.getVideoId());
            }
        }

        Log.d(TAG, "active android task completed");
    }

    private boolean addDownloadWorkerFor(VideoModel video) {

        VideoDownloadTask downloadTask = new VideoDownloadTask(video); // download the video
        boolean added = mConvoFragment.addTaskToQueue(downloadTask, video.getGuid());
        if (!added) { // couldn't create the download worker, so lets clear the state
            Log.d(TAG, "not adding download worker for: " + video.toString());
            return false;
        } else {
            return true;
        }

    }

    public boolean requestDownload(VideoModel video) {
        if (isAdded()) {
            // add the video to the list of videos
            mConvoVideoMap.put(video.getGuid(), video);
            boolean added = addDownloadWorkerFor(video);
            if (added) {
                mWaitingDownloadList.add(video.getVideoId());
            }

            return added;
        }

        return false;
    }

    private void handleVideoDownload(VideoDownloadTask t) {

        Log.d(TAG, "video download task completed");

        VideoModel video = mConvoVideoMap.get(((VideoDownloadTask) t).getVideoId());
        Log.d(TAG, "downloaded video with id: " + video.getGuid());

        video.setState(VideoModel.ResourceState.ON_DISK); // even though the download task sets it, but this copy doesn't have the state set

        mOnModelLoadedListener.onVideoDownloaded(video);

    }

    public interface OnVideoModelLoaded {

        public void onVideoModelLoaded(ArrayList<VideoModel> videos);

        public void onVideoDownloaded(VideoModel video);

        public void onVideoDownloadFailed(VideoModel video);
    }

    /**
     * This class listens to the background download service for updates on
     * downloads or failures of video
     * @author sajjad
     *
     */
    private class BgDownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String guid = intent.getStringExtra(IABIntent.PARAM_ID);

            if (intent.getAction().equals(IABIntent.VIDEO_DOWNLOADED) && guid != null) {

                Log.d(TAG, "we got a video download broadcast while watching");

                // if the video was downloaded, lets update the videomap
                if (mConvoVideoMap.containsKey(guid)) {

                    VideoModel video = mConvoVideoMap.get(guid);
                    Log.d(TAG, "video: " + video.getGuid() + " state: " + video.getState());

                    mOnModelLoadedListener.onVideoDownloaded(video);

                }

            } else if (intent.getAction().equals(IABIntent.VIDEO_DOWNLOAD_FAILED)) {

                Log.d(TAG, "we got a video download failed broadcast while watching");

                // lets attempt to recover by creating an on demand downloader
                if (mConvoVideoMap.containsKey(guid)) {

                    VideoModel video = mConvoVideoMap.get(guid);

                    // lets see if there's already a worker assigned to download this video
                    Fragment f = mConvoFragment.getFragmentManager().findFragmentByTag(guid);
                    if (f == null) { // if there isn't one
                        Log.d(TAG, "adding new worker for: " + video.getGuid());
                        addDownloadWorkerFor(video);
                    }
                }

            }

        }

    }

    public static class GetUnwatchedVideoModelTask extends AbsTask {

        private long mConvoId;
        private List<VideoModel> mAllConvoVideos;
        private List<VideoModel> mVideosForDownload;
        private String mWhere;

        public GetUnwatchedVideoModelTask(long convoId) {
            mConvoId = convoId;
            mWhere = ActiveRecordFields.C_VID_CONV_ID + "=" + mConvoId + " AND " + ActiveRecordFields.C_VID_ISREAD + "=0";
        }

        @Override
        public void run() {

            mAllConvoVideos = new Select()//
                    .from(VideoModel.class) //
                    .where(mWhere).orderBy("strftime('%s'," + ActiveRecordFields.C_VID_CREATED_AT + ")").execute();

            // get the videos that we wish to download and set them as transacting
            mVideosForDownload = VideoHelper.getVideosForTransaction(mWhere + " AND " + ActiveRecordFields.C_VID_STATE + "='" + VideoModel.ResourceState.PENDING_DOWNLOAD + "'");

        }

        public List<VideoModel> getAllConvoVideos() {
            return mAllConvoVideos;
        }

        public List<VideoModel> getVideosForDownload() {
            return mVideosForDownload;
        }

    }

}
