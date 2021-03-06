package com.moziy.hollerback.fragment.delegates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.AbsFragmentLifecylce;
import com.moziy.hollerback.fragment.ConversationFragment;
import com.moziy.hollerback.fragment.RecordVideoFragment;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate.OnHistoryUpdateListener;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate.OnVideoModelLoaded;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.date.TimeUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class VideoPlayerDelegate extends AbsFragmentLifecylce implements OnVideoModelLoaded, OnHistoryUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = VideoPlayerDelegate.class.getSimpleName();
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";
    public static final String PLAYBACK_INDEX_INSTANCE_STATE = "PLAYBACK_INDEX_INSTANCE_STATE";
    public static final String PLAYING_INSTANCE_STATE = "PLAYING_INSTANCE_STATE";
    private LinkedList<VideoModel> mPlaybackQueue;
    private int mPlaybackIndex = 0;
    private boolean mStartedRecording;

    // views
    private VideoView mVideoView;
    private ProgressBar mProgress;
    private ImageButton mSkipForwardBtn;
    private ImageButton mSkipBackwardBtn;
    private boolean mHasHistoryVideo = false;
    private boolean mHasNewVideo = false;

    private ConversationFragment mConvoFragment;
    private ConversationModel mConversation;
    private long mConvoId;

    private boolean mPlayingDuringConfigChange;
    private boolean mPausedDuringPlayback; // not saved

    private boolean mIsPlayingSegmented; // flag for playing a segmented video
    private int mSegmentPart; // the segment part that is being played

    public enum VIDEO_MODEL_ENUM {
        LOCAL_HISTORY_LOADED, REMOTE_HISTORY_LOADED, NEW_VIDEO_MODEL_LOADED
    };

    private EnumSet<VIDEO_MODEL_ENUM> mHistoryFlag = EnumSet.noneOf(VIDEO_MODEL_ENUM.class);

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
                mPlaybackQueue = new LinkedList<VideoModel>((ArrayList<VideoModel>) savedInstance.getSerializable(PLAYBACK_QUEUE_INSTANCE_STATE));
            }

            if (savedInstance.containsKey(PLAYBACK_INDEX_INSTANCE_STATE)) {
                mPlaybackIndex = savedInstance.getInt(PLAYBACK_INDEX_INSTANCE_STATE);
            }
        }

        mIsPlayingSegmented = false;
        mSegmentPart = 0;
        mHasNewVideo = false;
        mHasHistoryVideo = false;
        mStartedRecording = false;

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
            outState.putSerializable(PLAYBACK_QUEUE_INSTANCE_STATE, new ArrayList<VideoModel>(mPlaybackQueue));
        }

        if (mPlayingDuringConfigChange || mPausedDuringPlayback) {
            mPlayingDuringConfigChange = true;
        }

        outState.putInt(PLAYBACK_INDEX_INSTANCE_STATE, mPlaybackIndex);

        outState.putBoolean(PLAYING_INSTANCE_STATE, mPlayingDuringConfigChange);
    }

    @Override
    public void onVideoModelLoaded(ArrayList<VideoModel> videos) {

        mHistoryFlag.add(VIDEO_MODEL_ENUM.NEW_VIDEO_MODEL_LOADED);

        if (videos != null && !videos.isEmpty()) {
            mHasNewVideo = true;
        } else { // there's nothing

            checkPlayerStatus();
            return;
        }

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
        Log.d(TAG, "onVideoDownloaded - playback index: " + mPlaybackIndex);
        // check to see if this is the next video that must be played
        if (mPlaybackIndex >= mPlaybackQueue.size()) {
            Log.w(TAG, "downloaded a file right after we left the fragmetn");
            return;
        }

        VideoModel queuedVideo = mPlaybackQueue.get(mPlaybackIndex);
        Log.d(TAG, queuedVideo.toString());
        Log.d(TAG, video.toString());

        if (queuedVideo.getGuid().equals(video.getGuid())) { // if the queued is the one that just got downloaded then just play
            mProgress.setVisibility(View.INVISIBLE);
            Log.d(TAG, "playing back video that was just downloaded");
            playVideo(video);

        }
    }

    @Override
    public void onVideoDownloadFailed(VideoModel video) {
        // since it failed, lets remove it from the playback queue
        for (int i = 0; i < mPlaybackQueue.size(); i++) {
            if (video.getGuid().equals(mPlaybackQueue.get(i).getGuid())) {

                mPlaybackQueue.remove(i); // remove the element
                if (i <= mPlaybackIndex) { // if necessary, update the playback index
                    --mPlaybackIndex;
                }
            }
        }

        if (mPlaybackQueue.isEmpty()) {
            checkPlayerStatus();
            Log.w(TAG, "can't play history, no network connection");
        }
    }

    @Override
    public void onHistoryModelLoaded(VideoModel video) {
        // TODO: insert video at proper location
        mPlaybackQueue.add(0, video);

        if (mHasHistoryVideo || mHasNewVideo) { // only increase the playback index if there's already a history video
            Log.d(TAG, "onHistoryModel- has History: " + mHasHistoryVideo + " has new video: " + mHasNewVideo);
            ++mPlaybackIndex;
        }

        VideoModel nextVideo = mPlaybackQueue.get(mPlaybackIndex);

        // sort the videos
        Collections.sort(mPlaybackQueue, new Comparator<VideoModel>() {

            @Override
            public int compare(VideoModel lhs, VideoModel rhs) {

                if (lhs.isRead() && !rhs.isRead()) {
                    return -1;
                } else if (!lhs.isRead() && rhs.isRead()) {
                    return 1;
                } else if (lhs.isRead() && rhs.isRead()) {

                    if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() < TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                        return -1;
                    } else if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() > TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                        return 1;
                    }

                    return 0;

                } else if (!lhs.isRead() && !rhs.isRead()) {
                    if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() < TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                        return -1;
                    } else if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() > TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                        return 1;
                    }

                    return 0;
                }

                return 0;
            }
        });

        // find the video in the playback queue
        for (int i = 0; i < mPlaybackQueue.size(); i++) {
            if (nextVideo == mPlaybackQueue.get(i)) {
                mPlaybackIndex = i;
                break;
            }
        }

        // if we're just watching history, then start immediate playback, if it's on disk
        if (!mHasNewVideo && (VideoModel.ResourceState.ON_DISK.equals(video.getState()) || (video.isSegmented() /* && VideoModel.ResourceState.UPLOADED.equals(video.getState()) */))) {
            if (mPlaybackQueue.get(mPlaybackIndex).getGuid() == video.getGuid()) {
                mProgress.setVisibility(View.INVISIBLE);
                playVideo(video);
            }
        }

        mHasHistoryVideo = true;
    }

    @Override
    public void onLocalHistoryLoaded(List<VideoModel> videos) {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.LOCAL_HISTORY_LOADED);
        checkPlayerStatus();
    }

    @Override
    public void onRemoteHistoryLoaded(List<VideoModel> videos) {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.REMOTE_HISTORY_LOADED);
        checkPlayerStatus();
    }

    @Override
    public void onLocalHistoryFailed() {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.LOCAL_HISTORY_LOADED);
        checkPlayerStatus();
    }

    @Override
    public void onRemoteHistoryFailed() {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.REMOTE_HISTORY_LOADED);
        checkPlayerStatus();
    }

    private void checkPlayerStatus() {
        if (mHistoryFlag.containsAll(EnumSet.allOf(VIDEO_MODEL_ENUM.class))) {
            Log.d(TAG, "all local and remote history has been loaded");
            if (mPlaybackQueue.isEmpty()) {
                Log.w(TAG, "there's nothing in the playback queue");
                mProgress.setVisibility(View.INVISIBLE);
                Toast t = Toast.makeText(HollerbackApplication.getInstance(), "No videos to play at this time", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        } else {
            for (Enum e : mHistoryFlag) {
                Log.d(TAG, "enum: " + e);
            }
        }

    }

    private void playVideo(VideoModel v) {
        Log.d(TAG, "starting playback of: " + v.getGuid());

        Uri videoUri;
        if (v.isSegmented()) {
            if (mIsPlayingSegmented == false) { // first time processing this videomodel
                mIsPlayingSegmented = true;
                mSegmentPart = 0; // start from 0
            }

            videoUri = Uri.fromFile(HBFileUtil.getSegmentedFile(mSegmentPart, v.getGuid(), "mp4"));

        } else {
            videoUri = Uri.fromFile(HBFileUtil.getOutputVideoFile(v));

        }

        mVideoView.setOnPreparedListener(this);
        mVideoView.setVideoURI(videoUri);
        mVideoView.setOnCompletionListener(this);
    }

    private void playNextVideo() {

        if (mPlaybackIndex < mPlaybackQueue.size()) {
            VideoModel playingVideo = mPlaybackQueue.get(mPlaybackIndex);
            if (!playingVideo.isRead())
                setVideoSeen(playingVideo);

        }

        if (mPlaybackIndex >= mPlaybackQueue.size() - 1) {

            if (!mStartedRecording)
                beginRecording();

            return;
        }

        mProgress.setVisibility(View.VISIBLE);

        // mark the video as seen
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }

        ++mPlaybackIndex;

        VideoModel videoToPlay = mPlaybackQueue.get(mPlaybackIndex);
        // making the assumption that video is on the device when it's segmented since it's a video the user has sent
        if (ResourceState.ON_DISK.equals(videoToPlay.getState()) || videoToPlay.isSegmented()) {
            playVideo(videoToPlay);
        }

    }

    private void playLastVideo() {
        mProgress.setVisibility(View.VISIBLE);
        --mPlaybackIndex;
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }

        VideoModel videoToPlay = mPlaybackQueue.get(mPlaybackIndex);
        if (ResourceState.ON_DISK.equals(videoToPlay.getState()) || videoToPlay.isSegmented()) {
            playVideo(videoToPlay);
        }
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
        mVideoView.stopPlayback();
        // mp.reset();

        // once the playback is complete, lets see if the next one is ready
        VideoModel video = mPlaybackQueue.get(mPlaybackIndex); // remove the one that just finished
        if (video.isSegmented() && mIsPlayingSegmented) {
            ++mSegmentPart; // we're playing a segmented file, so instead of going to the next video, just play the next part
            if (mSegmentPart < video.getNumParts()) {
                playVideo(video); // move to the next part
                return;
            } else {
                mIsPlayingSegmented = false;
                mSegmentPart = 0;
            }
        }

        ++mPlaybackIndex; // increate the playback index;

        if (!video.isRead()) { // mark as read only if it isn't read already
            setVideoSeen(video);
        }

        if (mPlaybackIndex < mPlaybackQueue.size()) {

            Log.d(TAG, "playback after completion and queue is not empty");
            video = mPlaybackQueue.get(mPlaybackIndex);

            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                Log.d(TAG, "starting to play video after completion");
                playVideo(video);
            } else if (video.isSegmented() /* && VideoModel.ResourceState.UPLOADED.equals(video.getState()) */) {
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
            mStartedRecording = true;
            // we're ready to move to the recording fragment
            RecordVideoFragment f = RecordVideoFragment.newInstance(mConvoId, "Muhahahaha");
            f.setTargetFragment(mConvoFragment, 0);
            mConvoFragment.getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom).replace(R.id.fragment_holder, f)
                    .addToBackStack(ConversationFragment.FRAGMENT_TAG).commit();
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
