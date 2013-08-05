package com.moziy.hollerback.fragment;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.actionbarsherlock.view.MenuItem;
import com.krish.horizontalscrollview.CustomListBaseAdapter;
import com.moziy.hollerback.HollerbackInterfaces.OnCustomItemClickListener;
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
import com.moziy.hollerback.view.CustomVideoView;
import com.moziy.hollerback.view.ScrollingSyncListView;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnProgressListener;
import com.origamilabs.library.views.StaggeredGridView;

public class CopyOfConversationFragment2 extends BaseFragment {

	/**
	 * This piece of shit takes up 100% height unless you restrict it
	 */
	private ScrollingSyncListView mVideoGalleryLeft;
	private ScrollingSyncListView mVideoGalleryRight;

	private CustomListBaseAdapter mVideoGalleryAdapterLeft;
	private CustomListBaseAdapter mVideoGalleryAdapterRight;

	private ViewGroup mRootView;
	
	// Image Loading
	private ImageFetcher mImageFetcher;
	private int mImageThumbSize;
	private int mImageThumbSpacing;

	private static final String IMAGE_CACHE_DIR = "thumbs";

	// Image Loading

	// Video Playback Stuff
	private VideoView mVideoViewLeft;
	private VideoView mVideoViewRight;

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
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		this.getFragmentManager().popBackStack();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		this.getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mRootView = (ViewGroup)inflater.inflate(R.layout.conversation_fragment2,
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

		initializeArgs();

		/*
		mScrollWrapper = (ScrollView)mRootView.findViewById(R.id.scrollWrapper);
		mScrollWrapper.requestDisallowInterceptTouchEvent(true);
		
		mScrollWrapper.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
		        switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                // if we can scroll pass the event to the superclass
	                // only continue to handle the touch event if scrolling enabled
	                return true; // mScrollable is always false at this point
	            default:
	                return super(event);
	        }
			}
		});	
		*/
		return mRootView;
	}

