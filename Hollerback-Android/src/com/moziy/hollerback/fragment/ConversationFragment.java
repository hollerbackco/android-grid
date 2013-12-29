package com.moziy.hollerback.fragment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate;
import com.moziy.hollerback.fragment.delegates.VideoPlayerDelegate;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.helper.VideoHelper;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.util.date.TimeUtil;

public class ConversationFragment extends SherlockFragment implements TaskClient, RecordingInfo {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";
    public static final String CONVO_ID_INSTANCE_STATE = "CONVO_ID_INSTANCE_STATE";
    public static final String VIDEO_MODEL_INSTANCE_STATE = "VIDEO_MODEL_INSTANCE_STATE";
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";
    public static final String TASK_QUEUE_INSTANCE_STATE = "TASK_QUEUE_INSTANCE_STATE";
    public static final String PLAYING_INSTANCE_STATE = "PLAYING_INSTANCE_STATE";
    public static final String RECORDING_INFO_INSTANCE_STATE = "RECORDING_INFO_INSTANCE_STATE";
    public static final String PLAYBACK_INDEX_INSTANCE_STATE = "PLAYBACK_INDEX_INSTANCE_STATE";

    public static ConversationFragment newInstance(long conversationId) {
        ConversationFragment c = new ConversationFragment();
        Bundle args = new Bundle();
        args.putLong(CONVO_ID_BUNDLE_ARG_KEY, conversationId);
        c.setArguments(args);
        return c;
    }

    public static final int HISTORY_LIMIT = 5;

    private long mConvoId;
    private LinkedList<VideoModel> mPlayBackQueue; // the queue used for playback
    private LinkedList<Task> mTaskQueue; // queue of tasks such as fetching the model and fetching the videos
    private VideoView mVideoView; // the video view

    private Bundle mRecordingInfo;
    private ProgressBar mProgress;

    private ConvoHistoryDelegate mHistoryDelegate;
    private boolean mHasNew;

    private ConvoLoaderDelegate mConvoDelegate;
    private VideoPlayerDelegate mVideoPlayerDelegate;

    // navigation buttons
    private ImageButton mSkipForwardBt;
    private ImageButton mSkipBackwardBt;

    @Override
    public void onAttach(Activity activity) {

        if (mConvoDelegate == null) {
            mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
            mConvoDelegate = new ConvoLoaderDelegate(mConvoId);
            mVideoPlayerDelegate = new VideoPlayerDelegate(mConvoId);
            mConvoDelegate.setOnModelLoadedListener(mVideoPlayerDelegate);
        }

        if (mPlayBackQueue == null) {
            mPlayBackQueue = new LinkedList<VideoModel>();
        }

        if (mHistoryDelegate == null) {
            mHistoryDelegate = new ConvoHistoryDelegate();
            mHistoryDelegate.onAttach(this);
        }

        mVideoPlayerDelegate.onPreSuperAttach(this);
        mConvoDelegate.onPreSuperAttach(this);
        super.onAttach(activity);
        mVideoPlayerDelegate.onPostSuperAttach(this);
        mConvoDelegate.onPostSuperAttach(this);

    }

    @Override
    public void onDetach() {
        mHistoryDelegate.onDetach();
        mConvoDelegate.onPreSuperDetach(this);
        super.onDetach();
        mConvoDelegate.onPostSuperDetach(this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);

        // mPlaybackIndex = -1;

        if (savedInstanceState != null) { // TODO: we probably don't need to worry about this because setRetainInstance is set to true
            Log.d(TAG, "restoring instance");

            if (savedInstanceState.containsKey(TASK_QUEUE_INSTANCE_STATE)) {
                mTaskQueue = (LinkedList<Task>) savedInstanceState.getSerializable(TASK_QUEUE_INSTANCE_STATE);
            }

            if (savedInstanceState.containsKey(RECORDING_INFO_INSTANCE_STATE)) {
                mRecordingInfo = savedInstanceState.getBundle(RECORDING_INFO_INSTANCE_STATE);
            }

        }

        if (mTaskQueue == null) {
            mTaskQueue = new LinkedList<Task>();
        }

        mVideoPlayerDelegate.init(savedInstanceState);
        mConvoDelegate.init(savedInstanceState);

    }

    /**
     * Add a task to the queue with an executer
     * @param t
     * @param workerName
     * @return
     */
    public boolean addTaskToQueue(Task t, String workerName) {
        Fragment f = getFragmentManager().findFragmentByTag(workerName);
        if (f == null) {
            mTaskQueue.add(t);
            FragmentTaskWorker worker = FragmentTaskWorker.newInstance(true);
            worker.setTargetFragment(this, 0);
            getFragmentManager().beginTransaction().add(worker, workerName).commit();

            return true;
        }
        return false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_layout, container, false);
        mVideoView = (VideoView) v.findViewById(R.id.vv_preview);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mProgress.setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView");

