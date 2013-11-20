package com.moziy.hollerback.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.activeandroid.query.Update;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.VideoUploadIntentService;
import com.moziy.hollerback.service.VideoUploadService;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.util.CameraUtil;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.ImageUtil;
import com.moziy.hollerback.util.TimeUtil;

public class RecordVideoFragment extends BaseFragment {

    public static final String FRAGMENT_ARG_WATCHED_IDS = "watched_ids";
    public static final String FRAGMENT_ARG_PHONES = "phones";
    public static final String FRAGMENT_ARG_TITLE = "title";
    public static final String FRAGMENT_ARG_GOTO_CONVO = "to_conversation";

    public static interface RecordingInfo {
        public static final String RECORDED_PARTS = "recorded_parts";
        public static final String RESOURCE_ROW_ID = "resource_row_id";

        public void onRecordingFinished(Bundle info);
    }

    protected ViewGroup mRootView;

    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera mCamera = null;
    private boolean inPreview = false;
    private boolean mToConversation = false;
    TextView mTimer;
    final Handler mHandler = new Handler();

    int VIDEO_SENT = 4;

    int secondsPassed;
    private int currentCameraId = CameraInfo.CAMERA_FACING_FRONT;

    View mTopView, mBottomView;

    private String mFileDataPath;
    protected String mFileDataName;
    private String mFileExt; // the file extension

    private VideoModel mVideoModel; // the model that represents the resource that will get uploaded

    private int mPartNum;
    private int mTotalParts;

    private ArrayList<String> mWatchedIds;

    int timer = 20;

    public static String TAG = "VideoApp";

    private boolean isRecording = false;

    static MediaRecorder recorder;

    float targetPreviewWidth;
    float targetPreviewHeight;
    String targetExtension;

    // Preview shit
    private View mPreviewParentView;
    private VideoView mPreviewVideoView;
    private ImageButton mRecordButton, mPreviewPlayBtn, mSwitchButton, mFilterButton;
    protected Button mSendButton;
    private ImageView mImagePreview;
    private long mConversationId = -1;
    private TextView mTxtPlaying;

    private String[] mPhones = null;
    ViewPager mFilterPagers;

    int mBestCameraWidth, mBestCameraHeight;

    public static RecordVideoFragment newInstance(long conversationId, String title, ArrayList<String> watchedIds) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(IABIntent.PARAM_ID, conversationId);
        bundle.putString(FRAGMENT_ARG_TITLE, title);
        bundle.putStringArrayList(FRAGMENT_ARG_WATCHED_IDS, watchedIds);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static RecordVideoFragment newInstance(long conversationId, boolean toConversation, String title, ArrayList<String> watchedIds) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(IABIntent.PARAM_ID, conversationId);
        bundle.putString(FRAGMENT_ARG_TITLE, title);
        bundle.putBoolean(FRAGMENT_ARG_GOTO_CONVO, toConversation);
        bundle.putStringArrayList(FRAGMENT_ARG_WATCHED_IDS, watchedIds);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity.getSupportActionBar().setTitle(R.string.action_record);
        mActivity.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle args = getArguments();

        // bind arguments to fragment
        if (args.containsKey(FRAGMENT_ARG_TITLE)) {
            // setting title
            mActivity.getSupportActionBar().setTitle(args.getString(FRAGMENT_ARG_TITLE));
        }

        if (args.containsKey(IABIntent.PARAM_ID)) {
            mConversationId = args.getLong(IABIntent.PARAM_ID);
            LogUtil.i("HollerbackCamera CONVO: " + mConversationId);
        }