	public void playNewestVideo() {
		if (playStartInitialized) {
			if (mVideoGalleryAdapterLeft.getCount() > 0) {

				mVideoGalleryAdapterLeft.notifyDataSetChanged();

			}
			if (mVideoGalleryAdapterRight.getCount() > 0) {

				mVideoGalleryAdapterRight.notifyDataSetChanged();

			}
			//setGalleryToEnd();
			return;
		}

		if (mVideoGalleryAdapterLeft.getCount() < 1 && mVideoGalleryAdapterRight.getCount() < 1) {
			return;
		}

		playStartInitialized = true;
		boolean set = false;

		for (int i = mVideoGalleryAdapterLeft.getCount() - 1; i >= 0; i--) {
			if (mVideoGalleryAdapterLeft.getItem(i).isRead() == false) {

				// model.setRead(true);
				set = true;
				mVideoGalleryAdapterLeft.notifyDataSetChanged();
				// }
			}
		}

		
		for (int i = mVideoGalleryAdapterRight.getCount() - 1; i >= 0; i--) {
			if (mVideoGalleryAdapterRight.getItem(i).isRead() == false) {

				// model.setRead(true);
				set = true;
				mVideoGalleryAdapterRight.notifyDataSetChanged();
				// }
			}
		}

		if (!set) {
			if (mVideoGalleryAdapterLeft.getCount() > 0) {

				mVideoGalleryAdapterLeft.notifyDataSetChanged();

			}
			if (mVideoGalleryAdapterRight.getCount() > 0) {

				mVideoGalleryAdapterRight.notifyDataSetChanged();

			}
			//setGalleryToEnd();
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		//mImageFetcher.setPauseWork(false);
		//mImageFetcher.setExitTasksEarly(true);
		//mImageFetcher.flushCache();

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
		Bundle bundle = getArguments();
		mConversationId = bundle.getString("conv_id");

		conversation = QU.getConv(mConversationId);
/*
		HollerbackBaseActivity.getCustomActionBar().setHeaderText(
				conversation.getConversationName());*/
		LogUtil.i("Conversation Fragment: ID: " + mConversationId);
		// mVideoGalleryAdapter.setVideos(TempMemoryStore.conversations.get(index)
		// .getVideos());
		// helper.getS3URLParams(generateUploadParams(index));

	}

	@Override
	protected void initializeView(View view) {

		mPlayBtn = (ImageButton) view.findViewById(R.id.ib_play_btn);

		mProgressHelper = new ProgressHelper(
				view.findViewById(R.id.rl_progress));

		mVideoGalleryLeft = (ScrollingSyncListView) view
				.findViewById(R.id.hlz_video_gallery_left);

		mVideoGalleryRight = (ScrollingSyncListView) view
				.findViewById(R.id.hlz_video_gallery_right);
		
		
		
		mVideoViewLeft = (VideoView) view
				.findViewById(R.id.videoLeft);
		mVideoViewRight = (VideoView) view
				.findViewById(R.id.videoRight);
		
		
		mVideoGalleryAdapterLeft = new CustomListBaseAdapter(getActivity(),
				mImageFetcher, mS3RequestHelper);
		
		mVideoGalleryAdapterRight = new CustomListBaseAdapter(getActivity(),
				mImageFetcher, mS3RequestHelper);
		
		mVideoGalleryLeft.setAdapter(mVideoGalleryAdapterLeft);
		mVideoGalleryRight.setAdapter(mVideoGalleryAdapterRight);
		
		mVideoGalleryLeft.setOnTouchListener(touchListener);
		mVideoGalleryRight.setOnTouchListener(touchListener);		
		mVideoGalleryLeft.setOnScrollListener(scrollListener);
		mVideoGalleryRight.setOnScrollListener(scrollListener);
		
		mVideoGalleryAdapterLeft.setOnCustomItemClickListener(mListenerLeft);
		mVideoGalleryAdapterRight.setOnCustomItemClickListener(mListenerRight);

		//mVideoGallery.setOnItemClickListener(mListener);
		// mVideoGallery.setOnScrollListener(mOnScrollListener);

		mReplyBtn = (Button) view.findViewById(R.id.btn_video_reply);

		mReplyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				Intent intent = new Intent(getActivity(),
						HollerbackCameraActivity.class);
				LogUtil.i("Putting Extra ID: " + mConversationId == null ? "null"
						: mConversationId);

				Bundle mBundle = new Bundle();
				mBundle.putString(IABIntent.PARAM_ID, mConversationId);
				intent.putExtras(mBundle);

				// intent.putExtra(IABIntent.PARAM_ID, mConversationId);
				startActivity(intent);
				*/
				
				RecordVideoFragment fragment = RecordVideoFragment.newInstance(mConversationId);
				CopyOfConversationFragment2.this.getFragmentManager()
				.beginTransaction().replace(R.id.fragment_holder, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		        .addToBackStack(null)
		        .commitAllowingStateLoss();

			}
		});
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

	int currentPlayingPosition;
	OnCustomItemClickListener mListenerLeft = new OnCustomItemClickListener() {

		@Override
		public void onItemClicked(int position, View view) {
			LogUtil.i("Clicked ON: " + position);
			currentPlayingPosition = position;
			VideoModel model = mVideoGalleryAdapterLeft.getItem(position);
			mS3RequestHelper.downloadS3(
					AppEnvironment.getInstance().PICTURE_BUCKET,
					model.getFileName());
			mProgressHelper.startIndeterminateSpinner();
			if (!model.isRead()) {
				model.setRead(true);
			}
			mVideoGalleryAdapterLeft.notifyDataSetChanged();			
			HBRequestManager
					.postVideoRead(Integer.toString(model.getVideoId()));
			mVideoGalleryLeft.clearScrollingSiblings();
			mVideoGalleryRight.clearScrollingSiblings();

			mVideoGalleryLeft.addScrollingSibling(position, R.id.videoLeft);

			mVideoViewLeft.setVisibility(View.VISIBLE);
			mVideoViewLeft.pause();
			mVideoViewRight.setVisibility(View.INVISIBLE);
			mVideoViewRight.pause();
		}
	};
	
