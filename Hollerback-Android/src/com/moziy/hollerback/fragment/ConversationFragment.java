package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker.TaskClient;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.TimeUtil;

public class ConversationFragment extends SherlockFragment implements TaskClient, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, RecordingInfo {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";
    public static final String CONVO_ID_INSTANCE_STATE = "CONVO_ID_INSTANCE_STATE";
    public static final String VIDEO_MODEL_INSTANCE_STATE = "VIDEO_MODEL_INSTANCE_STATE";
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";
    public static final String TASK_QUEUE_INSTANCE_STATE = "TASK_QUEUE_INSTANCE_STATE";
    public static final String PLAYING_INSTANCE_STATE = "PLAYING_INSTANCE_STATE";
    public static final String RECORDING_INFO_INSTANCE_STATE = "RECORDING_INFO_INSTANCE_STATE";

    public static ConversationFragment newInstance(long conversationId) {
        ConversationFragment c = new ConversationFragment();
        Bundle args = new Bundle();
        args.putLong(CONVO_ID_BUNDLE_ARG_KEY, conversationId);
        c.setArguments(args);
        return c;
    }

    private long mConvoId;
    private ArrayList<VideoModel> mVideos;
    private Map<String, VideoModel> mVideoMap;
    private LinkedList<VideoModel> mPlayBackQueue; // the queue used for playback
    private LinkedList<Task> mTaskQueue; // queue of tasks such as fetching the model and fetching the videos
    private VideoView mVideoView; // the video view

    private boolean mPlayingDuringConfigChange;
    private boolean mPausedDuringPlayback; // not saved
    private int mPosition = 0;
    private Bundle mRecordingInfo;

    private BgDownloadReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
        mReceiver = new BgDownloadReceiver();

        if (savedInstanceState != null) { // TODO: we probably don't need to worry about this because setRetainInstance is set to true
            Log.d(TAG, "restoring instance");

            // restore the video model if, see what the status of the resource is, and also if we're in the middle of playback
            if (savedInstanceState.containsKey(VIDEO_MODEL_INSTANCE_STATE)) {

                mVideos = (ArrayList<VideoModel>) savedInstanceState.getSerializable(VIDEO_MODEL_INSTANCE_STATE);

                // check to see if any of the videos have or been downloaded
                mVideoMap = new HashMap<String, VideoModel>();
                for (VideoModel video : mVideos) {
                    mVideoMap.put(video.getGuid(), video);

                }
            }

            if (savedInstanceState.containsKey(PLAYBACK_QUEUE_INSTANCE_STATE)) {
                mPlayBackQueue = (LinkedList<VideoModel>) savedInstanceState.getSerializable(PLAYBACK_QUEUE_INSTANCE_STATE);
            }

            if (savedInstanceState.containsKey(TASK_QUEUE_INSTANCE_STATE)) {
                mTaskQueue = (LinkedList<Task>) savedInstanceState.getSerializable(TASK_QUEUE_INSTANCE_STATE);
            }

            if (savedInstanceState.containsKey(RECORDING_INFO_INSTANCE_STATE)) {
                mRecordingInfo = savedInstanceState.getBundle(RECORDING_INFO_INSTANCE_STATE);
            }

        }

        // Start work on getting the list of unseen videos for this conversation
        Fragment worker;
        if (mVideos == null && (worker = getFragmentManager().findFragmentByTag(TAG + "model_worker")) == null) { // we check the model and the worker because the worker removes itself once work is

            mTaskQueue = new LinkedList<Task>();// done

            mTaskQueue.add(new ActiveAndroidTask<VideoModel>( //
                    new Select()//
                            .from(VideoModel.class) //
                            .where(ActiveRecordFields.C_VID_CONV_ID + "=? AND " + ActiveRecordFields.C_VID_ISREAD + "=?", mConvoId, 0))); //

            // figure out how many tasks we need to create
            worker = new AbsTaskWorker() {
            };
            worker.setTargetFragment(this, 0);
            getFragmentManager().beginTransaction().add(worker, TAG + "model_worker").commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_layout, container, false);
        mVideoView = (VideoView) v.findViewById(R.id.vv_preview);

