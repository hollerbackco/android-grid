package com.moziy.hollerback.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.activeandroid.query.Update;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.camera.util.CameraUtil;
import com.moziy.hollerback.camera.view.Preview;
import com.moziy.hollerback.camera.view.PreviewSurfaceView;
import com.moziy.hollerback.camera.view.PreviewTextureView;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.TTYLService;
import com.moziy.hollerback.service.VideoUploadIntentService;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.date.TimeUtil;
import com.moziy.hollerback.widget.CustomButton;

public class RecordVideoFragment extends BaseFragment implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback2 {
    private static final String TAG = RecordVideoFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = RecordVideoFragment.class.getSimpleName();
    public static final String FRAGMENT_ARG_PHONES = "phones";
    public static final String FRAGMENT_ARG_TITLE = "title";
    public static final String FRAGMENT_ARG_GOTO_CONVO = "to_conversation";

    public static interface RecordingInfo {
        public static final String RECORDED_PARTS = "recorded_parts";
        public static final String RESOURCE_ROW_ID = "resource_row_id";
        public static final String STATUS_BUNDLE_ARG_KEY = "record_status";
        public static final String RESOURCE_GUID = "resource_guid";

        public void onRecordingFinished(Bundle info);
    }

    private static final int PREFERRED_VIDEO_WIDTH = 320;
    private static final int PREFERRED_VIDEO_HEIGHT = 240;

    private final Semaphore mCameraSemaphore = new Semaphore(1);
    private volatile Camera mCamera = null;
    private Preview mCameraPreview;
    private PreviewTouchDelegate mPreviewDelegate;
    private volatile boolean inPreview = false;
    private volatile Camera.Size mBestVideoSize;
    private volatile Camera.Size mBestPreviewSize;
    protected CustomButton mSendButton;
    private int mVolumeBeforeShutoff;
    private Button mSwitchCameraButton;

    private volatile boolean mSurfaceCreated = false;
    private boolean mSpecialOrientationRequest;

    private static final boolean USE_SURFACE_VIEW = (Build.VERSION.SDK_INT < 16 ? true : false);

    TextView mTimer;
    private final Handler mHandler = new Handler();

    int secondsPassed;
    private volatile int mCurrentCameraId = CameraInfo.CAMERA_FACING_FRONT;
    private volatile boolean mIsSwitching;

    private String mFileDataPath;
    protected String mFileDataName;
    private String mFileExt; // the file extension
    private String mGuid;

    private VideoModel mVideoModel; // the model that represents the resource that will get uploaded

    private int mPartNum;
    private int mTotalParts;

    int timer = 30;

    private volatile boolean mHasRecordingStarted = false;
    private volatile boolean mIsRecording = false;

    private volatile boolean mIsExiting;

    static MediaRecorder recorder;

    float targetPreviewWidth;
    float targetPreviewHeight;
    String targetExtension;

    private long mConversationId = -1;

    private String[] mPhones = null;
    ViewPager mFilterPagers;

    private TextureView mTexturePreview;

