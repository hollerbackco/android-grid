package com.moziy.hollerback.fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.service.VideoUploadService;
import com.moziy.hollerback.util.CameraUtil;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.util.GPUImageFilterTools;
import com.moziy.hollerback.util.GPUImageFilterTools.FilterAdjuster;
import com.moziy.hollerback.util.GPUImageFilterTools.FilterList;
import com.moziy.hollerback.util.ImageUtil;
import com.moziy.hollerback.util.UploadCacheUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder.OutputFormat;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class RecordVideoFragment extends BaseFragment{
	protected ViewGroup mRootView;
	protected static SherlockFragmentActivity mActivity;

    private static GPUImage mGPUImage;
    private static GPUImageFilter mFilter;
    private static FilterList mFilterList;
    
	private GLSurfaceView preview = null;
	private static SurfaceHolder previewHolder = null;
	private static Camera camera = null;
	private boolean inPreview = false;
	private boolean mToConversation = false;
	TextView mTimer;
	Handler handler;

	int VIDEO_SENT = 4;

	int secondsPassed;
	private int currentCameraId = CameraInfo.CAMERA_FACING_FRONT;

	View mTopView, mBottomView;

	private String mFileDataPath;
	private String mFileDataName;
	
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
	private String mConversationId;
	private TextView mTxtPlaying;

	ViewPager mFilterPagers;
	
	int mBestCameraWidth, mBestCameraHeight;
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.record, menu);
    }
    
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		this.getFragmentManager().popBackStack();
	    		break;	
	    	case R.id.action_cancel:
	    		this.getFragmentManager().popBackStack();
	    		break;
    	
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
	
	public static RecordVideoFragment newInstance(String conversationId){
		RecordVideoFragment fragment = new RecordVideoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(IABIntent.PARAM_ID, conversationId);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	public static RecordVideoFragment newInstance(String conversationId, boolean toConversation){
		RecordVideoFragment fragment = new RecordVideoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(IABIntent.PARAM_ID, conversationId);
		bundle.putBoolean("mToConversation", toConversation);
		fragment.setArguments(bundle);
		return fragment;
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		this.getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRootView = (ViewGroup) inflater.inflate(R.layout.custom_camera, null);
        mActivity = (SherlockFragmentActivity)this.getActivity();
        
        mActivity.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        mToConversation = this.getArguments().getBoolean("mToConversation", false);
        
		handler = new Handler();

		mTopView = mRootView.findViewById(R.id.top_bar);
		mBottomView = mRootView.findViewById(R.id.bottom_bar);
		mSendButton = (Button)mRootView.findViewById(R.id.send_button);

		mPreviewParentView = mRootView.findViewById(R.id.rl_video_preview);
		mPreviewVideoView = (VideoView) mRootView.findViewById(R.id.vv_video_preview);
		mPreviewPlayBtn = (ImageButton) mRootView.findViewById(R.id.ib_play_btn);
		mImagePreview = (ImageView) mRootView.findViewById(R.id.iv_video_preview);
		mFilterButton = (ImageButton)mRootView.findViewById(R.id.ib_filter_btn);
		
		mTxtPlaying = (TextView)mRootView.findViewById(R.id.txtPlaying);
		
		preview = (GLSurfaceView) mRootView.findViewById(R.id.surface);
		preview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isRecording) {
					stopRecording();
				} else {
					startRecording();
				}
			}
		});
		
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		targetPreviewWidth = 480;
		targetPreviewHeight = 320;
		
		// this 1.5 i guess assumes 640 x 480
		previewHolder
				.setFixedSize(
						mActivity.getWindow().getWindowManager().getDefaultDisplay()
								.getWidth(),
						(int) (mActivity.getWindow().getWindowManager()
								.getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight)));

		RelativeLayout.LayoutParams mImagePreviewParams = (RelativeLayout.LayoutParams) mImagePreview
				.getLayoutParams();
		mImagePreviewParams.height = (int) (mActivity.getWindow().getWindowManager()
				.getDefaultDisplay().getWidth() * (targetPreviewWidth / targetPreviewHeight));

		mImagePreview.setLayoutParams(mImagePreviewParams);

		if(this.getArguments().containsKey(IABIntent.PARAM_ID))
		{
			mConversationId = this.getArguments().getString(IABIntent.PARAM_ID);
			LogUtil.i("HollerbackCamera CONVO: " + mConversationId);
		}

        mGPUImage = new GPUImage(mActivity);
		mGPUImage.setGLSurfaceView(preview);
		mFilterList = GPUImageFilterTools.getFilters();
		mFilterPagers = (ViewPager)mRootView.findViewById(R.id.filters);
		mFilterPagers.setAdapter(new FilterAdapter(mActivity.getSupportFragmentManager()));
		
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
				if (mConversationId == null) {
					HBRequestManager
							.postConversations(TempMemoryStore.invitedUsers);
					LogUtil.e("Conversation ID NULL");
				} else {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm", Locale.US);
					
					JSONObject cacheData = new JSONObject();
					try {
						File tmp = new File(FileUtil.getLocalFile(FileUtil.getImageUploadName(mFileDataName)));
						String fileurl = Uri.fromFile(tmp).toString();
						
						cacheData.put("filename", mFileDataName);
						cacheData.put("id", 0);
						cacheData.put("conversation_id", mConversationId);
						cacheData.put("isRead", true);
						cacheData.put("url", fileurl);
						cacheData.put("thumb_url", fileurl);
						cacheData.put("created_at", df.format(new Date()));
						cacheData.put("username", "me");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Intent serviceIntent = new Intent(mActivity, VideoUploadService.class);
					serviceIntent.putExtra("ConversationId", mConversationId);
					serviceIntent.putExtra("FileDataName", mFileDataName);
					serviceIntent.putExtra("ImageUploadName", FileUtil.getImageUploadName(mFileDataName));
					
					if(cacheData != new JSONObject())
					{
						serviceIntent.putExtra("JSONCache", cacheData.toString());
						UploadCacheUtil.setUploadCacheFlag(mActivity, mConversationId, cacheData);
					}
					
					
					
					mActivity.startService(serviceIntent);
					mActivity.getSupportFragmentManager().popBackStack();
					
					if(mToConversation)
					{

						ConversationFragment fragment = ConversationFragment.newInstance(mConversationId);
						mActivity.getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_holder, fragment)
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				        .addToBackStack(null)
				        .remove(RecordVideoFragment.this)
				        .commitAllowingStateLoss();
					}
				}
			}
		});
        
		mSwitchButton = (ImageButton)mRootView.findViewById(R.id.btnSwitch);
		mSwitchButton.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				switchCamera();
			}
		});
		
        return mRootView;
    }
	
	@Override
	protected void initializeView(View view) {
		
	}

	Runnable timeTask = new Runnable() {

		@Override
		public void run() {
			secondsPassed += 1;
			String seconds = String.valueOf(timer - secondsPassed) + "s";
			mTimer.setText(seconds);
			if(secondsPassed >= timer)
			{
				stopRecording();
			}
			
			handler.postDelayed(timeTask, 1000);
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		try {
			camera = Camera.open(currentCameraId);

			Log.e("Hollerback", "Camera successfully opened");
		} catch (RuntimeException e) {
			Log.e("Hollerback",
					"Camera failed to open: " + e.getLocalizedMessage());
		}

		if (camera == null) {
			camera = Camera.open();
		}
		previewHolder.addCallback(surfaceCallback);
	}

	@Override
	public void onPause() {
		 if (inPreview) {
		 camera.stopPreview();
		 }
		
		 camera.release();
		 camera = null;
		 inPreview = false;
		super.onPause();
		//IABroadcastManager.unregisterLocalReceiver(receiver);
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
		
		if (prepareVideoRecorder()) {
			mTimer.setText("20s");
			// Camera is available and unlocked, MediaRecorder is
			// prepared,
			// now you can start recording

			recorder.start();

			// inform the user that recording has started
			//mRecordButton.setImageResource(R.drawable.stop_button);
			isRecording = true;
			handler.postDelayed(timeTask, 1000);
		} else {
			// prepare didn't work, release the camera
			releaseMediaRecorder();
			// inform user
		}
	}

	protected void stopRecording() {
		// stop recording and release camera
		try
		{
			recorder.stop(); // stop the recording
			releaseMediaRecorder(); // release the MediaRecorder object
			camera.lock(); // take camera access back from MediaRecorder			
		}
		catch(java.lang.RuntimeException e)
		{
			this.getFragmentManager().popBackStack();
		}
		
		mRecordButton.clearAnimation();
		mRecordButton.setBackgroundResource(R.drawable.recording_spinner);
		mTimer.setText(String.valueOf(secondsPassed) + "s");
		mTimer.setTextColor(mActivity.getResources().getColor(R.color.timer_default));
		mTxtPlaying.setVisibility(View.GONE);

		// inform the user that recording has stopped
		//mRecordButton.setImageResource(R.drawable.record_button);
		isRecording = false;

		if (mFileDataPath != null) {
			//mRecordButton.setVisibility(View.GONE);
			mRecordButton.setEnabled(false);
			mRecordButton.setClickable(false);
			mSendButton.setVisibility(View.VISIBLE);
			mSwitchButton.setVisibility(View.GONE);
		}
		handler.removeCallbacks(timeTask);
		secondsPassed = 0;

		displayPreview();
		ImageUtil.generateThumbnail(mFileDataName);
	}

	public void displayPreview() {

		if (inPreview) {
			camera.stopPreview();
		}

		mImagePreview.setVisibility(View.VISIBLE);

		preview.setVisibility(View.GONE);

		inPreview = false;

		mPreviewParentView.setVisibility(View.VISIBLE);
		mPreviewVideoView.setVisibility(View.VISIBLE);
		mPreviewPlayBtn.setVisibility(View.VISIBLE);
		
		mFilterButton.setVisibility(View.GONE);

		Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(
				FileUtil.getLocalFile(mFileDataName),
				Thumbnails.FULL_SCREEN_KIND);
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
				playVideo(mFileDataName);
				mPreviewPlayBtn.setEnabled(false);
			}
		});

	}

	private String getNewFileName() {

		mFileDataName = FileUtil.generateRandomFileName() + "."
				+ targetExtension;
		mFileDataPath = FileUtil.getOutputVideoFile(mFileDataName).toString();

		return mFileDataPath;
	}

	private void playVideo(String fileKey) {

		mImagePreview.setVisibility(View.GONE);

		mPreviewVideoView.setVideoPath(FileUtil.getLocalFile(fileKey));

		mPreviewVideoView.requestFocus();
		mPreviewVideoView.start();
		LogUtil.i("vid size: " + mPreviewVideoView.getHeight()
				+ mPreviewVideoView.getWidth());
	}

	private boolean prepareVideoRecorder() {

		recorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		camera.unlock();
		recorder.setCamera(camera);

		// Step 2: Set sources
		recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		// recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)

		CameraUtil.setFrontFacingParams(recorder, mBestCameraWidth,
				mBestCameraHeight);

		targetExtension = FileUtil.getFileFormat(OutputFormat.MPEG_4);

		CamcorderProfile prof = CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW);

		LogUtil.i("Record size: " + prof.videoFrameWidth + " "
				+ prof.videoFrameHeight);

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
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
    private static void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
        }
    }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (inPreview) {
			camera.stopPreview();
		}
		if(camera != null)
		{
			camera.release();
			camera = null;
		}
		inPreview = false;
		super.onDestroy();
	}

	private void releaseMediaRecorder() {

		if (recorder != null) {
			// recorder.reset(); // clear configuration (optional here)
			recorder.release();
			// recorder = null;
		}
	}
	
	private void switchCamera()
	{
		if (inPreview) {
		    camera.stopPreview();
		}
		//NB: if you don't release the current camera before switching, you app will crash
		camera.release();

		//swap the id of the camera to be used
		if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
		    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		}
		else {
		    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		camera = Camera.open(currentCameraId);

		setCameraDisplayOrientation(mActivity, currentCameraId, camera);
		try {

		    camera.setPreviewDisplay(previewHolder);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		
		camera.startPreview();
	}
	
	private void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(previewHolder);
				camera.setDisplayOrientation(90);
				MediaRecorder m = new MediaRecorder();
				m.setCamera(camera);
				m.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
				m.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			} catch (Throwable t) {
				Log.e("SurfaceCallback", "Exception in setPreviewDisplay()", t);
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = CameraUtil.getBestPreviewSize(
					(int) targetPreviewWidth, (int) targetPreviewHeight,
					parameters);
			LogUtil.i("Best size: " + size.width + " " + size.height);

			mBestCameraWidth = size.width;
			mBestCameraHeight = size.height;

			if (size != null) {
				parameters.setPreviewSize(size.width, size.height);
				camera.setParameters(parameters);
				camera.setDisplayOrientation(90);
				camera.startPreview();
				inPreview = true;
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {

		}
	};

    public class FilterAdapter extends FragmentPagerAdapter {
        public FilterAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFilterList.names.size();
        }

        @Override
        public Fragment getItem(int position) {
            return FilterFragment.newInstance(position);
        }
    }
    

    public static class FilterFragment extends Fragment {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static FilterFragment newInstance(int num) {
        	FilterFragment f = new FilterFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.text_filter, container, false);
            View tv = v.findViewById(R.id.txtFilter);
            ((TextView)tv).setText(mFilterList.names.get(mNum));
            switchFilterTo(GPUImageFilterTools.createFilterForType(mActivity, mFilterList.filters.get(mNum)));
            return v;
        }
    }
}