        Log.d(TAG, "onCreateView");

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            // if we were in the middle of playing, then adjust the playback elements such as seek position
            mPlayingDuringConfigChange = savedInstanceState.getBoolean(PLAYING_INSTANCE_STATE);
            if (mPlayingDuringConfigChange) {
                playVideo(mPlayBackQueue.peek());
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRecordingInfo != null) { // so we got our result from the recording fragment, time to go back
            getFragmentManager().popBackStack();
            return;
        }

        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.VIDEO_DOWNLOADED);
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.VIDEO_DOWNLOAD_FAILED);

        if (mPausedDuringPlayback) { // reset the flag
            playVideo(mPlayBackQueue.peek());
        }
    }

    @Override
    public void onPause() {

        // allow for config changes while we were paused{

        mPausedDuringPlayback = mVideoView.isPlaying();
        Log.d(TAG, "onPause - currentPosition: " + mPosition);
        if (mPausedDuringPlayback)
            mVideoView.stopPlayback();

        super.onPause();

        IABroadcastManager.unregisterLocalReceiver(mReceiver);
        IABroadcastManager.unregisterLocalReceiver(mReceiver);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(CONVO_ID_INSTANCE_STATE, mConvoId);

        if (mVideos != null) {
            outState.putSerializable(VIDEO_MODEL_INSTANCE_STATE, mVideos);
        }

        // TODO: Save the video seek position, the video file that is being played so that once restored we can begin playing
        if (mPlayBackQueue != null) {
            outState.putSerializable(PLAYBACK_QUEUE_INSTANCE_STATE, mPlayBackQueue);
        }

        if (mTaskQueue != null) {
            outState.putSerializable(TASK_QUEUE_INSTANCE_STATE, mTaskQueue);
        }

        if (mPlayingDuringConfigChange || mPausedDuringPlayback) {
            mPlayingDuringConfigChange = true;
        }

        if (mRecordingInfo != null) {
            outState.putBundle(RECORDING_INFO_INSTANCE_STATE, mRecordingInfo);
        }

        outState.putBoolean(PLAYING_INSTANCE_STATE, mPlayingDuringConfigChange);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTaskComplete(Task t) {

        if (t instanceof ActiveAndroidTask) {

            handleModelTaskComplete(t);

        } else if (t instanceof VideoDownloadTask) {

            handleVideoDownload((VideoDownloadTask) t);
        }

    }

    @Override
    public void onTaskError(Task t) {

        Log.d(TAG, "there was a problem with a task");
        // TODO: handle this later
        if (isAdded())
            Toast.makeText(getActivity(), "Couldn't download video..", Toast.LENGTH_SHORT).show();
        // retry the task

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    private void handleModelTaskComplete(Task t) {

        mVideos = new ArrayList<VideoModel>(((ActiveAndroidTask<VideoModel>) t).getResults());
        Log.d(TAG, "total unread videos found: " + mVideos.size());

        mVideoMap = new HashMap<String, VideoModel>();
        mPlayBackQueue = new LinkedList<VideoModel>();

        for (VideoModel video : mVideos) {

            mVideoMap.put(video.getGuid(), video);
            // add the videos to the playback queue
            mPlayBackQueue.add(video);

        }

        for (VideoModel video : mVideos) {

            Log.d(TAG, "processing video with state: " + video.toString());
            if (VideoModel.ResourceState.PENDING_DOWNLOAD.equals(video.getState()) && !video.isTransacting()) { // create a download task for all non transacting videos

                addDownloadWorkerFor(video);

            } else if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {

                // if we've already been downloaded and we're on the first of the playback queue, then begin playback
                if (mPlayBackQueue.peek().getGuid().equals(video.getGuid())) {
                    playVideo(video);
                }
            }
        }

        Log.d(TAG, "active android task completed");
    }

    private void handleVideoDownload(VideoDownloadTask t) {

        Log.d(TAG, "video download task completed");

        VideoModel video = mVideoMap.get(((VideoDownloadTask) t).getVideoId());
        Log.d(TAG, "downloaded video with id: " + video.getGuid());

        video.setState(VideoModel.ResourceState.ON_DISK); // even though the download task sets it, but this copy doesn't have the state set

        // check to see if this is the next video that must be played
        VideoModel queuedVideo = mPlayBackQueue.peek();

        if (queuedVideo.getGuid().equals(video.getGuid())) { // if the queued is the one that just got downloaded then just play

            Log.d(TAG, "playing back video that was just downloaded");
            playVideo(video);

        }

    }

    private void addDownloadWorkerFor(VideoModel video) {
        // for the number of videos, lets create two workers, to download video alternately
        AbsTaskWorker worker = AbsTaskWorker.newInstance(true); // all videos will be downloaded sequentially

        VideoDownloadTask downloadTask = new VideoDownloadTask(video); // download the video
        mTaskQueue.add(downloadTask); // the worker fragment will automatically call the task listener of the fragment

        worker.setTargetFragment(this, 0);// lets create an S3 task and ask our worker to run it
        getFragmentManager().beginTransaction().add(worker, video.getGuid()).commit();
    }

    private void playVideo(VideoModel v) {
        Log.d(TAG, "starting playback of: " + v.getGuid());
        mVideoView.setOnPreparedListener(this);
        mVideoView.setVideoURI(Uri.fromFile(HBFileUtil.getOutputVideoFile(v)));
        mVideoView.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.d(TAG, "video playback complete");
        mp.reset();

        // once the playback is complete, lets see if the next one is ready
        VideoModel video = mPlayBackQueue.poll(); // remove the one that just finished

        setVideoSeen(video);

        // delete the video from the sdcard?

        if (!mPlayBackQueue.isEmpty()) {

            Log.d(TAG, "playback after completion and queue is not empty");
            video = mPlayBackQueue.peek();

            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                Log.d(TAG, "starting to play video after completion");
                playVideo(video);
            }

        } else {

            // also broadcast that the conversation has been updated
            beginRecording();

        }

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // only play if we're in the resumed state
        Log.d(TAG, "onPrepared()");

        if (isResumed()) {
            mVideoView.start();
        } else {
            Log.d(TAG, "not playing because not in resumed state");
        }

    }

    private void setVideoSeen(VideoModel video) {

        ActiveAndroidTask<VideoModel> updateVideoTask = new ActiveAndroidTask<VideoModel>(new Select().from(VideoModel.class).where("Id = ?", video.getId()));
        updateVideoTask.setTaskListener(new Task.Listener() {

            @Override
            public void onTaskError(Task t) {

                // if we couldn't write to the db..?
                Log.w(TAG, "error updating database after watching video ");

            }

            @Override
            public void onTaskComplete(Task t) {

                VideoModel video = ((ActiveAndroidTask<VideoModel>) t).getResults().get(0); // must be valid!
                Log.d(TAG, "fetching latest from db: " + video.toString());

                video.setRead(true); // mark the video as watched
                video.setWatchedState(VideoModel.ResourceState.WATCHED_PENDING_POST);
                video.save();

                // TODO - Sajjad: Create a service to go and remove the watched videos
                ConversationModel c = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId).executeSingle();
                Log.d(TAG, "new unread count: " + (c.getUnreadCount() - 1));
                c.setUnreadCount(c.getUnreadCount() - 1);
                c.save(); // save that we've unread

                // broadcast that the conversations have changed
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));
            }
        });

        TaskExecuter executer = new TaskExecuter(true); // allow only 1 update at a time
        executer.executeTask(updateVideoTask);
    }

    @Override
    public void onRecordingFinished(Bundle info) {
        mRecordingInfo = info;

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
        new TaskExecuter().executeTask(t);
    }

    private void beginRecording() {

        // we're ready to move to the recording fragment
        RecordVideoFragment f = RecordVideoFragment.newInstance(mConvoId, "Muhahahaha", new ArrayList<String>());
        f.setTargetFragment(this, 0);
        getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).commitAllowingStateLoss();
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
                if (mVideoMap.containsKey(guid)) {

                    VideoModel video = mVideoMap.get(guid);
                    Log.d(TAG, "video: " + video.getGuid() + " state: " + video.getState());

                    // if this happens to be the video that we should be playing next, then lets play it
                    if (mPlayBackQueue.peek() != null && mPlayBackQueue.peek().getGuid().equals(video.getGuid())) {
                        playVideo(video);
                    }

                }

            } else if (intent.getAction().equals(IABIntent.VIDEO_DOWNLOAD_FAILED)) {

                Log.d(TAG, "we got a video download failed broadcast while watching");

                // lets attempt to recover by creating an on demand downloader
                if (mVideoMap.containsKey(guid)) {

                    VideoModel video = mVideoMap.get(guid);

                    // lets see if there's already a worker assigned to download this video
                    Fragment f = getFragmentManager().findFragmentByTag(guid);
                    if (f == null) { // if there isn't one
                        Log.d(TAG, "adding new worker for: " + video.getGuid());
                        addDownloadWorkerFor(video);
                    }
                }

            }

        }
    }

}