    public static RecordVideoFragment newInstance(long conversationId, String title) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(IABIntent.PARAM_ID, conversationId);
        bundle.putString(FRAGMENT_ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static RecordVideoFragment newInstance(long conversationId, boolean toConversation, String title) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(IABIntent.PARAM_ID, conversationId);
        bundle.putString(FRAGMENT_ARG_TITLE, title);
        bundle.putBoolean(FRAGMENT_ARG_GOTO_CONVO, toConversation);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static RecordVideoFragment newInstance(String[] phones, String title) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(FRAGMENT_ARG_PHONES, phones);
        bundle.putString(FRAGMENT_ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    private Thread mOpenCameraThread = new Thread() {
        public void run() {

            openCamera();

            setBestPreviewAndVideoSizes();

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    mCameraPreview.setAspectRatio((double) mBestVideoSize.width / (double) mBestVideoSize.height); // adjust the surfaceview

                    new Thread() {
                        public void run() {
                            if (startPreview(mCurrentCameraId)) {
                                if (!isRecording()) {
                                    startRecording();
                                }
                            }
                        };
                    }.start();

                }
            });
        };
    };

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSpecialOrientationRequest = true;
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getActivity().getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        // mActivity.getSupportActionBar().setTitle(R.string.action_record);

        Bundle args = getArguments();

        if (args.containsKey(IABIntent.PARAM_ID)) {
            mConversationId = args.getLong(IABIntent.PARAM_ID);
            LogUtil.i("HollerbackCamera CONVO: " + mConversationId);
        }

        mPhones = args.getStringArray(FRAGMENT_ARG_PHONES);
        args.getBoolean(FRAGMENT_ARG_GOTO_CONVO, false); // TODO, CLEANUP

        mPartNum = 0;
        mTotalParts = 0;
        mFileDataName = null;

    }

    private boolean mScreenOrientationLocked = false;

    private void lockScreenOrientation() {
        if (!mScreenOrientationLocked) {
            final int orientation = getResources().getConfiguration().orientation;
            final int rotation = getActivity().getWindowManager().getDefaultDisplay().getOrientation();

            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }

            mScreenOrientationLocked = true;
        }
    }

    private void unlockScreenOrientation() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mScreenOrientationLocked = false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.recording_layout, container, false);

        mTimer = (TextView) v.findViewById(R.id.tv_timer);
        GradientDrawable d = (GradientDrawable) mTimer.getBackground();
        d.mutate();
        d.setColor(Color.WHITE);

        mBlinker = (ImageView) v.findViewById(R.id.iv_blinker);

        ViewGroup previewHolder = (FrameLayout) v.findViewById(R.id.preview);
        mPreviewDelegate = new PreviewTouchDelegate();
        if (USE_SURFACE_VIEW) {
            PreviewSurfaceView surfaceView = new PreviewSurfaceView(getActivity());
            surfaceView.getHolder().addCallback(this);
            // surfaceView.setOnClickListener(mSendButtonClick); //disable these for now
            // surfaceView.setOnTouchListener(mPreviewDelegate.mOnPreviewTouchListener);
            mCameraPreview = surfaceView;
        } else {
            PreviewTextureView textureView = new PreviewTextureView(getActivity());
            // textureView.setOnClickListener(mSendButtonClick); //disable these for now
            // textureView.setOnTouchListener(mPreviewDelegate.mOnPreviewTouchListener);
            textureView.setSurfaceTextureListener(this);
            mCameraPreview = textureView;
        }

        mSwitchCameraButton = (Button) v.findViewById(R.id.bt_switch_camera);
        mSwitchCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchRecordingCamerasTo(getOppositeCamera());
            }
        });

        mSendButton = (CustomButton) v.findViewById(R.id.bt_send);
        mSendButton.setOnClickListener(mSendButtonClick);

        previewHolder.addView((View) mCameraPreview);

        return v;

    }

    private View.OnClickListener mSendButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isRecording()) { // send the video once recording has stopped

                stopRecording();

                if (mConversationId < 0) {
                    Log.d(TAG, "attempt to create new conversation");
                    inviteAndRecordVideo();
                } else {
                    Log.d(TAG, "attempt to post to existing conversation");
                    postToConversation(mConversationId);
                }
            }

        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.record, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onPause();
                break;
            case R.id.action_cancel:
                this.onPause();
                this.getFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, 0);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    // this can be used later if we go to a complete texture view solution
    public TextureView getTextureBasedView() {
        mTexturePreview = new PreviewTextureView(getActivity());
        mTexturePreview.setSurfaceTextureListener(this);

        mTexturePreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { // don't do anything
                // if (isRecording()) { // send the video once recording has stopped
                //
                // stopRecording();
                //
                // if (mConversationId < 0) {
                // Log.d(TAG, "attempt to create new conversation");
                // inviteAndRecordVideo();
                // } else {
                // Log.d(TAG, "attempt to post to existing conversation");
                // postToConversation(mConversationId);
                // }
                // }

            }
        });

        return mTexturePreview;
    }

    private void createNewConversation(ArrayList<String> contacts) {
        sendVideo(-1, contacts);
    }

    private void postToConversation(long conversationId) {
        sendVideo(conversationId, null);
    }

    /**
     * 
     * @param fileName
     * @param contacts
     */
    private void sendVideo(long conversationId, ArrayList<String> recipients) {

        prepareVideoModel(conversationId, recipients);

        long resourceRowId = mVideoModel.getId();

        notifyTargetFragment(resourceRowId, mVideoModel.getGuid());

        launchVideoService(resourceRowId);

        // we're going back to the start conversation fragment
        if (conversationId > 0) {
            mActivity.getSupportFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack(); // pop the backstack - this is a new conversation
        }

    }

    private void launchVideoService(long resourceRowId) {
        Intent intent = new Intent();
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_RESOURCE_ID, resourceRowId);
        // NOTE: the part and total parts will change once that multi part chunks can be uploaded
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_PART, mPartNum);
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_TOTAL_PARTS, mTotalParts);
        if (getArguments().containsKey(FRAGMENT_ARG_TITLE)) {
            intent.putExtra(VideoUploadIntentService.TITLE_INTENT_ARG_KEY, getArguments().getString(FRAGMENT_ARG_TITLE));
        }
        intent.setClass(getActivity(), VideoUploadIntentService.class);
        getActivity().startService(intent);
    }

    private void prepareVideoModel(long conversationId, ArrayList<String> recipients) {

        // Prepare the model for sending the video
        if (mVideoModel == null) {

            mVideoModel = new VideoModel();
            mVideoModel.setSegmented(true);
            mVideoModel.setSegmentFileName(mFileDataName);
            mVideoModel.setSegmentFileExtension(mFileExt); // the file extenstion or container
            mVideoModel.setState(VideoModel.ResourceState.PENDING_UPLOAD);
            mVideoModel.setCreateDate(TimeUtil.FORMAT_ISO8601(new Date()));
            mVideoModel.setSenderName("me");
            mVideoModel.setNumParts(mTotalParts);
            mVideoModel.setRead(true); // since we recorded this, we've actually seen it too
            mVideoModel.setGuid(mGuid);
            if (recipients != null) {
                mVideoModel.setRecipients(recipients.toArray(new String[] {}));

                Log.d(TAG, "recipient: " + mVideoModel.getRecipients()[0]);
            }

            // TODO: if there's a conversation id then put it here
            if (conversationId > 0) {
                mVideoModel.setConversationId(conversationId);
                updateConversationTime(conversationId);
            }

            Log.d(TAG, "saving model");
            mVideoModel.save();

        }

    }

    private void notifyTargetFragment(long rowId, String guid) {
        Bundle info = new Bundle();
        info.putLong(RecordingInfo.RESOURCE_ROW_ID, rowId);
        info.putInt(RecordingInfo.RECORDED_PARTS, mTotalParts);
        info.putBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, true);
        info.putString(RecordingInfo.RESOURCE_GUID, guid);
        if (getTargetFragment() != null) {
            ((RecordingInfo) getTargetFragment()).onRecordingFinished(info);
        } else { // XXX: Create a unified place for launching toasts, like in a toast receiver
            Context c = HollerbackApplication.getInstance();
            Toast.makeText(c, c.getString(R.string.message_sent_simple), Toast.LENGTH_LONG).show();
        }
    }

    private void updateConversationTime(long conversationId) {
        String timeStamp = TimeUtil.FORMAT_ISO8601(new Date());

        Log.d(TAG, "new convo timestamp: " + timeStamp);
        ActiveAndroidUpdateTask updateTimeTask = new ActiveAndroidUpdateTask(new Update(ConversationModel.class) //
                .set(ActiveRecordFields.C_CONV_LAST_MESSAGE_AT + "='" + timeStamp + "'") //
                .where(ActiveRecordFields.C_CONV_ID + "=?", conversationId)); //
        updateTimeTask.setTaskListener(new Task.Listener() {

            @Override
            public void onTaskError(Task t) {
                Log.w(TAG, t.getClass().getSimpleName() + " failed");
            }

            @Override
            public void onTaskComplete(Task t) {
                Log.d(TAG, "updated the conversation");
                // broadcast that the conversations have been updated
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));

            }
        });
        new TaskExecuter(true).executeTask(updateTimeTask); // run on a serial executer
    }

    private void inviteAndRecordVideo() {
        Log.d(TAG, "invite and record video");
        if (mPhones == null || mPhones.length == 0) {
            // TODO: remove for prod
            throw new IllegalStateException("Incorrect phone supplied" + mPhones);
        }

        String[] phones = mPhones;

        ArrayList<String> contacts = new ArrayList<String>();
        contacts.addAll(Arrays.asList(phones));

        Log.d(TAG, "first contact: " + contacts.get(0));

        // TODO - Sajjad: get the file info passed in to "inviteAndRecord"
        createNewConversation(contacts);

    }

    Runnable timeTask = new Runnable() {

        @Override
        public void run() {
            secondsPassed += 1;
            String seconds = String.valueOf(timer - secondsPassed) + "s";
            mTimer.setText(seconds);
            mHandler.removeCallbacks(timeTask);
            if (secondsPassed >= timer) {
                if (isRecording()) { // send the video once recording has stopped

                    mHandler.removeCallbacks(timeTask); // remove
                    stopRecording();

                    if (mConversationId < 0) {
                        Log.d(TAG, "attempt to create new conversation");
                        inviteAndRecordVideo();
                    } else {
                        Log.d(TAG, "attempt to post to existing conversation");
                        postToConversation(mConversationId);
                    }
                }
            } else {
                mHandler.postDelayed(timeTask, 1000);
            }
        }
    };
    private ProgressDialog mProgressDialog;
    private ImageView mBlinker;

    @Override
    public void onPause() {

        try {
            // not a great idea, but how to prevent camera failures?
            mCameraSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mIsExiting = true;

        Log.d(TAG, "onPause");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mHandler.removeCallbacks(timeTask); // stop the timeer task from runnin

        if (isRecording()) {

            // push these off to the background thread
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock(); // take camera access back from MediaRecorder
            clearRecordingFlag();

            // cleanup
            deleteRecording();

            // TODO: delete the video as cleanup and remove the model

            if (getTargetFragment() != null) {
                Bundle recordingInfo = new Bundle();
                recordingInfo.putBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, false);
                recordingInfo.putLong(RecordingInfo.RESOURCE_ROW_ID, -1);
                ((RecordingInfo) getTargetFragment()).onRecordingFinished(recordingInfo);
            }

            // Broadcast that recording was cancelled
            IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_CANCELLED));

            if (!isRemoving()) { // if user paused or something happened, just pop the backstack
                getFragmentManager().popBackStack(); // pop the back stack
            }

            // fire off a ttyl
            Intent intent = new Intent();
            intent.setClass(getActivity(), TTYLService.class);
            intent.putExtra(TTYLService.CONVO_ID_INTENT_ARG_KEY, mConversationId);
            getActivity().startService(intent);

        } else if (!hasRecordingStarted()) { // recording hasn't event started yet and we're getting kicked out
            Log.d(TAG, "recording hasn't started so lets just cancel");
            if (getTargetFragment() != null) {
                Bundle recordingInfo = new Bundle();
                recordingInfo.putBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, false);
                recordingInfo.putLong(RecordingInfo.RESOURCE_ROW_ID, -1);
                ((RecordingInfo) getTargetFragment()).onRecordingFinished(recordingInfo);
            }

            // Broadcast that recording was cancelled
            IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_CANCELLED));

            if (!isRemoving()) {
                if (mConversationId > 0) {
                    mActivity.getSupportFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    getFragmentManager().popBackStack(); // pop the backstack - this is a new conversation
                }
            }
        }

        enableShutterSound();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        // TODO: put correct sajjad
        // mActivity.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        inPreview = false;

        super.onPause();

        mCameraSemaphore.release();

    }

    private boolean isRecording() {
        return mIsRecording;
    }

    private void setRecordingFlag() {
        mIsRecording = true;
    }

    private void clearRecordingFlag() {
        mIsRecording = false;
    }

    private void setRecordingStarted() {
        mHasRecordingStarted = true;
    }

    private boolean hasRecordingStarted() {
        return mHasRecordingStarted;
    }

    // TODO: Clean this up!!!!!
    private void initCamera() {

        BackgroundHelper.getInstance().mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (!openCamera()) // don't do anything if we couldn't open the camera for any reason
                    return;

                setBestPreviewAndVideoSizes();

                mHandler.post(new Runnable() { // camera preview must be updated on ui thread

                    @Override
                    public void run() {

                        mCameraPreview.setAspectRatio((double) mBestVideoSize.width / (double) mBestVideoSize.height); // adjust the surfaceview

                        BackgroundHelper.getInstance().mHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                if (mCamera == null) // return if anything is going on
                                    return;

                                try {
                                    mCameraSemaphore.acquire();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (mIsExiting) {
                                    mCameraSemaphore.release();
                                    Log.d(TAG, "exiting due to being paused");
                                    return;
                                }

                                boolean previewStarted = startPreview(mCurrentCameraId);

                                if (previewStarted) {

                                    if (!isRecording() && isResumed()) { // start the recording only if we're not recording and we're in the resumed state
                                        Log.d(TAG, "start recording");
                                        startRecording();
                                    } else if (!isResumed()) {
                                        Log.d(TAG, "not starting recording due to being paused");
                                    }
                                }

                                mProgressDialog.dismiss();

                                if (mIsSwitching) {
                                    mHandler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() { // don't allow a switch for at least 500msec
                                            synchronized (RecordVideoFragment.this) {
                                                mIsSwitching = false;
                                            }

                                        }
                                    }, 500);

                                }

                                mCameraSemaphore.release();
                            }
                        });
                    }
                });
            }
        });

    }

    protected void startRecording() {

        try {
            if (prepareVideoRecorder()) {

                recorder.start();

                setRecordingFlag();
                setRecordingStarted();
                mHandler.removeCallbacks(timeTask);
                mHandler.post(timeTask); // enable the time task

            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        } catch (java.lang.RuntimeException e) {
            e.printStackTrace();
            // Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();
            releaseMediaRecorder();
            onRecordingFailed();

        }
    }

    /**
     * 
     * @return whether recording was stopped successfully or not
     */
    protected boolean stopRecording() {
        // stop recording and release camera
        try {
            recorder.stop();

        } catch (java.lang.RuntimeException e) {

            onRecordingFailed();

            return false;

        } finally {
            clearRecordingFlag();
            recorder.reset();
            recorder.release();
        }

        try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mHandler.removeCallbacks(timeTask);

        // Precondition: User decides to send video (no ttyl without video, etc)
        return true;
    }

    private String getNewFileName() {

        if (mGuid == null) {
            mGuid = UUID.randomUUID().toString();
            mFileDataName = HBFileUtil.generateFileNameFromGUID(mGuid);
        }

        mFileExt = "mp4"; // although get this info from the output format type

        ++mTotalParts; // part info will change as video segments get recorded

        mFileDataPath = HBFileUtil.getOutputVideoFile(new StringBuilder(128).append(mFileDataName).append(".").append(mPartNum).append(".").append(mFileExt).toString()).toString();

        ++mPartNum;

        Log.d(TAG, "mFileDataName: " + mFileDataName + " path: " + mFileDataPath);

        return mFileDataPath;
    }

    private boolean prepareVideoRecorder() {

        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CameraUtil.printAllCamcorderProfiles(CameraInfo.CAMERA_FACING_FRONT);

        // Step 3: Configure Camera
        CameraUtil.setRecordingParams(recorder, mBestVideoSize.width, mBestVideoSize.height);
        // recorder.setProfile(CamcorderProfile.get(mCurrentCameraId, CamcorderProfile.QUALITY_LOW));

        // recorder.setvideoextension
        targetExtension = HBFileUtil.getFileFormat(OutputFormat.MPEG_4);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mCurrentCameraId == CameraInfo.CAMERA_FACING_FRONT) {
                if (Build.MANUFACTURER.equals("HTC")) {
                    recorder.setOrientationHint(90);
                } else {
                    recorder.setOrientationHint(270);
                }
            } else {
                recorder.setOrientationHint(90);
            }
        }

        // Step 4: Set output file
        recorder.setOutputFile(getNewFileName());

        // Step 5: Set Preview Display
        if (USE_SURFACE_VIEW)
            recorder.setPreviewDisplay(((PreviewSurfaceView) mCameraPreview).getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "TODO: delete files");
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mSpecialOrientationRequest) {
            // mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        mActivity.getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
        mActivity.getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        mActivity.getActionBar().show();

    }

    @Override
    public void onDestroy() {

        inPreview = false;
        super.onDestroy();
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset(); // clear configuration (optional here)
            recorder.release();
            recorder = null;
        }
    }

    private void broadcastFailure() {
        IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_FAILED));
    }

    private void onRecordingFailed() {
        Log.d(TAG, "recording failed");
        deleteRecording();
        getFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, 0);
        broadcastFailure();
    }

    private void deleteRecording() {
        if (mFileDataName != null) {
            for (int i = 0; i < mTotalParts; i++) {
                File f = new File(HBFileUtil.getLocalFile(mFileDataName + "." + i + ".mp4"));
                if (f.exists()) {
                    Log.d(TAG, "deleting: " + f.getPath());
                    f.delete();
                }
            }
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceCreated = true;

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Loading Camera...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            initCamera();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        BackgroundHelper.getInstance().mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
                inPreview = false;

            }
        });

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;

        mSurfaceCreated = true;

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Loading Camera...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            initCamera();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface changed: " + width + " x " + height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        inPreview = false;
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     * @return true for a successfull open or false for unsuccessfull open
     */
    private boolean openCamera() {
        Log.d(TAG, "opening " + mCurrentCameraId + " camera");

        // handle case where the open gives us a runtime exception
        int numRetries = 0;
        do {

            try {

                mCamera = Camera.open(mCurrentCameraId);

                if (mCamera == null)
                    ++numRetries;

            } catch (RuntimeException e) {
                e.printStackTrace();
                ++numRetries;
                mCamera = null;
            }

        } while (numRetries <= 1 && mCamera == null);

        if (mCamera == null) {
            Toast.makeText(HollerbackApplication.getInstance(), "camera cannot be opened", Toast.LENGTH_SHORT).show();
            return false;
        }

        disableShutterSound();

        return true;

    }

    private void setBestPreviewAndVideoSizes() {
        // logic:
        // 1. see if there is a preferred video size, if so just use that

        // 2. if there are no preferred video size, we must use the preview size

        // query the best size / aspect ratio
        mBestVideoSize = CameraUtil.getPreferredSize(mCamera.getParameters().getSupportedVideoSizes(), PREFERRED_VIDEO_WIDTH, PREFERRED_VIDEO_HEIGHT);
        if (mBestVideoSize != null) { // we're done if we found a best video size

            // we want our preview to have the highest quality, therefore we multiply the width and height by 10 so the optimal size is found
            mBestPreviewSize = CameraUtil.getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), mBestVideoSize.width * 10, mBestVideoSize.height * 10);
            Log.d(TAG, "mBestVideoSize:" + mBestVideoSize.width + "x" + mBestVideoSize.height);
            Log.d(TAG, "mBestPreviewSize: " + mBestPreviewSize.width + "x" + mBestPreviewSize.height);

            return;
        }

        // camera can't support different video/preview sizes; therefore, lets get the optimal preview size
        mBestPreviewSize = CameraUtil.getPreferredSize(mCamera.getParameters().getSupportedPreviewSizes(), PREFERRED_VIDEO_WIDTH, PREFERRED_VIDEO_HEIGHT);
        mBestVideoSize = mBestPreviewSize;

        if (mBestPreviewSize == null) {
            Log.e(TAG, "nothing suitable found for recording");
            // XXX: log this
            return;
        }

        Log.d(TAG, "mBestPreviewSize: " + mBestPreviewSize.width + "x" + mBestPreviewSize.height);
    }

    /**
     * 
     * @param cameraId
     * @return whether preview was started or not
     */
    private boolean startPreview(int cameraId) {

        stopPreview();

        configureCamera(cameraId);

        try {

            if (USE_SURFACE_VIEW) {
                mCamera.setPreviewDisplay(((PreviewSurfaceView) mCameraPreview).getHolder());
            } else {
                mCamera.setPreviewTexture(((PreviewTextureView) mCameraPreview).getSurfaceTexture());
            }
            mCamera.startPreview();
            inPreview = true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void configureCamera(int cameraId) {

        CameraUtil.setCameraDisplayOrientation(getActivity(), cameraId, mCamera);
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mBestPreviewSize.width, mBestPreviewSize.height);
        mCamera.setParameters(params);

    }

    private boolean stopPreview() {
        if (inPreview) {
            try {
                mCamera.stopPreview();
                inPreview = false;
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private void switchRecordingCamerasTo(final int cameraId) {

        if (!isAdded()) {
            Log.w(TAG, "switch request when fragment not added");
            return;
        }

        synchronized (RecordVideoFragment.this) {
            if (mIsSwitching) {
                Log.w(TAG, "switch requested while switching");
                return;
            }

            mIsSwitching = true;
        }

        BackgroundHelper.getInstance().mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isRecording()) {

                    if (!stopRecording()) {
                        Log.w(TAG, "attempting to stop recording failed");
                        return;
                    }
                }

                if (!stopPreview()) {
                    return;
                }

                mCamera.release();

                mCamera = null;

                // swap cameras
                mCurrentCameraId = cameraId; // lets change the cameras to the back camera

                initCamera();
            }
        });

        mProgressDialog.show();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void disableShutterSound() {
        boolean shutterSoundDisabled = false;
        if (Build.VERSION.SDK_INT >= 17)
            if (mCamera != null)
                shutterSoundDisabled = mCamera.enableShutterSound(false);

        if (!shutterSoundDisabled) {
            AudioManager am = (AudioManager) HollerbackApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
            mVolumeBeforeShutoff = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void enableShutterSound() {

        if (Build.VERSION.SDK_INT >= 17) {
            if (mCamera != null)
                mCamera.enableShutterSound(false);
        }

        if (mVolumeBeforeShutoff > 0) {
            AudioManager am = (AudioManager) HollerbackApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, mVolumeBeforeShutoff, 0);
        }

    }

    private int getOppositeCamera() {
        switch (mCurrentCameraId) {
            case CameraInfo.CAMERA_FACING_BACK:
                return CameraInfo.CAMERA_FACING_FRONT;

            default:
                return CameraInfo.CAMERA_FACING_BACK;
        }
    }

    /**
     * This class manages and handles touch events to the camera preview
     * @author sajjad
     *
     */
    private class PreviewTouchDelegate {

        private boolean mIsPressed = false;
        private static final long HOLD_TIMEOUT = 300;

        private long tapDownTime = 0;

        // TODO - sajjad: We're really not using this anymore
        private GestureDetectorCompat mPreviewGestureDetector = new GestureDetectorCompat(mActivity, new GestureDetector.SimpleOnGestureListener() {

        });

        public boolean isPressed() {
            return mIsPressed;
        }

        OnTouchListener mOnPreviewTouchListener = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP && mIsPressed) {
                    // Log.d(TAG, "switching camera back to front camera");
                    mIsPressed = false;

                    // requestSwitch(CameraInfo.CAMERA_FACING_FRONT);
                    // switchRecordingCamerasTo(CameraInfo.CAMERA_FACING_FRONT);

                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tapDownTime = System.currentTimeMillis();
                    return true;
                }

                if (mPreviewGestureDetector.onTouchEvent(event)) {
                    return true;
                }

                if (mIsPressed == false && (System.currentTimeMillis() - tapDownTime) > HOLD_TIMEOUT) {
                    mIsPressed = true;
                    // switch the cameras
                    // requestSwitch(CameraInfo.CAMERA_FACING_BACK);
                    Log.d(TAG, "switching camera to back camera");
                    return true;
                }

                return false;

            }
        };

    }

    public static class BackgroundHelper extends Thread {
        private static BackgroundHelper sInstance;

        private Looper mLooper;
        public Handler mHandler;

        public static BackgroundHelper getInstance() {
            if (sInstance == null) {
                sInstance = new BackgroundHelper();
                sInstance.start();
            }

            return sInstance;
        }

        private BackgroundHelper() {
        }

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new CameraHandler();
            Looper.loop();
            mLooper = Looper.myLooper();
        }

        public static class CameraHandler extends Handler {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    default:
                        super.handleMessage(msg);
                }

            }
        }
    }

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.VIDEO_RECORD;
    }

}
