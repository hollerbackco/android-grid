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
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.moziy.hollerback.fragment.ConvoHistoryTwo;
import com.moziy.hollerback.fragment.ConvoHistoryTwo.ConvoHistoryAdapter;
import com.moziy.hollerback.fragment.RecordVideoFragment;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.fragment.VideoPlaybackFragment;
import com.moziy.hollerback.fragment.VideoPlaybackFragment.VideoViewStatusListener;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate.OnHistoryUpdateListener;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate.OnVideoModelLoaded;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.VideoModel.ResourceState;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.service.task.VideoDownloadTask.ProgressListener;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.date.TimeUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class VideoPlayerDelegateTwo extends AbsFragmentLifecylce implements OnVideoModelLoaded, OnHistoryUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        VideoViewStatusListener, RecordingInfo, ProgressListener {
    private static final String TAG = VideoPlayerDelegateTwo.class.getSimpleName();
    public static final String PLAYBACK_QUEUE_INSTANCE_STATE = "PLAYBACK_QUEUE_INSTANCE_STATE";
    public static final String PLAYBACK_INDEX_INSTANCE_STATE = "PLAYBACK_INDEX_INSTANCE_STATE";
    public static final String PLAYING_INSTANCE_STATE = "PLAYING_INSTANCE_STATE";
    public static final String MODEL_LOADED_INSTANCE_STATE = "MODEL_LOADED";
    private LinkedList<VideoModel> mPlaybackQueue;
    private int mPlaybackIndex = 0;
    private boolean mIsEnteringRecording;

    // views
    private VideoView mVideoView;
    private TextView mSenderName;
    private TextView mDateSent;
    private ProgressBar mProgress;
    private ImageButton mSkipForwardBtn;
    private ImageButton mSkipBackwardBtn;
    private boolean mHasHistoryVideo = false;
    private boolean mHasNewVideo = false;
    private volatile int mNewVideoIndex = 0;

    private ConvoHistoryTwo mConvoFragment;
    private ConversationModel mConversation;
    private ConvoLoaderDelegate mLoaderDelegate;
    private long mConvoId;

    private boolean mPlayingDuringConfigChange;
    private boolean mPausedDuringPlayback; // not saved

    private boolean mIsPlayingSegmented; // flag for playing a segmented video
    private int mSegmentPart; // the segment part that is being played

    private boolean mInPlayback;
    private ConvoHistoryAdapter mAdapter;

    private VideoPlaybackFragment mPlaybackFragment;
    private String mPlaybackHeadId;

    public enum VIDEO_MODEL_ENUM {
        LOCAL_HISTORY_LOADED, REMOTE_HISTORY_LOADED, NEW_VIDEO_MODEL_LOADED
    };

    private EnumSet<VIDEO_MODEL_ENUM> mHistoryFlag = EnumSet.noneOf(VIDEO_MODEL_ENUM.class);

    public void setAdapter(ConvoHistoryAdapter adapter) {
        mAdapter = adapter;
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
    public void init(Bundle savedInstance) {
        if (savedInstance != null) {
            if (savedInstance.containsKey(PLAYBACK_QUEUE_INSTANCE_STATE)) {
                mPlaybackQueue = new LinkedList<VideoModel>((ArrayList<VideoModel>) savedInstance.getSerializable(PLAYBACK_QUEUE_INSTANCE_STATE));
                mAdapter.addAll(mPlaybackQueue);
            }

            if (savedInstance.containsKey(PLAYBACK_INDEX_INSTANCE_STATE)) {
                mPlaybackIndex = savedInstance.getInt(PLAYBACK_INDEX_INSTANCE_STATE);
            }

            Fragment f = mConvoFragment.getFragmentManager().findFragmentByTag(VideoPlaybackFragment.FRAGMENT_TAG);
            if (f == null) {
                mPlaybackFragment = new VideoPlaybackFragment();
            } else {
                mPlaybackFragment = (VideoPlaybackFragment) f;
            }

            mHistoryFlag = (EnumSet<VideoPlayerDelegateTwo.VIDEO_MODEL_ENUM>) savedInstance.getSerializable(MODEL_LOADED_INSTANCE_STATE);

        } else {
            mPlaybackFragment = new VideoPlaybackFragment();
        }

        mIsPlayingSegmented = false;
        mSegmentPart = 0;
        mHasNewVideo = false;
        mHasHistoryVideo = false;
        mIsEnteringRecording = false;
        mInPlayback = false;

    }

    public boolean inPlaybackMode() {
        return mInPlayback;
    }

    public VideoPlayerDelegateTwo(long convoId) {
        this(convoId, null);
    }

    public VideoPlayerDelegateTwo(long convoId, ConvoLoaderDelegate delegate) {
        mPlaybackQueue = new LinkedList<VideoModel>();
        mConvoId = convoId;
        mLoaderDelegate = delegate;
    }

    @Override
    public void onPostSuperActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // if we were in the middle of playing, then adjust the playback elements such as seek position
            mPlayingDuringConfigChange = savedInstanceState.getBoolean(PLAYING_INSTANCE_STATE);
            if (mPlayingDuringConfigChange && mVideoView != null) {
                playVideo(mPlaybackQueue.get(mPlaybackIndex));
            }
        }
    }

    /**
     * Called when the videoview is created and ready
     * @param parentView
     */
    public void onViewCreated(final View parentView) {
        mVideoView = (VideoView) parentView.findViewById(R.id.vv_preview);
        mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mVideoView.setLayoutParams(getVideoViewLayoutParams());

        mSenderName = (TextView) parentView.findViewById(R.id.tv_name);
        mDateSent = (TextView) parentView.findViewById(R.id.tv_date);

        mProgress = (ProgressBar) parentView.findViewById(R.id.progress);
        mProgress.setVisibility(View.VISIBLE);

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

    private LayoutParams getVideoViewLayoutParams() {

        LayoutParams params = new LayoutParams(AppEnvironment.OPTIMAL_VIDEO_SIZE.x, AppEnvironment.OPTIMAL_VIDEO_SIZE.y, Gravity.CENTER);
        return params;
    }

    @Override
    public void onPostSuperResume(Fragment fragment) {

        if (mPausedDuringPlayback /* || mPlayingDuringConfigChange) && mVideoView != null && mPlaybackFrament.isResumed() */) { // reset the flag
            mProgress.setVisibility(View.INVISIBLE);
            playVideo(mPlaybackQueue.get(mPlaybackIndex));
        }
    }

    @Override
    public void onPreSuperPause(Fragment fragment) {
        // allow for config changes while we were paused{

        if (mVideoView != null)
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

        outState.putSerializable(MODEL_LOADED_INSTANCE_STATE, mHistoryFlag);

        outState.putInt(PLAYBACK_INDEX_INSTANCE_STATE, mPlaybackIndex);

        outState.putBoolean(PLAYING_INSTANCE_STATE, mPlayingDuringConfigChange);
    }

    public void play(int index) {

        if (!isAllModelLoaded())
            return;

        mPlaybackIndex = index;

        // VideoModel v = mPlaybackQueue.get(mPlaybackIndex);
        // mInPlayback = true;
        // if (v.getState().equals(VideoModel.ResourceState.ON_DISK)) {
        // playVideo(v);
        // } else {
        // download(mPlaybackIndex, 3);
        // }
        if (mConvoFragment.getFragmentManager().findFragmentByTag(VideoPlaybackFragment.FRAGMENT_TAG) == null) {
            mPlaybackFragment.setTargetFragment(mConvoFragment, 0);
            mConvoFragment.getFragmentManager().beginTransaction().add(R.id.fragment_holder, mPlaybackFragment, VideoPlaybackFragment.FRAGMENT_TAG).addToBackStack(null).commit();
        } else {
            Log.d(TAG, "video view still attached");
            // just start playing ?
        }
    }

    @Override
    public void onVideoViewReady(View layout) {
        Log.d(TAG, "onVideoViewReady");
        onViewCreated(layout);
        // MediaController controller = new MediaController(mConvoFragment.getActivity());
        // controller.setAnchorView(mVideoView);
        // mVideoView.setMediaController(controller);
        VideoModel v = mPlaybackQueue.get(mPlaybackIndex);
        mPlaybackHeadId = v.getVideoId();
        mProgress.setProgress(0);
        mSenderName.setText(v.getSenderName());
        mDateSent.setText(ConversionUtil.timeAgo(TimeUtil.PARSE(v.getCreateDate())));

        // Picasso.with(mConvoFragment.getActivity()).load(v.getThumbUrl()).;
        mInPlayback = true;
        Log.d(TAG, v.toString());
        if (v.getState().equals(VideoModel.ResourceState.ON_DISK) || v.isSegmented()) {
            playVideo(v);
        } else {
            download(mPlaybackIndex, 3);
        }
    }

    @Override
    public void onVideoViewFinish() {

    }

    private void download(int index, int numToDownload) {

        for (int i = index; i < index + numToDownload; i++) {
            if (i < mPlaybackQueue.size()) {
                VideoModel v = mPlaybackQueue.get(i);
                if (v.getState().equals(ResourceState.PENDING_DOWNLOAD)) {
                    mLoaderDelegate.requestDownload(v, this);
                }
            }
        }

    }

    @Override
    public void onVideoModelLoaded(ArrayList<VideoModel> videos) {

        mHistoryFlag.add(VIDEO_MODEL_ENUM.NEW_VIDEO_MODEL_LOADED);

        if (videos != null && !videos.isEmpty()) {
            mHasNewVideo = true;
        } else { // there's nothing

            onModelLoaded();
            return;
        }

        int index = mPlaybackQueue.size();

        mPlaybackQueue.addAll(videos);
        mAdapter.addAll(videos);

        mNewVideoIndex += index;

        onModelLoaded();

        // don't auto play

        if (mInPlayback) {
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

        if (mInPlayback) {
            if (queuedVideo.getGuid().equals(video.getGuid())) { // if the queued is the one that just got downloaded then just play
                mProgress.setVisibility(View.INVISIBLE);
                Log.d(TAG, "playing back video that was just downloaded");
                playVideo(video);

            }
        }
    }

    @Override
    public void onVideoDownloadFailed(VideoModel video) {
        // since it failed, lets remove it from the playback queue
        for (int i = 0; i < mPlaybackQueue.size(); i++) {
            if (video.getGuid().equals(mPlaybackQueue.get(i).getGuid())) {

                VideoModel removed = mPlaybackQueue.remove(i); // remove the element
                mAdapter.remove(removed);
                if (i <= mPlaybackIndex) { // if necessary, update the playback index
                    --mPlaybackIndex;
                }
            }
        }

        if (mPlaybackQueue.isEmpty()) {
            onModelLoaded();
            Log.w(TAG, "can't play history, no network connection");
        }
    }

    private Comparator<VideoModel> mVideoSorter = new Comparator<VideoModel>() {

        @Override
        public int compare(VideoModel lhs, VideoModel rhs) {

            if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() < TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                return -1;
            } else if (TimeUtil.PARSE(lhs.getCreateDate()).getTime() > TimeUtil.PARSE(rhs.getCreateDate()).getTime()) {
                return 1;
            }

            return 0;

        }
    };

    private Comparator<VideoModel> mVideoSorter1 = new Comparator<VideoModel>() {

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
    };

    // @Override
    // public void onHistoryModelLoaded(VideoModel video) {
    // // TODO: insert video at proper location
    // mPlaybackQueue.add(0, video);
    // mAdapter.insert(video, 0);
    // if (mHasHistoryVideo || mHasNewVideo) { // only increase the playback index if there's already a history video
    // Log.d(TAG, "onHistoryModel- has History: " + mHasHistoryVideo + " has new video: " + mHasNewVideo);
    // ++mPlaybackIndex;
    // }
    //
    // VideoModel nextVideo = mPlaybackQueue.get(mPlaybackIndex);
    //
    // // sort the videos
    // Collections.sort(mPlaybackQueue, mVideoSorter);
    //
    // mAdapter.sort(mVideoSorter);
    //
    // // find the video in the playback queue
    // for (int i = 0; i < mPlaybackQueue.size(); i++) {
    // if (nextVideo == mPlaybackQueue.get(i)) {
    // mPlaybackIndex = i;
    // break;
    // }
    // }
    //
    // if (mInPlayback) {
    // // if we're just watching history, then start immediate playback, if it's on disk
    // if (!mHasNewVideo && (VideoModel.ResourceState.ON_DISK.equals(video.getState()) || (video.isSegmented() /* && VideoModel.ResourceState.UPLOADED.equals(video.getState()) */))) {
    // if (mPlaybackQueue.get(mPlaybackIndex).getGuid() == video.getGuid()) {
    // mProgress.setVisibility(View.INVISIBLE);
    // playVideo(video);
    // }
    // }
    // }
    //
    // mHasHistoryVideo = true;
    // }

    @Override
    public void onHistoryModelLoaded(VideoModel video) {
        // TODO: insert video at proper location
        mPlaybackQueue.add(0, video);
        mAdapter.insert(video, 0);

        mHasHistoryVideo = true;
    }

    @Override
    public void onLocalHistoryLoaded(List<VideoModel> videos) {
        Collections.sort(mPlaybackQueue, mVideoSorter);
        mAdapter.sort(mVideoSorter);
        mHistoryFlag.add(VIDEO_MODEL_ENUM.LOCAL_HISTORY_LOADED);
        mNewVideoIndex += videos.size();
        onModelLoaded();

    }

    @Override
    public void onRemoteHistoryLoaded(List<VideoModel> videos) {
        Collections.sort(mPlaybackQueue, mVideoSorter);
        mAdapter.sort(mVideoSorter);
        mHistoryFlag.add(VIDEO_MODEL_ENUM.REMOTE_HISTORY_LOADED);
        mNewVideoIndex += videos.size();
        onModelLoaded();

    }

    @Override
    public void onLocalHistoryFailed() {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.LOCAL_HISTORY_LOADED);
        onModelLoaded();
    }

    @Override
    public void onRemoteHistoryFailed() {
        mHistoryFlag.add(VIDEO_MODEL_ENUM.REMOTE_HISTORY_LOADED);
        onModelLoaded();
    }

    private boolean isAllModelLoaded() {
        return mHistoryFlag.containsAll(EnumSet.allOf(VIDEO_MODEL_ENUM.class));
    }

    private void onModelLoaded() {
        if (isAllModelLoaded()) {

            if (mConvoFragment != null) {

                if (mHasNewVideo && mConvoFragment.getConvoListView() != null) {
                    mConvoFragment.getConvoListView().smoothScrollToPosition(mNewVideoIndex);
                }

                if (mAdapter.getCount() > 20) {
                    mConvoFragment.getConvoListView().setFastScrollEnabled(true);
                }

                mConvoFragment.stopLoadingProgress();
            }

            Log.d(TAG, "all local and remote history has been loaded");
            if (mPlaybackQueue.isEmpty()) {
                Log.w(TAG, "there's nothing in the playback queue");
                // mProgress.setVisibility(View.INVISIBLE);
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

        mConvoFragment.getConvoListView().setSelection(mPlaybackIndex);

        Uri videoUri;
        if (v.isSegmented()) {
            if (mIsPlayingSegmented == false) { // first time processing this videomodel
                mIsPlayingSegmented = true;
                mSegmentPart = 0; // start from 0
            }

            videoUri = Uri.fromFile(HBFileUtil.getSegmentedFile(mSegmentPart, v.getGuid(), v.getSegmentFileExtension()));

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

            if (!mIsEnteringRecording)
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
        // if (mConvoFragment.isResumed()) { // was mConvoFragment

        if (mPlaybackFragment.isResumed()) {
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

            // scroll to the next position
            mConvoFragment.getConvoListView().setSelection(mPlaybackIndex);
            Log.d(TAG, "smooth scroll: " + mPlaybackIndex);

            Log.d(TAG, "playback after completion and queue is not empty");
            video = mPlaybackQueue.get(mPlaybackIndex);

            // set the view
            mSenderName.setText(video.getSenderName());
            mDateSent.setText(ConversionUtil.timeAgo(TimeUtil.PARSE(video.getCreateDate())));

            if (VideoModel.ResourceState.ON_DISK.equals(video.getState())) {
                Log.d(TAG, "starting to play video after completion");
                playVideo(video);
            } else if (video.isSegmented() /* && VideoModel.ResourceState.UPLOADED.equals(video.getState()) */) {
                playVideo(video);
            }

            download(mPlaybackIndex + 1, 3);

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

    public void beginRecording() {
        if (mConvoFragment.isResumed()) {

            mInPlayback = false;
            mIsEnteringRecording = true;

            Fragment playbackFragment = mConvoFragment.getFragmentManager().findFragmentByTag(VideoPlaybackFragment.FRAGMENT_TAG);
            if (playbackFragment != null) {
                // remove the playback fragment
                mConvoFragment.getFragmentManager().popBackStack();
            }

            // we're ready to move to the recording fragment
            RecordVideoFragment f = RecordVideoFragment.newInstance(mConvoId, "Muhahahaha");
            f.setTargetFragment(mConvoFragment, 0);
            mConvoFragment.getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                    .replace(R.id.fragment_holder, f).addToBackStack(ConversationFragment.FRAGMENT_TAG).commit();
        }
    }

    public boolean isEnteringRecording() {
        return mIsEnteringRecording;
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

    @Override
    public void onRecordingFinished(Bundle info) {
        mIsEnteringRecording = false;
        if (!info.getBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, true)) {
            return;
        }

        if (info.containsKey(RecordingInfo.VIDEO_MODEL)) {
            VideoModel newlyRecorded = (VideoModel) info.getSerializable(RecordingInfo.VIDEO_MODEL);
            mPlaybackQueue.add(newlyRecorded);
            mAdapter.add(newlyRecorded);

        }

    }

    // get updates to the progress of video downloads
    @Override
    public synchronized void onProgress(int progress, String videoId) {
        if (videoId.equals(mPlaybackHeadId) && mProgress != null && mConvoFragment.isResumed()) {
            mProgress.setProgress(progress);
        }

    }
}