        mVideoPlayerDelegate.setVideoView(mVideoView);
        mVideoPlayerDelegate.setProgressView(mProgress);

        mSkipBackwardBt = (ImageButton) v.findViewById(R.id.ib_skip_backward);
        // mSkipBackwardBt.setVisibility(View.GONE);
        mSkipBackwardBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // if (mPlaybackIndex > 0)
                // playLastVideo();

            }
        });

        mSkipForwardBt = (ImageButton) v.findViewById(R.id.ib_skip_forward);
        // mSkipForwardBt.setVisibility(View.GONE);
        mSkipForwardBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // if (mPlaybackIndex < mPlayBackQueue.size() - 1)
                // playNextVideo();

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mVideoPlayerDelegate.onPreSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPreSuperActivityCreated(savedInstanceState);

        super.onActivityCreated(savedInstanceState);

        mVideoPlayerDelegate.onPostSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPostSuperActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {

        mVideoPlayerDelegate.onPreSuperResume(this);
        mConvoDelegate.onPreSuperResume(this);

        super.onResume();

        if (mRecordingInfo != null) { // so we got our result from the recording fragment, time to go back
            getFragmentManager().popBackStack();
            return;
        }

        mVideoPlayerDelegate.onPostSuperResume(this);
        mConvoDelegate.onPostSuperResume(this);
    }

    @Override
    public void onPause() {

        mVideoPlayerDelegate.onPreSuperPause(this);
        mConvoDelegate.onPreSuperPause(this);
        super.onPause();
        mVideoPlayerDelegate.onPostSuperPause(this);
        mConvoDelegate.onPostSuperPause(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(CONVO_ID_INSTANCE_STATE, mConvoId);

        if (mTaskQueue != null) {
            outState.putSerializable(TASK_QUEUE_INSTANCE_STATE, mTaskQueue);
        }

        if (mRecordingInfo != null) {
            outState.putBundle(RECORDING_INFO_INSTANCE_STATE, mRecordingInfo);
        }

        mVideoPlayerDelegate.onSaveInstanceState(outState);
        mConvoDelegate.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTaskComplete(Task t) {
        mConvoDelegate.onTaskComplete(t);

        // if (t instanceof GetVideoModelTask) {
        //
        // handleModelTaskComplete(t);
        //
        // } else if (t instanceof VideoDownloadTask) {
        //
        // handleVideoDownload((VideoDownloadTask) t);
        // }
        //
        // mHistoryDelegate.onTaskComplete(t);

    }

    @Override
    public void onTaskError(Task t) {

        Log.d(TAG, "there was a problem with a task");
        mConvoDelegate.onTaskError(t);
        // TODO: handle this later
        if (isAdded())
            Toast.makeText(getActivity(), "Couldn't download video..", Toast.LENGTH_SHORT).show();
        // retry the task
        mHistoryDelegate.onTaskError(t);

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    public LinkedList<Task> getTaskQueue() {
        return mTaskQueue;
    }

    // public void addHistoryVideo(VideoModel video) {
    // mPlayBackQueue.add(0, video);
    // ++mPlaybackIndex; // update the playback index
    // }

    // private void playLastVideo() {
    // --mPlaybackIndex;
    // if (mVideoView.isPlaying()) {
    // mVideoView.stopPlayback();
    // }
    //
    // playVideo(mPlayBackQueue.get(mPlaybackIndex));
    //
    // }
    //
    // private void playNextVideo() {
    // ++mPlaybackIndex;
    // if (mVideoView.isPlaying()) {
    // mVideoView.stopPlayback();
    // }
    //
    // playVideo(mPlayBackQueue.get(mPlaybackIndex));
    //
    // }

    // public void startHistoryPlayback() {
    // mPlaybackIndex = mPlayBackQueue.size() - 1;
    // mProgress.setVisibility(View.INVISIBLE);
    // playVideo(mPlayBackQueue.get(mPlaybackIndex)); // get the last element
    // }

    public boolean hasNewVideos() {
        return mHasNew;
    }

    // @Override
    // public void onCompletion(MediaPlayer mp) {
    //
    // Log.d(TAG, "video playback complete");
    // mp.reset();
    //
    // // once the playback is complete, lets see if the next one is ready
    // VideoModel video = mPlayBackQueue.get(mPlaybackIndex); // remove the one that just finished
    // ++mPlaybackIndex; // increate the playback index;
    //
    // setVideoSeen(video);
    //
    // // delete the video from the sdcard?
    //
    // if (mPlaybackIndex < mPlayBackQueue.size()) {
    //
    // Log.d(TAG, "playback after completion and queue is not empty");
    // video = mPlayBackQueue.get(mPlaybackIndex);
    //
    // if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
    // Log.d(TAG, "starting to play video after completion");
    // playVideo(video);
    // }
    //
    // } else {
    //
    // boolean isShown = PreferenceManagerUtil.getPreferenceValue(HBPreferences.SHOWN_START_RECORDING_DIALOG, false);
    // if (!isShown && isAdded()) {
    // AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // builder.setTitle(getString(R.string.start_recording_dialog_title));
    // builder.setMessage(getString(R.string.start_recording_dialog_message));
    // builder.setCancelable(false);
    // builder.setPositiveButton(getString(R.string.start_recording_dialog_button), new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // PreferenceManagerUtil.setPreferenceValue(HBPreferences.SHOWN_START_RECORDING_DIALOG, true);
    //
    // // also broadcast that the conversation has been updated
    // beginRecording();
    //
    // }
    // });
    // builder.show();
    // } else {
    // beginRecording();
    // }
    //
    // }
    //
    // }
    //
    // @Override
    // public void onPrepared(MediaPlayer mp) {
    //
    // // only play if we're in the resumed state
    // Log.d(TAG, "onPrepared()");
    //
    // if (isResumed()) {
    // mVideoView.start();
    // } else {
    // Log.d(TAG, "not playing because not in resumed state");
    // }
    //
    // }

    // private void setVideoSeen(VideoModel video) {
    //
    // ActiveAndroidTask<VideoModel> updateVideoTask = new ActiveAndroidTask<VideoModel>(new Select().from(VideoModel.class).where("Id = ?", video.getId()));
    // updateVideoTask.setTaskListener(new Task.Listener() {
    //
    // @Override
    // public void onTaskError(Task t) {
    //
    // // if we couldn't write to the db..?
    // Log.w(TAG, "error updating database after watching video ");
    //
    // }
    //
    // @Override
    // public void onTaskComplete(Task t) {
    //
    // ActiveAndroid.beginTransaction();
    // try {
    // VideoModel video = ((ActiveAndroidTask<VideoModel>) t).getResults().get(0); // must be valid!
    // Log.d(TAG, "fetching latest from db: " + video.toString());
    //
    // synchronized (ConversationModel.class) { // very important synchronization with the sync service
    //
    // video.setRead(true); // mark the video as watched
    // video.setWatchedState(VideoModel.ResourceState.WATCHED_PENDING_POST);
    // video.save();
    //
    // // TODO - Sajjad: Create a service to go and remove the watched videos
    // if (mConversation == null) {
    // mConversation = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId).executeSingle();
    // }
    // Log.d(TAG, "new unread count: " + (mConversation.getUnreadCount() - 1));
    // mConversation.setUnreadCount(mConversation.getUnreadCount() - 1);
    // mConversation.save(); // save that we've unread
    // }
    // ActiveAndroid.setTransactionSuccessful();
    //
    // } finally {
    // ActiveAndroid.endTransaction();
    // }
    //
    // // broadcast that the conversations have changed
    // IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));
    // }
    // });
    //
    // TaskExecuter executer = new TaskExecuter(true); // allow only 1 update at a time
    // executer.executeTask(updateVideoTask);
    // }

    @Override
    public void onRecordingFinished(Bundle info) {
        mRecordingInfo = info;

        if (!info.getBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, true)) {
            return;
        }

        // if the recording was successfull, then update the conversation, and set the last message time
        Task t = new ActiveAndroidUpdateTask(new Update(ConversationModel.class) //
                .set(ActiveRecordFields.C_CONV_LAST_MESSAGE_AT + "='" + TimeUtil.SERVER_TIME_FORMAT.format(new Date()) + "'") //
                .where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId));
        t.setTaskListener(new Task.Listener() {

            @Override
            public void onTaskError(Task t) {
                Log.w(TAG, "error updating database in case of recording finished");
            }

            @Override
            public void onTaskComplete(Task t) {
                Log.d(TAG, "updated the conversation");
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));
            }
        });

        Context c = HollerbackApplication.getInstance();
        Toast.makeText(c, c.getString(R.string.message_sent_simple), Toast.LENGTH_LONG).show();

        // UPDATE: this is being done in RecordVideoFragment.updateConversationTime
        // new TaskExecuter().executeTask(t);
    }

    public static class GetVideoModelTask extends AbsTask {

        private long mConvoId;
        private List<VideoModel> mAllConvoVideos;
        private List<VideoModel> mVideosForDownload;
        private String mWhere;

        public GetVideoModelTask(long convoId) {
            mConvoId = convoId;
            mWhere = ActiveRecordFields.C_VID_CONV_ID + "=" + mConvoId + " AND " + ActiveRecordFields.C_VID_ISREAD + "=0";
        }

        @Override
        public void run() {

            mAllConvoVideos = new Select()//
                    .from(VideoModel.class) //
                    .where(mWhere).execute();

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

    /**
     * A class used to differentiate a video download and a history video download
     * @author sajjad
     *
     */
    public static class HistoryVideoDownloadTask extends VideoDownloadTask {

        public HistoryVideoDownloadTask(VideoModel model) {
            super(model);
        }

    }

}
