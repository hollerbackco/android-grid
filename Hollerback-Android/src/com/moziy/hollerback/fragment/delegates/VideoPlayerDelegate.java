package com.moziy.hollerback.fragment.delegates;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.AbsFragmentLifecylce;
import com.moziy.hollerback.fragment.ConversationFragment;
import com.moziy.hollerback.fragment.RecordVideoFragment;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate.OnHistoryVideoDownloaded;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate.OnVideoModelLoaded;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;

public class VideoPlayerDelegate extends AbsFragmentLifecylce implements OnVideoModelLoaded, OnHistoryVideoDownloaded, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = VideoPlayerDelegate.class.getSimpleName();
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";
    public static final String PLAYBACK_INDEX_INSTANCE_STATE = "PLAYBACK_INDEX_INSTANCE_STATE";
    public static final String PLAYING_INSTANCE_STATE = "PLAYING_INSTANCE_STATE";
    private LinkedList<VideoModel> mPlaybackQueue;
    private int mPlaybackIndex = 0;

    // views
    private VideoView mVideoView;
    private ProgressBar mProgress;
    private ImageButton mSkipForwardBtn;
    private ImageButton mSkipBackwardBtn;
    private boolean mHasHistoryVideo = false;

    private ConversationFragment mConvoFragment;
    private ConversationModel mConversation;
    private long mConvoId;

    private boolean mPlayingDuringConfigChange;
    private boolean mPausedDuringPlayback; // not saved

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
        if (savedInstance != null) {
            if (savedInstance.containsKey(PLAYBACK_QUEUE_INSTANCE_STATE)) {
                mPlaybackQueue = (LinkedList<VideoModel>) savedInstance.getSerializable(PLAYBACK_QUEUE_INSTANCE_STATE);
            }

            if (savedInstance.containsKey(PLAYBACK_INDEX_INSTANCE_STATE)) {
                mPlaybackIndex = savedInstance.getInt(PLAYBACK_INDEX_INSTANCE_STATE);
            }
        }
    }

    public VideoPlayerDelegate(long convoId) {
        mPlaybackQueue = new LinkedList<VideoModel>();
        mConvoId = convoId;
    }

    @Override
    public void onPostSuperActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // if we were in the middle of playing, then adjust the playback elements such as seek position
            mPlayingDuringConfigChange = savedInstanceState.getBoolean(PLAYING_INSTANCE_STATE);
            if (mPlayingDuringConfigChange) {
                playVideo(mPlaybackQueue.get(mPlaybackIndex));
            }
        }
    }

    public void onViewCreated(View parentView) {
        mVideoView = (VideoView) parentView.findViewById(R.id.vv_preview);
        mProgress = (ProgressBar) parentView.findViewById(R.id.progress);
        mProgress.setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView");

        mSkipBackwardBtn = (ImageButton) parentView.findViewById(R.id.ib_skip_backward);
        // mSkipBackwardBt.setVisibility(View.GONE);
        mSkipBackwardBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlaybackIndex > 0)
                    playLastVideo();

            }
        });

        mSkipForwardBtn = (ImageButton) parentView.findViewById(R.id.ib_skip_forward);
        // mSkipForwardBt.setVisibility(View.GONE);
        mSkipForwardBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlaybackIndex < mPlaybackQueue.size() - 1)
                    playNextVideo();

            }
        });
    }

    @Override
    public void onPostSuperResume(Fragment fragment) {

        if (mPausedDuringPlayback) { // reset the flag
            mProgress.setVisibility(View.INVISIBLE);
            playVideo(mPlaybackQueue.get(mPlaybackIndex));
        }
    }

    @Override
    public void onPreSuperPause(Fragment fragment) {
        // allow for config changes while we were paused{

        mPausedDuringPlayback = mVideoView.isPlaying();
        if (mPausedDuringPlayback)
            mVideoView.stopPlayback();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO: Save the video seek position, the video file that is being played so that once restored we can begin playing
        if (mPlaybackQueue != null) {
            outState.putSerializable(PLAYBACK_QUEUE_INSTANCE_STATE, mPlaybackQueue);
        }

        if (mPlayingDuringConfigChange || mPausedDuringPlayback) {
            mPlayingDuringConfigChange = true;
        }

        outState.putInt(PLAYBACK_INDEX_INSTANCE_STATE, mPlaybackIndex);

        outState.putBoolean(PLAYING_INSTANCE_STATE, mPlayingDuringConfigChange);
    }

    @Override
    public void onVideoModelLoaded(ArrayList<VideoModel> videos) {

        mPlaybackQueue.addAll(videos);

        for (VideoModel video : videos) {

            Log.d(TAG, "processing video with state: " + video.toString());
            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                mProgress.setVisibility(View.INVISIBLE);
                // if we've already been downloaded and we're on the first of the playback queue, then begin playback
                if (mPlaybackQueue.get(mPlaybackIndex).getGuid().equals(video.getGuid())) {
                    playVideo(video);
                }
            }
        }

    }

    @Override
    public void onVideoDownloaded(VideoModel video) {
        // check to see if this is the next video that must be played
        VideoModel queuedVideo = mPlaybackQueue.get(mPlaybackIndex);

        if (queuedVideo.getGuid().equals(video.getGuid())) { // if the queued is the one that just got downloaded then just play
            mProgress.setVisibility(View.INVISIBLE);
            Log.d(TAG, "playing back video that was just downloaded");
            playVideo(video);

        }
    }

    @Override
    public void onHistoryVideoDownloaded(VideoModel video) {
        // TODO: insert video at proper location
        mPlaybackQueue.add(0, video);

        if (mHasHistoryVideo) // only increase the playback index if there's already a history video
            ++mPlaybackIndex;

        mHasHistoryVideo = true;
    }

    private void playVideo(VideoModel v) {
        Log.d(TAG, "starting playback of: " + v.getGuid());
        mVideoView.setOnPreparedListener(this);
        mVideoView.setVideoURI(Uri.fromFile(HBFileUtil.getOutputVideoFile(v)));
        mVideoView.setOnCompletionListener(this);
    }

    private void playNextVideo() {
        mProgress.setVisibility(View.VISIBLE);
        ++mPlaybackIndex;
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        playVideo(mPlaybackQueue.get(mPlaybackIndex));

    }

    private void playLastVideo() {
        mProgress.setVisibility(View.VISIBLE);
        --mPlaybackIndex;
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }

        playVideo(mPlaybackQueue.get(mPlaybackIndex));

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // only play if we're in the resumed state
        Log.d(TAG, "onPrepared()");
        mProgress.setVisibility(View.INVISIBLE);
        if (mConvoFragment.isResumed()) {

            mVideoView.start();
        } else {
            Log.d(TAG, "not playing because not in resumed state");
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "video playback complete");
        mp.reset();

        // once the playback is complete, lets see if the next one is ready
        VideoModel video = mPlaybackQueue.get(mPlaybackIndex); // remove the one that just finished
        ++mPlaybackIndex; // increate the playback index;

        if (!video.isRead()) { // mark as read only if it isn't read already
            setVideoSeen(video);
        }

        // delete the video from the sdcard?

        if (mPlaybackIndex < mPlaybackQueue.size()) {

            Log.d(TAG, "playback after completion and queue is not empty");
            video = mPlaybackQueue.get(mPlaybackIndex);

            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                Log.d(TAG, "starting to play video after completion");
                playVideo(video);
            }

        } else {

            boolean isShown = PreferenceManagerUtil.getPreferenceValue(HBPreferences.SHOWN_START_RECORDING_DIALOG, false);
            if (!isShown && mConvoFragment.isAdded()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mConvoFragment.getActivity());
                builder.setTitle(mConvoFragment.getString(R.string.start_recording_dialog_title));
                builder.setMessage(mConvoFragment.getString(R.string.start_recording_dialog_message));
                builder.setCancelable(false);
                builder.setPositiveButton(mConvoFragment.getString(R.string.start_recording_dialog_button), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManagerUtil.setPreferenceValue(HBPreferences.SHOWN_START_RECORDING_DIALOG, true);

                        // also broadcast that the conversation has been updated
                        beginRecording();

                    }
                });
                builder.show();
            } else {
                beginRecording();
            }

        }

    }

    private void beginRecording() {
        if (mConvoFragment.isResumed()) {
            // we're ready to move to the recording fragment
            RecordVideoFragment f = RecordVideoFragment.newInstance(mConvoId, "Muhahahaha");
            f.setTargetFragment(mConvoFragment, 0);
            mConvoFragment.getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_top, R.anim.slide_out_to_bottom)
                    .replace(R.id.fragment_holder, f).addToBackStack(ConversationFragment.FRAGMENT_TAG).commit();
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

                ActiveAndroid.beginTransaction();
                try {
                    VideoModel video = ((ActiveAndroidTask<VideoModel>) t).getResults().get(0); // must be valid!
                    Log.d(TAG, "fetching latest from db: " + video.toString());

                    synchronized (ConversationModel.class) { // very important synchronization with the sync service

                        video.setRead(true); // mark the video as watched
                        video.setWatchedState(VideoModel.ResourceState.WATCHED_PENDING_POST);
                        video.save();

                        // TODO - Sajjad: Create a service to go and remove the watched videos
                        if (mConversation == null) {
                            mConversation = new Select().from(ConversationModel.class).where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId).executeSingle();
                        }
                        Log.d(TAG, "new unread count: " + (mConversation.getUnreadCount() - 1));
                        mConversation.setUnreadCount(mConversation.getUnreadCount() - 1);
                        mConversation.save(); // save that we've unread
                    }
                    ActiveAndroid.setTransactionSuccessful();

                } finally {
                    ActiveAndroid.endTransaction();
                }

                // broadcast that the conversations have changed
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));
            }
        });

        TaskExecuter executer = new TaskExecuter(true); // allow only 1 update at a time
        executer.executeTask(updateVideoTask);
    }

}