        mPhones = args.getStringArray(FRAGMENT_ARG_PHONES);
        mWatchedIds = args.getStringArrayList(FRAGMENT_ARG_WATCHED_IDS);
        mToConversation = args.getBoolean(FRAGMENT_ARG_GOTO_CONVO, false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // this is to make sure the camera runs square when allowed, otherwise we are using
        // the mask version
        try {
            mCamera = Camera.open(currentCameraId);
            if (CameraUtil.HasSquareCamera(mCamera.getParameters())) {
                mRootView = (ViewGroup) inflater.inflate(R.layout.custom_camera_square, null);
            } else {
                mRootView = (ViewGroup) inflater.inflate(R.layout.custom_camera, null);
            }
            mCamera.release();
            mCamera = null;

        } catch (RuntimeException e) {
            Log.e("Hollerback", "Camera failed to open: " + e.getLocalizedMessage());
            Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();

            onRecordingFailed();

            return null;
        }

        mTopView = mRootView.findViewById(R.id.top_bar);
        mBottomView = mRootView.findViewById(R.id.bottom_bar);
        mSendButton = (Button) mRootView.findViewById(R.id.send_button);

        mPreviewParentView = mRootView.findViewById(R.id.rl_video_preview);
        mPreviewVideoView = (VideoView) mRootView.findViewById(R.id.vv_video_preview);
        mPreviewPlayBtn = (ImageButton) mRootView.findViewById(R.id.ib_play_btn);
        mImagePreview = (ImageView) mRootView.findViewById(R.id.iv_video_preview);
        mFilterButton = (ImageButton) mRootView.findViewById(R.id.ib_filter_btn);
        mTxtPlaying = (TextView) mRootView.findViewById(R.id.txtPlaying);

        preview = (SurfaceView) mRootView.findViewById(R.id.surface);
        preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isRecording) { // send the video once recording has stopped

                    stopRecording();

                    if (mConversationId < 0) {
                        Log.d(TAG, "attempt to create new conversation");
                        inviteAndRecordVideo();
                    } else {
                        Log.d(TAG, "attempt to post to existing conversation");
                        postToConversation(mConversationId, mWatchedIds);
                    }
                }
            }
        });

        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        targetPreviewWidth = 480;
        targetPreviewHeight = 480;

        // this 1.5 i guess assumes 640 x 480
        previewHolder.setFixedSize(mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth(),
                (int) (mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight)));

        RelativeLayout.LayoutParams mImagePreviewParams = (RelativeLayout.LayoutParams) mImagePreview.getLayoutParams();
        mImagePreviewParams.height = (int) (mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight));

        mImagePreview.setLayoutParams(mImagePreviewParams);

        mRecordButton = (ImageButton) mRootView.findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        mTimer = (TextView) mRootView.findViewById(R.id.timer);

        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // if (mConversationId < 0) {
                // // HBRequestManager
                // // .postConversations(TempMemoryStore.invitedUsers);
                // LogUtil.e("Conversation ID NULL");
                // mSendButton.setEnabled(false);
                // inviteAndRecordVideo();
                // } else {
                // uploadAndSend();
                // }
            }
        });

        mSwitchButton = (ImageButton) mRootView.findViewById(R.id.btnSwitch);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        return mRootView;
    }

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
                this.getFragmentManager().popBackStack();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initializeView(View view) {
        // TODO: write up this portion in cleanup
    }

    private void createNewConversation(ArrayList<String> contacts) {
        sendVideo(-1, contacts, null);
    }

    private void postToConversation(long conversationId, ArrayList<String> watchedIds) {
        sendVideo(conversationId, null, watchedIds);
    }

    /**
     * 
     * @param fileName
     * @param contacts
     */
    private void sendVideo(long conversationId, ArrayList<String> recipients, ArrayList<String> watchedIds) {

        // Prepare the model for sending the video
        if (mVideoModel == null) {

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            mVideoModel = new VideoModel();
            mVideoModel.setSegmented(true);
            mVideoModel.setSegmentFileName(mFileDataName);
            mVideoModel.setSegmentFileExtension(mFileExt); // the file extenstion or container
            mVideoModel.setState(VideoModel.ResourceState.PENDING_UPLOAD);
            mVideoModel.setCreateDate(df.format(new Date()));
            mVideoModel.setSenderName("me");
            mVideoModel.setRead(true); // since we recorded this, we've actually seen it too
            mVideoModel.setGuid(UUID.randomUUID().toString());
            if (recipients != null) {
                mVideoModel.setRecipients(recipients.toArray(new String[] {}));

                Log.d(TAG, "recipient: " + mVideoModel.getRecipients()[0]);
            }

            // TODO: if there's a conversation id then put it here
            if (conversationId > 0) {
                mVideoModel.setConversationId(conversationId);
                updateConversationTime(conversationId);
            }

            mVideoModel.save();

        }

        // TODO - Sajjad: Bind this resource to the conversation list so that we can mark the conversation as uploading
        long resourceRowId = mVideoModel.getId();
        Bundle info = new Bundle();
        info.putLong(RecordingInfo.RESOURCE_ROW_ID, resourceRowId);
        info.putInt(RecordingInfo.RECORDED_PARTS, mTotalParts);
        if (getTargetFragment() != null) {
            ((RecordingInfo) getTargetFragment()).onRecordingFinished(info);
        }

        Intent intent = new Intent();
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_RESOURCE_ID, resourceRowId);

        // NOTE: the part and total parts will change once that multi part chunks can be uploaded
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_PART, mPartNum);
        intent.putExtra(VideoUploadIntentService.INTENT_ARG_TOTAL_PARTS, mTotalParts);
        intent.setClass(getActivity(), VideoUploadIntentService.class);
        getActivity().startService(intent);

        // we're going back to the start conversation fragment
        mActivity.getSupportFragmentManager().popBackStack();

        if (mToConversation) { // when do we go back to the conversation?

            // ConversationFragment fragment = ConversationFragment.newInstance(mConversationId);
            // mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            // .addToBackStack(ConversationFragment.class.getSimpleName()).remove(RecordVideoFragment.this).commitAllowingStateLoss();
        }

    }

    private void updateConversationTime(long conversationId) {
        String timeStamp = TimeUtil.SERVER_TIME_FORMAT.format(new Date());
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
                // broadcast that the conversations have been updated
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));

            }
        });
        new TaskExecuter().executeTask(updateTimeTask);
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
            if (secondsPassed >= timer) {
                stopRecording();
            }

            mHandler.postDelayed(timeTask, 1000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (this.isVisible()) {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.background_camera));
        }
        try {
            mCamera = Camera.open(currentCameraId);

            Log.e("Hollerback", "Camera successfully opened");
        } catch (RuntimeException e) {
            Log.e("Hollerback", "Camera failed to open: " + e.getLocalizedMessage());
            Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();

            onRecordingFailed();
        }

        previewHolder.addCallback(surfaceCallback);

        if (!isUploadRunning()) {
            Intent serviceIntent = new Intent(mActivity, VideoUploadService.class);
            mActivity.startService(serviceIntent);
        }
    }

    @Override
    public void onPause() {
        if (isRecording) {
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock(); // take camera access back from MediaRecorder
            isRecording = false;
            mHandler.removeCallbacks(timeTask); // stop the timeer task from runnin
            // TODO: delete the video as cleanup and remove the model

            // Broadcast that recording was cancelled
            IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_CANCELLED));

        }

        if (mCamera != null) {
            if (inPreview) {
                mCamera.stopPreview();
            }
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        mActivity.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        inPreview = false;
        super.onPause();
    }

    protected void startRecording() {
        mSwitchButton.setEnabled(false);
        mSendButton.setVisibility(View.GONE);

        mSwitchButton.setVisibility(View.GONE);
        mFilterButton.setVisibility(View.GONE);
        mTimer.setTextColor(mActivity.getResources().getColor(R.color.timer_green));
        mRecordButton.setBackgroundResource(R.drawable.green_recording_spinner);
        Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.rotation_reverse_clockwise);
        rotation.setRepeatCount(Animation.INFINITE);
        mRecordButton.startAnimation(rotation);
        mTxtPlaying.setText(R.string.tap_stop);

        // intialize the part info
        mPartNum = 0;
        mTotalParts = 1;

        try {
            if (prepareVideoRecorder()) {
                mTimer.setText("20s");
                // Camera is available and unlocked, MediaRecorder is
                // prepared,
                // now you can start recording

                recorder.start();

                // inform the user that recording has started
                // mRecordButton.setImageResource(R.drawable.stop_button);
                isRecording = true;
                mHandler.postDelayed(timeTask, 1000);
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        } catch (java.lang.RuntimeException e) {
            Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();

            onRecordingFailed();

        }
    }

    protected void stopRecording() {
        // stop recording and release camera
        try {
            recorder.stop();
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock(); // take camera access back from MediaRecorder
        } catch (java.lang.RuntimeException e) {

            onRecordingFailed();
            return;

        }

        mRecordButton.clearAnimation();
        mRecordButton.setBackgroundResource(R.drawable.recording_spinner);
        mTimer.setText(String.valueOf(secondsPassed) + "s");
        mTimer.setTextColor(mActivity.getResources().getColor(R.color.timer_default));
        mTxtPlaying.setVisibility(View.GONE);

        // inform the user that recording has stopped
        // mRecordButton.setImageResource(R.drawable.record_button);
        isRecording = false;

        if (mFileDataPath != null) {
            // mRecordButton.setVisibility(View.GONE);
            mRecordButton.setEnabled(false);
            mRecordButton.setClickable(false);
            mSendButton.setVisibility(View.VISIBLE);
            mSwitchButton.setVisibility(View.GONE);
        }
        mHandler.removeCallbacks(timeTask);
        secondsPassed = 0;

        displayPreview();
        ImageUtil.generateThumbnail(mFileDataName + "." + mFileExt);

        // TODO: Review code segment above

        // Precondition: User decides to send video (no ttyl without video, etc)
    }

    public void displayPreview() {

        if (inPreview) {
            mCamera.stopPreview();
        }

        mImagePreview.setVisibility(View.VISIBLE);

        preview.setVisibility(View.GONE);

        inPreview = false;

        mPreviewParentView.setVisibility(View.VISIBLE);
        mPreviewVideoView.setVisibility(View.VISIBLE);
        mPreviewPlayBtn.setVisibility(View.VISIBLE);

        mFilterButton.setVisibility(View.GONE);

        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(HBFileUtil.getLocalFile(mFileDataName + "." + mFileExt), Thumbnails.FULL_SCREEN_KIND);
        mImagePreview.setBackgroundDrawable(new BitmapDrawable(bmThumbnail));

        mPreviewVideoView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mPreviewPlayBtn.setEnabled(true);
            }
        });

        mPreviewPlayBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playVideo(mFileDataName + "." + mFileExt);
                mPreviewPlayBtn.setEnabled(false);
            }
        });

    }

    private String getNewFileName() {

        mFileDataName = HBFileUtil.generateRandomFileName();

        mFileExt = "mp4"; // although get this info from the output format type

        mPartNum = 0; // part info will change as video segments get recorded

        mTotalParts = 1; // part info will change as video segments get recorded

        mFileDataPath = HBFileUtil.getOutputVideoFile(new StringBuilder(128).append(mFileDataName).append(".").append(mPartNum).append(".").append(mFileExt).toString()).toString();

        Log.d(TAG, "mFileDataName: " + mFileDataName + " path: " + mFileDataPath);

        return mFileDataPath;
    }

    private void playVideo(String fileKey) {

        mImagePreview.setVisibility(View.GONE);

        mPreviewVideoView.setVideoPath(HBFileUtil.getLocalFile(fileKey));

        mPreviewVideoView.requestFocus();
        mPreviewVideoView.start();
        LogUtil.i("vid size: " + mPreviewVideoView.getHeight() + mPreviewVideoView.getWidth());
    }

    private boolean prepareVideoRecorder() {

        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)

        CameraUtil.setFrontFacingParams(recorder, mBestCameraWidth, mBestCameraHeight);

        targetExtension = HBFileUtil.getFileFormat(OutputFormat.MPEG_4);

        CamcorderProfile prof = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);

        LogUtil.i("Record size: " + prof.videoFrameWidth + " " + prof.videoFrameHeight);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recorder.setOrientationHint(270);
        }

        // Step 4: Set output file
        recorder.setOutputFile(getNewFileName());

        // Step 5: Set the preview output
        recorder.setPreviewDisplay(preview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (inPreview) {
            mCamera.stopPreview();
        }
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
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

    private void switchCamera() {
        if (inPreview) {
            mCamera.stopPreview();
        }
        // NB: if you don't release the current camera before switching, you app will crash
        mCamera.release();

        // swap the id of the camera to be used
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = Camera.open(currentCameraId);

        setCameraDisplayOrientation(mActivity, currentCameraId, mCamera);
        try {

            mCamera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void broadcastFailure() {
        IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_FAILED));
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surface created");
            try {
                mCamera.setPreviewDisplay(previewHolder);
                mCamera.setDisplayOrientation(90);
                MediaRecorder m = new MediaRecorder();
                m.setCamera(mCamera);
                m.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                m.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            } catch (Throwable t) {
                Log.e("SurfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surface changed");
            if (mCamera == null) {
                onRecordingFailed();
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = CameraUtil.getBestPreviewSize((int) targetPreviewWidth, (int) targetPreviewWidth, parameters);
            LogUtil.i("Best size: " + size.width + " " + size.height);

            mBestCameraWidth = size.width;
            mBestCameraHeight = size.height;

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
                inPreview = true;
            }

            // once a valid surface has been created and
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!isRecording) {
                        startRecording();
                    }

                }
            });

        }

        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void onRecordingFailed() {

        getFragmentManager().popBackStack();
        broadcastFailure();
    }

    /**
     * check if service is running
     * @param tmp
     * @return
     */
    public boolean isUploadRunning() {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (VideoUploadService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