	OnCustomItemClickListener mListenerRight = new OnCustomItemClickListener() {

		@Override
		public void onItemClicked(int position, View view) {
			LogUtil.i("Clicked ON: " + position);
			currentPlayingPosition = position;
			VideoModel model = mVideoGalleryAdapterLeft.getItem(position);
			mS3RequestHelper.downloadS3(
					AppEnvironment.getInstance().PICTURE_BUCKET,
					model.getFileName());
			mProgressHelper.startIndeterminateSpinner();
			if (!model.isRead()) {
				model.setRead(true);
			}
			mVideoGalleryAdapterLeft.notifyDataSetChanged();			
			HBRequestManager
					.postVideoRead(Integer.toString(model.getVideoId()));
			
			mVideoGalleryLeft.clearScrollingSiblings();
			mVideoGalleryRight.clearScrollingSiblings();

			mVideoGalleryRight.addScrollingSibling(position, R.id.videoRight);
			
			mVideoViewLeft.setVisibility(View.INVISIBLE);
			mVideoViewLeft.pause();
			mVideoViewRight.setVisibility(View.VISIBLE);
			mVideoViewRight.pause();
		}
	};

	/**
	 * Create a new instance of CountingFragment, providing "num" as an
	 * argument.
	 */
	public static CopyOfConversationFragment2 newInstance(String conversation_id) {

		CopyOfConversationFragment2 f = new CopyOfConversationFragment2();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("conv_id", conversation_id);
		f.setArguments(args);
		return f;
	}

	String currentVideo;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (IABIntent.isIntent(intent, IABIntent.INTENT_REQUEST_VIDEO)) {
				LogUtil.e("Received ID: "
						+ intent.getStringExtra(IABIntent.PARAM_ID));
				if(mVideoViewLeft.getVisibility() == View.VISIBLE)
				{
					playVideoLeft(intent.getStringExtra(IABIntent.PARAM_ID), currentPlayingPosition);
				}
				else
				{
					playVideoRight(intent.getStringExtra(IABIntent.PARAM_ID), currentPlayingPosition);	
				}
				

			} else if (IABIntent.isIntent(intent,
					IABIntent.INTENT_GET_CONVERSATION_VIDEOS)) {

				String hash = intent
						.getStringExtra(IABIntent.PARAM_INTENT_DATA);

				ArrayList<VideoModel> tempVideos = (ArrayList<VideoModel>) QU
						.getDM()
						.getObjectForToken(
								intent.getStringExtra(IABIntent.PARAM_INTENT_DATA));

				// helper.getS3URLParams(generateUploadParams(hash,
				// intent.getStringExtra(IABIntent.PARAM_ID)));

				if (mVideos != null && mVideos.equals(tempVideos)) {
					LogUtil.i("Setting: same data set");
					return;
				}

				mVideos = (ArrayList<VideoModel>) tempVideos.clone();

				if (mVideos != null) {

					LogUtil.i("Setting Received videos: " + mVideos.size());

					mVideoGalleryAdapterLeft.setListItems(mVideos);
					mVideoGalleryAdapterLeft.notifyDataSetChanged();
					
					mVideoGalleryAdapterRight.setListItems(mVideos);
					mVideoGalleryAdapterRight.notifyDataSetChanged();

					leftViewsHeights = new int[mVideos.size()];
					rightViewsHeights = new int[mVideos.size()];	
					//mVideoGallery.setAdapter(getActivity(), mVideoGalleryAdapter);

					LogUtil.d("Setting new Videos size: " + mVideos.size());

					// TODO: Fix issues here
					// playNewestVideo();
					LogUtil.i("Setting center index: " + mVideos.size());
					LogUtil.i("Setting count index: "
							+ mVideoGalleryAdapterLeft.getCount());
					if (mVideoGalleryAdapterLeft.getCount() > 0) {
						// mVideoGalleryAdapter.selectedIndex =
						// mVideoGalleryAdapter
						// .getCount() - 1;
						// mVideoGalleryAdapter.notifyDataSetChanged();
						//setGalleryToEnd();
					}
					// setGalleryToEnd();

				}

			}
		}
	};
	
