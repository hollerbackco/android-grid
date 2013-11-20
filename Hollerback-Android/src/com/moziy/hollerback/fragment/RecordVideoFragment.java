package com.moziy.hollerback.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    // protected ViewGroup mRootView;
    protected Preview mRootView;

    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera mCamera = null;
    private boolean inPreview = false;
    private boolean mToConversation = false;
    TextView mTimer;
    private final Handler mHandler = new Handler();

    int VIDEO_SENT = 4;

    int secondsPassed;
    private int currentCameraId = CameraInfo.CAMERA_FACING_FRONT;

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

        // mActivity.getSupportActionBar().setTitle(R.string.action_record);
        mActivity.getSupportActionBar().hide();
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

            // mRootView = (ViewGroup) inflater.inflate(R.layout.custom_camera, null);
            mRootView = new Preview(getActivity());

            // mCamera.release();
            // mCamera = null;

        } catch (RuntimeException e) {
            Log.e("Hollerback", "Camera failed to open: " + e.getLocalizedMessage());
            Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();

            onRecordingFailed();

            return null;
        }

        // mSendButton = (Button) mRootView.findViewById(R.id.send_button);
        //
        // mPreviewParentView = mRootView.findViewById(R.id.rl_video_preview);
        // mPreviewVideoView = (VideoView) mRootView.findViewById(R.id.vv_video_preview);
        // mPreviewPlayBtn = (ImageButton) mRootView.findViewById(R.id.ib_play_btn);
        // mImagePreview = (ImageView) mRootView.findViewById(R.id.iv_video_preview);
        // mFilterButton = (ImageButton) mRootView.findViewById(R.id.ib_filter_btn);
        // mTxtPlaying = (TextView) mRootView.findViewById(R.id.txtPlaying);
        //
        // preview = (SurfaceView) mRootView.findViewById(R.id.surface);
        mRootView.setOnClickListener(new View.OnClickListener() {

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

        // previewHolder = preview.getHolder();
        // previewHolder.addCallback(surfaceCallback);
        // previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //sajjad - evaluate whether this is needed

        targetPreviewWidth = 480;
        targetPreviewHeight = 480;

        // TODO - sajjad: evaluate this previewholder
        // this 1.5 i guess assumes 640 x 480
        // previewHolder.setFixedSize(mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth(),
        // (int) (mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight)));

        // RelativeLayout.LayoutParams mImagePreviewParams = (RelativeLayout.LayoutParams) mImagePreview.getLayoutParams();
        // mImagePreviewParams.height = (int) (mActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight));
        //
        // mImagePreview.setLayoutParams(mImagePreviewParams);
        //
        // mRecordButton = (ImageButton) mRootView.findViewById(R.id.record_button);
        // mRecordButton.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // if (isRecording) {
        // stopRecording();
        // } else {
        // startRecording();
        // }
        // }
        // });
        //
        // mTimer = (TextView) mRootView.findViewById(R.id.timer);
        //
        // mSendButton.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View arg0) {
        // // if (mConversationId < 0) {
        // // // HBRequestManager
        // // // .postConversations(TempMemoryStore.invitedUsers);
        // // LogUtil.e("Conversation ID NULL");
        // // mSendButton.setEnabled(false);
        // // inviteAndRecordVideo();
        // // } else {
        // // uploadAndSend();
        // // }
        // }
        // });

        // mSwitchButton = (ImageButton) mRootView.findViewById(R.id.btnSwitch);
        // mSwitchButton.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // switchCamera();
        // }
        // });

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
            mRootView.setCamera(mCamera);

            // test
            // List<Size> sizes = mCamera.getParameters().getSupportedVideoSizes();
            // if (sizes == null) {
            // Log.d(TAG, "null size");
            // }
            // for (Size s : sizes) {
            // Log.d(TAG, "supported size: " + s.width + " x " + s.height);
            // }

            Log.e("Hollerback", "Camera successfully opened");
        } catch (RuntimeException e) {
            Log.e("Hollerback", "Camera failed to open: " + e.getLocalizedMessage());
            Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();

            onRecordingFailed();
        }

        // previewHolder.addCallback(surfaceCallback);

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
            mRootView.setCamera(null);
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        mActivity.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        inPreview = false;
        super.onPause();
    }

    protected void startRecording() {
        // mSwitchButton.setEnabled(false);
        // mSendButton.setVisibility(View.GONE);
        //
        // mSwitchButton.setVisibility(View.GONE);
        // mFilterButton.setVisibility(View.GONE);
        // mTimer.setTextColor(mActivity.getResources().getColor(R.color.timer_green));
        // mRecordButton.setBackgroundResource(R.drawable.green_recording_spinner);
        // Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.rotation_reverse_clockwise);
        // rotation.setRepeatCount(Animation.INFINITE);
        // mRecordButton.startAnimation(rotation);
        // mTxtPlaying.setText(R.string.tap_stop);

        // intialize the part info
        mPartNum = 0;
        mTotalParts = 1;

        try {
            if (prepareVideoRecorder()) {
                // mTimer.setText("20s");
                // Camera is available and unlocked, MediaRecorder is
                // prepared,
                // now you can start recording

                recorder.start();

                // inform the user that recording has started
                // mRecordButton.setImageResource(R.drawable.stop_button);
                isRecording = true;
                // mHandler.postDelayed(timeTask, 1000);
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

        // mRecordButton.clearAnimation();
        // mRecordButton.setBackgroundResource(R.drawable.recording_spinner);
        // mTimer.setText(String.valueOf(secondsPassed) + "s");
        // mTimer.setTextColor(mActivity.getResources().getColor(R.color.timer_default));
        // mTxtPlaying.setVisibility(View.GONE);

        // inform the user that recording has stopped
        // mRecordButton.setImageResource(R.drawable.record_button);
        isRecording = false;

        if (mFileDataPath != null) {
            // mRecordButton.setVisibility(View.GONE);
            // mRecordButton.setEnabled(false);
            // mRecordButton.setClickable(false);
            // mSendButton.setVisibility(View.VISIBLE);
            // mSwitchButton.setVisibility(View.GONE);
        }
        mHandler.removeCallbacks(timeTask);
        secondsPassed = 0;

        // displayPreview();
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

        // CameraUtil.setFrontFacingParams(recorder, 480, 360);
        CameraUtil.printAllCamcorderProfiles(CameraInfo.CAMERA_FACING_FRONT);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        // recorder.setProfile(CamcorderProfile.get(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_CIF));
        // get optimal width/heigh
        // Camera.Size size = CameraUtil.getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), 640, 480);
        // if (size == null) {
        // Log.d(TAG, "size is null");
        // }
        // Log.d(TAG, "optimal size w: " + size.width + " h: " + size.height);
        //
        // CameraUtil.setFrontFacingParams(recorder, 320, 240);
        recorder.setProfile(CamcorderProfile.get(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_QVGA));

        // recorder.setvi
        targetExtension = HBFileUtil.getFileFormat(OutputFormat.MPEG_4);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recorder.setOrientationHint(270);
        }

        // Step 4: Set output file
        recorder.setOutputFile(getNewFileName());

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
            // mCamera.stopPreview();
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

    // private void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
    // android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
    // android.hardware.Camera.getCameraInfo(cameraId, info);
    // int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    // int degrees = 0;
    // switch (rotation) {
    // case Surface.ROTATION_0:
    // degrees = 0;
    // break;
    // case Surface.ROTATION_90:
    // degrees = 90;
    // break;
    // case Surface.ROTATION_180:
    // degrees = 180;
    // break;
    // case Surface.ROTATION_270:
    // degrees = 270;
    // break;
    // }
    //
    // int result;
    // if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
    // result = (info.orientation + degrees) % 360;
    // result = (360 - result) % 360; // compensate the mirror
    // } else { // back-facing
    // result = (info.orientation - degrees + 360) % 360;
    // }
    // camera.setDisplayOrientation(result);
    // }

    private void broadcastFailure() {
        IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.RECORDING_FAILED));
    }

    // SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
    // public void surfaceCreated(SurfaceHolder holder) {
    // Log.d(TAG, "surface created");
    // try {
    // mCamera.setPreviewDisplay(previewHolder);
    // mCamera.setDisplayOrientation(90);
    // mCamera.getParameters().setRecordingHint(true);
    // MediaRecorder m = new MediaRecorder();
    // m.setCamera(mCamera);
    // m.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    // m.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    // } catch (Throwable t) {
    // Log.e("SurfaceCallback", "Exception in setPreviewDisplay()", t);
    // }
    // }
    //
    // public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // Log.d(TAG, "surface changed");
    // if (mCamera == null) {
    // onRecordingFailed();
    // return;
    // }
    // Camera.Parameters parameters = mCamera.getParameters();
    //
    // // TODO - sajjad: remove test
    // for (Size s : parameters.getSupportedPreviewSizes()) {
    // Log.d(TAG, "supported preview sizes: " + s.width + " " + s.height);
    // }
    // Camera.Size size = CameraUtil.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), width, height);// CameraUtil.getBestPreviewSize((int) targetPreviewWidth, (int)
    // // targetPreviewWidth, parameters);
    // LogUtil.i("Best size: " + size.width + " " + size.height);
    //
    // mBestCameraWidth = size.width;
    // mBestCameraHeight = size.height;
    //
    // if (size != null) {
    // // parameters.setPreviewSize(size.width, size.height);
    // parameters.setPreviewSize(720, 480);
    // mCamera.setParameters(parameters);
    // mCamera.setDisplayOrientation(90);
    // mCamera.startPreview();
    // inPreview = true;
    // }
    //
    // // once a valid surface has been created and
    // mHandler.post(new Runnable() {
    //
    // @Override
    // public void run() {
    // if (!isRecording) {
    // startRecording();
    // }
    //
    // }
    // });
    //
    // }
    //
    // public void surfaceDestroyed(SurfaceHolder holder) {
    //
    // }
    // };

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

    /**
     * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
     * to the surface. We need to center the SurfaceView because not all devices have cameras that
     * support preview sizes at the same aspect ratio as the device's display.
     */
    public class Preview extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "Preview";

        SurfaceView mSurfaceView;
        SurfaceHolder mHolder;
        Size mPreviewSize;
        List<Size> mSupportedPreviewSizes;
        Camera mCamera;

        Preview(Context context) {
            super(context);

            mSurfaceView = new SurfaceView(context);
            addView(mSurfaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void setCamera(Camera camera) {
            mCamera = camera;
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout();
            }
        }

        public void switchCamera(Camera camera) {
            setCamera(camera);
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            camera.setParameters(parameters);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
                } else {
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) w / h;
            if (sizes == null)
                return null;

            Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            // Try to find an size match aspect ratio and size
            for (Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.startPreview();

            getHandler().post(new Runnable() {

                @Override
                public void run() {
                    if (!RecordVideoFragment.this.isRecording) {
                        startRecording();
                    }
                }
            });

        }

    }
}
