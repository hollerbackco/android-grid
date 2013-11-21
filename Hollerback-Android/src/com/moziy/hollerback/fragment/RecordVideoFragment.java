package com.moziy.hollerback.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.TimeUtil;
import com.moziy.hollerback.util.camera.CameraUtil;
import com.moziy.hollerback.view.camera.PreviewSurfaceView;
import com.moziy.hollerback.view.camera.PreviewTextureView;

public class RecordVideoFragment extends BaseFragment implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback2 {

    public static final String FRAGMENT_ARG_WATCHED_IDS = "watched_ids";
    public static final String FRAGMENT_ARG_PHONES = "phones";
    public static final String FRAGMENT_ARG_TITLE = "title";
    public static final String FRAGMENT_ARG_GOTO_CONVO = "to_conversation";

    public static interface RecordingInfo {
        public static final String RECORDED_PARTS = "recorded_parts";
        public static final String RESOURCE_ROW_ID = "resource_row_id";

        public void onRecordingFinished(Bundle info);
    }

    private static final int PREFERRED_VIDEO_WIDTH = 320;
    private static final int PREFERRED_VIDEO_HEIGHT = 240;

    private Camera mCamera = null;
    private PreviewSurfaceView mCameraSurfaceView;
    private boolean inPreview = false;
    private boolean mToConversation = false;
    TextView mTimer;
    private final Handler mHandler = new Handler();

    int VIDEO_SENT = 4;

    int secondsPassed;
    private int mCurrentCameraId = CameraInfo.CAMERA_FACING_FRONT;

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
    protected Button mSendButton;
    private long mConversationId = -1;

    private String[] mPhones = null;
    ViewPager mFilterPagers;

    int mBestCameraWidth, mBestCameraHeight;

    private TextureView mTexturePreview;

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
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        View v = inflater.inflate(R.layout.recording_layout, container, false);
        mCameraSurfaceView = (PreviewSurfaceView) v.findViewById(R.id.preview);
        mCameraSurfaceView.getHolder().addCallback(this);
        mCameraSurfaceView.setOnClickListener(new View.OnClickListener() {

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

        return v;
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

    // this can be used later if we go to a complete texture view solution
    public TextureView getTextureBasedView() {
        mTexturePreview = new PreviewTextureView(getActivity());
        mTexturePreview.setSurfaceTextureListener(this);

        mTexturePreview.setOnClickListener(new View.OnClickListener() {

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

        return mTexturePreview;
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
        // try {
        // mCamera = Camera.open(currentCameraId);
        // mRootView.setCamera(mCamera);
        //
        // // test
        // // List<Size> sizes = mCamera.getParameters().getSupportedVideoSizes();
        // // if (sizes == null) {
        // // Log.d(TAG, "null size");
        // // }
        // // for (Size s : sizes) {
        // // Log.d(TAG, "supported size: " + s.width + " x " + s.height);
        // // }
        //
        // Log.e("Hollerback", "Camera successfully opened");
        // } catch (RuntimeException e) {
        // Log.e("Hollerback", "Camera failed to open: " + e.getLocalizedMessage());
        // Toast.makeText(mActivity, R.string.record_error, Toast.LENGTH_LONG).show();
        //
        // onRecordingFailed();
        // }

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

        mActivity.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        inPreview = false;
        super.onPause();
    }

    protected void startRecording() {

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

        isRecording = false;

        mHandler.removeCallbacks(timeTask);
        secondsPassed = 0;

        // // displayPreview();
        // ImageUtil.generateThumbnail(mFileDataName + "." + mFileExt);

        // Precondition: User decides to send video (no ttyl without video, etc)
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

    private boolean prepareVideoRecorder() {

        recorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        recorder.setCamera(mCamera);

        // Step 2: Set sources
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // CameraUtil.printAllCamcorderProfiles(CameraInfo.CAMERA_FACING_FRONT);

        // Step 3: Configure Camera
        CameraUtil.setFrontFacingParams(recorder, 320, 240);

        // recorder.setvideoextension
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

        inPreview = false;

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        mActivity.getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!RecordVideoFragment.this.isRecording) {
                        startRecording();
                    }
                }
            });
        } catch (IOException e) {

        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mCamera = Camera.open(mCurrentCameraId);

        // query the best size / aspect ratio
        Camera.Size actualSize = CameraUtil.getPreferredVideoSize(PREFERRED_VIDEO_WIDTH, PREFERRED_VIDEO_HEIGHT, mCamera.getParameters());

        mCameraSurfaceView.setAspectRatio((double) actualSize.width / (double) actualSize.height); // adjust the surfaceview

    }

    Thread mOpenCameraThread = new Thread() {

        public void run() {
            mCamera = Camera.open(mCurrentCameraId);
        };
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try {

            // mOpenCameraThread.run();
            // mOpenCameraThread.join();

            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            inPreview = true;

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!RecordVideoFragment.this.isRecording) {
                        startRecording();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        inPreview = false;
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
}