/*
	private void setGalleryToEnd() {
		mVideoGallery.post(new Runnable() {
			@Override
			public void run() {

				//mVideoGallery.snapCenter(mVideos.size() - 1);

			}
		});
	}
*/
	
	
	private void playVideoLeft(String fileKey, int position) {
		String path = FileUtil.getLocalFile(fileKey);
		
		LogUtil.i("Play video: " + path);
		mPlayBtn.setVisibility(View.GONE);

		currentVideo = fileKey;
	
//		mVideoWrapper.invalidate();
		//mVideoWrapper.getLayoutParams().height = mVideoGallery.getHeight();
		
		//mVideoView.getLayoutParams().width = 200;
		//mVideoView.getLayoutParams().height = 200;
		
		/*
		FrameLayout.LayoutParams params = 
				new FrameLayout.LayoutParams(
						mVideoGallery.getChildAt(position).getWidth(),
						mVideoGallery.getChildAt(position).getHeight())
						;
		
		mVideoView.setLayoutParams(params);
		mVideoView.invalidate();
		
		mVideoView.changeVideoSize(mVideoGallery.getChildAt(position).getWidth(), mVideoGallery.getChildAt(position).getHeight());
		*/
		mVideoViewLeft.setVideoPath(path);
		mVideoViewLeft.requestFocus();
		mVideoViewLeft.start();
	}
	
	private void playVideoRight(String fileKey, int position) {
		
		String path = FileUtil.getLocalFile(fileKey);
		currentVideo = fileKey;

		
		mVideoViewRight.setVideoPath(path);
		mVideoViewRight.requestFocus();
		mVideoViewRight.start();
	}

	private OnProgressListener mOnProgressListener = new OnProgressListener() {

		@Override
		public void onProgress(long amount, long total) {
			final String percent = Long.toString((amount * 100 / total));
			CopyOfConversationFragment2.this.getActivity().runOnUiThread(
					new Runnable() {
						public void run() {
							mProgressHelper.startUpdateProgress(percent);
						}
					});
		}

		@Override
		public void onComplete() {
			CopyOfConversationFragment2.this.getActivity().runOnUiThread(
					new Runnable() {
						public void run() {
							mProgressHelper.hideLoader();
						}
					});

		}
	};
	

	int[] leftViewsHeights;
	int[] rightViewsHeights;

	// Passing the touch event to the opposite list
	OnTouchListener touchListener = new OnTouchListener() {					
		boolean dispatched = false;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v.equals(mVideoGalleryLeft) && !dispatched) {
				dispatched = true;
				mVideoGalleryRight.dispatchTouchEvent(event);
			} else if (v.equals(mVideoGalleryRight) && !dispatched) {
				dispatched = true;
				mVideoGalleryLeft.dispatchTouchEvent(event);
			}

			dispatched = false;
			return false;
		}
	};

	/**
	 * Synchronizing scrolling 
	 * Distance from the top of the first visible element opposite list:
	 * sum_heights(opposite invisible screens) - sum_heights(invisible screens) + distance from top of the first visible child
	 */
	OnScrollListener scrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView v, int scrollState) {	
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			if (view.getChildAt(0) != null) {
				if (view.equals(mVideoGalleryLeft) ){
					leftViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

					int h = 0;
					for (int i = 0; i < mVideoGalleryRight.getFirstVisiblePosition(); i++) {
						h += rightViewsHeights[i];
					}

					int hi = 0;
					for (int i = 0; i < mVideoGalleryLeft.getFirstVisiblePosition(); i++) {
						hi += leftViewsHeights[i];
					}

					int top = h - hi + view.getChildAt(0).getTop();
					mVideoGalleryRight.setSelectionFromTop(mVideoGalleryRight.getFirstVisiblePosition(), top);
				} else if (view.equals(mVideoGalleryRight)) {
					rightViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

					int h = 0;
					for (int i = 0; i < mVideoGalleryLeft.getFirstVisiblePosition(); i++) {
						h += leftViewsHeights[i];
					}

					int hi = 0;
					for (int i = 0; i < mVideoGalleryRight.getFirstVisiblePosition(); i++) {
						hi += rightViewsHeights[i];
					}

					int top = h - hi + view.getChildAt(0).getTop();
					mVideoGalleryLeft.setSelectionFromTop(mVideoGalleryLeft.getFirstVisiblePosition(), top);
				}

			}

		}
	};
}