package com.moziy.hollerback.fragment;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.bitmap.ImageCache;
import com.moziy.hollerback.bitmap.ImageFetcher;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.ProgressHelper;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.util.QU;
import com.moziy.hollerback.video.S3UploadParams;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnProgressListener;

public class VideoPlayFragment extends DialogFragment {

	/**
	 * This piece of shit takes up 100% height unless you restrict it
	 */
	private ViewGroup mRootView;
	
	// Image Loading
	private ImageFetcher mImageFetcher;
	private int mImageThumbSize;
	private int mImageThumbSpacing;

	private static final String IMAGE_CACHE_DIR = "thumbs";

	// Image Loading

	// Video Playback Stuff
	private VideoView mVideoView;

	private S3RequestHelper mS3RequestHelper;

	// Reply stuff
	private Button mReplyBtn;

	public int TAKE_VIDEO = 0x683;

	private String mConversationId;

	private ImageButton mPlayBtn;

	// state
	boolean urlLoaded = false;

	S3RequestHelper helper = new S3RequestHelper();

	private ConversationModel conversation;

	private ArrayList<VideoModel> mVideos;

	boolean playStartInitialized;

	ProgressHelper mProgressHelper;
	

	/**
	 * Create a new instance of CountingFragment, providing "num" as an
	 * argument.
	 */
	public static VideoPlayFragment newInstance(Serializable data) {

		VideoPlayFragment f = new VideoPlayFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putSerializable("data", data);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = (ViewGroup)inflater.inflate(R.layout.video_fragment,
				null);
		
	//	mVideoWrapper = (RelativeLayout)mRootView.findViewById(R.id.videoWrapper);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				getActivity(), IMAGE_CACHE_DIR);

		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.placeholder_sq);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);

		mS3RequestHelper = new S3RequestHelper();

		mS3RequestHelper.registerOnProgressListener(mOnProgressListener);

		initializeView(mRootView);

		
		VideoModel model = (VideoModel)this.getArguments().getSerializable("data");// mVideoGalleryAdapter.getItem(position);
		mConversationId = model.getConversationId();
		
		initializeArgs();
		
		mS3RequestHelper.downloadS3(
				AppEnvironment.getInstance().PICTURE_BUCKET,
				model.getFileName());
		mProgressHelper.startIndeterminateSpinner();
		if (!model.isRead()) {
			model.setRead(true);
		}
		
		HBRequestManager
				.postVideoRead(Integer.toString(model.getVideoId()));
		
		return mRootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mImageFetcher != null) {
			mImageFetcher.closeCache();
		}
		IABroadcastManager.unregisterLocalReceiver(receiver);
		if(mS3RequestHelper != null)
		{
			mS3RequestHelper.clearOnProgressListener();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// TODO: Do this less often
		QU.getDM().getVideos(false, mConversationId);

		mImageFetcher.setExitTasksEarly(false);
		
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_REQUEST_VIDEO);
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_GET_URLS);
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_GET_CONVERSATION_VIDEOS);
	
	}

	// TODO: Move out of here
	private ArrayList<S3UploadParams> generateUploadParams(String hash,
			String conversationId) {

		ArrayList<S3UploadParams> mGetUrls = new ArrayList<S3UploadParams>();

		ArrayList<VideoModel> videos = ((ArrayList<VideoModel>) QU.getDM()
				.getObjectForToken(hash));

		if (videos != null && videos.size() > 0) {
			for (VideoModel video : videos) {
				S3UploadParams param = new S3UploadParams();
				param.setFileName(video.getFileName());
				param.setOnS3UploadListener(null);
				param.mVideo = video;
				mGetUrls.add(param);
			}
		}

		return mGetUrls;
	}

	public void initializeArgs() {
		conversation = QU.getConv(mConversationId);
/*
		HollerbackBaseActivity.getCustomActionBar().setHeaderText(
				conversation.getConversationName());*/
		LogUtil.i("Conversation Fragment: ID: " + mConversationId);
		// mVideoGalleryAdapter.setVideos(TempMemoryStore.conversations.get(index)
		// .getVideos());
		// helper.getS3URLParams(generateUploadParams(index));

	}

	protected void initializeView(View view) {

		mPlayBtn = (ImageButton) view.findViewById(R.id.ib_play_btn);

		mProgressHelper = new ProgressHelper(
				view.findViewById(R.id.rl_progress));
		
		mVideoView = (VideoView) view.findViewById(R.id.videoView);

		/*
		mReplyBtn = (Button) view.findViewById(R.id.btn_video_reply);

		mReplyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecordVideoFragment fragment = RecordVideoFragment.newInstance(mConversationId);
				VideoPlayFragment.this.getFragmentManager()
				.beginTransaction().replace(R.id.fragment_holder, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		        .addToBackStack(null)
		        .commitAllowingStateLoss();

			}
		});*/
/*
		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mPlayBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						playVideo(currentVideo);
					}
				});
			}
		});
		*/
	}

	String currentVideo;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (IABIntent.isIntent(intent, IABIntent.INTENT_REQUEST_VIDEO)) {
				LogUtil.e("Received ID: "
						+ intent.getStringExtra(IABIntent.PARAM_ID));
				playVideo(intent.getStringExtra(IABIntent.PARAM_ID));

			}
		}
	};
	
	private void playVideo(String fileKey) {
		String path = FileUtil.getLocalFile(fileKey);
		
		LogUtil.i("Play video: " + path);
		mVideoView.setVisibility(View.VISIBLE);
		mPlayBtn.setVisibility(View.GONE);

		currentVideo = fileKey;
		mVideoView.setVideoPath(path);
		mVideoView.requestFocus();
		mVideoView.start();
	}
	

	private OnProgressListener mOnProgressListener = new OnProgressListener() {

		@Override
		public void onProgress(long amount, long total) {
			final String percent = Long.toString((amount * 100 / total));
			VideoPlayFragment.this.getActivity().runOnUiThread(
					new Runnable() {
						public void run() {
							mProgressHelper.startUpdateProgress(percent);
						}
					});
		}

		@Override
		public void onComplete() {
			VideoPlayFragment.this.getActivity().runOnUiThread(
					new Runnable() {
						public void run() {
							mProgressHelper.hideLoader();
						}
					});

		}
	};
}