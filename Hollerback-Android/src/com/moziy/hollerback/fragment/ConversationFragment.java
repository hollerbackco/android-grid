package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Select;
import com.moziy.hollerback.R;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker.TaskClient;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.util.HBFileUtil;

public class ConversationFragment extends SherlockFragment implements TaskClient, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";
    public static final String CONVO_ID_INSTANCE_STATE = "CONVO_ID_INSTANCE_STATE";
    public static final String VIDEO_MODEL_INSTANCE_STATE = "VIDEO_MODEL_INSTANCE_STATE";
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";

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
    private Queue<Task> mTaskQueue; // queue of tasks such as fetching the model and fetching the videos
    private VideoView mVideoView; // the video view

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
        mTaskQueue = new LinkedList<Task>();

        if (savedInstanceState != null) {
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

            // if we were in the middle of playing, then adjust the playback elements such as seek position
        }

        // Start work on getting the list of unseen videos for this conversation
        Fragment worker;
        if (mVideos == null && (worker = getFragmentManager().findFragmentByTag(TAG + "model_worker")) == null) {
            mTaskQueue.add(new ActiveAndroidTask<VideoModel>(new Select().from(VideoModel.class).where(ActiveRecordFields.C_VID_CONV_ID + " = ?", mConvoId)));

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

        return v;
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

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof ActiveAndroidTask) {

            handleModelTaskComplete(t);

        } else if (t instanceof VideoDownloadTask) {

            Log.d(TAG, "video download task completed");
            VideoModel video = mVideoMap.get(((VideoDownloadTask) t).getVideoId());
            Log.d(TAG, "downloaded video with id: " + video.getGuid());
            video.setState(VideoModel.ResourceState.ON_DISK);

            // check to see if this is the next video that must be played
            VideoModel queuedVideo = mPlayBackQueue.peek();
            if (queuedVideo.getGuid().equals(video.getGuid())) { // if the queued is the one that just got downloaded then just play
                Log.d(TAG, "playing back video that was just downloaded");
                playVideo(video);
            }
        }

    }

    @Override
    public void onTaskError(Task t) {
        Log.d(TAG, "there was a problem with a task");
        // TODO: handle this later

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

            if (VideoModel.ResourceState.PENDING_DOWNLOAD.equals(video.getState())) {

                // for the number of videos, lets create two workers, to download video alternately
                AbsTaskWorker worker = new AbsTaskWorker() {
                };
                VideoDownloadTask downloadTask = new VideoDownloadTask(video); // download the video
                mTaskQueue.add(downloadTask);
                // lets create an S3 task and ask our worker to run it
                worker.setTargetFragment(this, 0);
                getFragmentManager().beginTransaction().add(worker, video.getGuid()).commit();

            } else if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {

                // if we've already been downloaded and we're on the first of the playback queue, then begin playback
                if (mPlayBackQueue.peek().getGuid().equals(video.getGuid())) {
                    playVideo(video);

                }
            }
        }

        Log.d(TAG, "active android task completed");
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
        // once the playback is complete, lets see if the next one is ready
        VideoModel video = mPlayBackQueue.poll(); // remove the one that just finished

        video.setRead(true); // mark the video as watched
        video.save();

        if (!mPlayBackQueue.isEmpty()) {
            Log.d(TAG, "playback after completion and queue is not empty");
            video = mPlayBackQueue.peek();

            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                Log.d(TAG, "starting to play video after completion");
                playVideo(video);

            }
        }

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoView.start();

    }
}
