package com.moziy.hollerback.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.krish.horizontalscrollview.CustomListBaseAdapter;
import com.moziy.hollerback.HollerbackInterfaces.OnCustomItemClickListener;
import com.moziy.hollerback.R;
import com.moziy.hollerback.bitmap.ImageCache;
import com.moziy.hollerback.bitmap.ImageFetcher;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.VideoUploadService;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.util.JsonModelUtil;
import com.moziy.hollerback.util.QU;
import com.moziy.hollerback.util.UploadCacheUtil;
import com.moziy.hollerback.view.CustomVideoView;
import com.origamilabs.library.views.StaggeredGridView;

public class ConversationFragment extends BaseFragment {

	/**
	 * This piece of shit takes up 100% height unless you restrict it
	 */
	private StaggeredGridView mVideoGallery;
	private CustomListBaseAdapter mVideoGalleryAdapter;
	private ViewGroup mRootView;
	private TextView mTxtVideoInfo;
	private SherlockFragmentActivity mActivity;
	private LinearLayout mWrapperInformation;
	// Image Loading
	private ImageFetcher mImageFetcher;
	private int mImageThumbSize;

	private static final String IMAGE_CACHE_DIR = "thumbs";

	// Image Loading

	// Video Playback Stuff
	private S3RequestHelper mS3RequestHelper;

	// Reply stuff
	private ImageButton mReplyBtn;

	public int TAKE_VIDEO = 0x683;

	private String mConversationId;

	// state
	boolean urlLoaded = false;
	private ConversationModel conversation;

	private ArrayList<VideoModel> mVideos;
	boolean playStartInitialized;	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
	    	case R.id.action_info:
	    		ConversationMembersFragment fragment = ConversationMembersFragment.newInstance(mConversationId);
				mActivity.getSupportFragmentManager()
				.beginTransaction().replace(R.id.fragment_holder, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		        .addToBackStack(ConversationMembersFragment.class.getSimpleName())
		        .commitAllowingStateLoss();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    	menu.clear();
        inflater.inflate(R.menu.conversation, menu);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = this.getSherlockActivity();
		mRootView = (ViewGroup)inflater.inflate(R.layout.conversation_fragment,
				null);
		
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				getActivity(), IMAGE_CACHE_DIR);

		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.placeholder_sq);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);

		mS3RequestHelper = new S3RequestHelper();

		initializeView(mRootView);

		initializeArgs();
		
		return mRootView;
	}

	public void playNewestVideo() {
		if (playStartInitialized) {
			if (mVideoGalleryAdapter.getCount() > 0) {

				mVideoGalleryAdapter.notifyDataSetChanged();

			}
			setGalleryToEnd();
			return;
		}

		if (mVideoGalleryAdapter.getCount() < 1) {
			return;
		}

		playStartInitialized = true;
		boolean set = false;

		for (int i = mVideoGalleryAdapter.getCount() - 1; i >= 0; i--) {
			if (mVideoGalleryAdapter.getItem(i).isRead() == false) {

				// model.setRead(true);
				set = true;
				mVideoGalleryAdapter.notifyDataSetChanged();
				// }
			}
		}

		if (!set) {
			if (mVideoGalleryAdapter.getCount() > 0) {

				mVideoGalleryAdapter.notifyDataSetChanged();

			}
			setGalleryToEnd();
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
		IABroadcastManager.unregisterLocalReceiver(receiver);
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
		this.startLoading();

		// TODO: Do this less often
		mImageFetcher.setExitTasksEarly(false);
		
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_GET_URLS);
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_GET_CONVERSATION_VIDEOS);
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_UPLOAD_VIDEO_UPDATE);
		QU.getDM().getVideos(false, mConversationId);

	}

	public void initializeArgs() {
		Bundle bundle = getArguments();
		mConversationId = bundle.getString("conv_id");
//		UploadCacheUtil.clearCache(this.getActivity(), mConversationId);
				
		//this is object oriented programming, retain Application from Activity.
		//Please change the caching system in the future
		conversation = QU.getConv(mConversationId);
		if(conversation != null)
		{
			LogUtil.i("Conversation Fragment: ID: " + mConversationId);
			mActivity.getSupportActionBar().setTitle(conversation.getConversationName());
		}
		else
		{
			mActivity.getSupportFragmentManager().popBackStack();
		}
	}

	@Override
	protected void initializeView(View view) {
		mWrapperInformation = (LinearLayout)mRootView.findViewById(R.id.wrapperInformation);
		mWrapperInformation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewGroup convertView = (ViewGroup)mWrapperInformation.getTag();
				final ViewGroup gridWrapper = (ViewGroup)convertView.findViewById(R.id.gridWrapper);
				if(gridWrapper!= null)
				{
			        final CustomVideoView videoView = (CustomVideoView)gridWrapper.findViewById(R.id.videoPlayer);
			        videoView.changeVideoSize(gridWrapper.getWidth()*2, gridWrapper.getHeight()*2);
			        int length=videoView.getCurrentPosition();
	
			        videoView.pause();
			        gridWrapper.removeView(videoView);
	
			        final LinearLayout blowupView = (LinearLayout)mRootView.findViewById(R.id.blowupView);			        
					blowupView.addView(videoView);
					blowupView.setVisibility(View.VISIBLE);
					videoView.seekTo(length);
					videoView.setBlowupParentView(blowupView);
					videoView.start();
					blowupView.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							videoView.setBlowupParentView(null);
							videoView.setVisibility(View.GONE);
							blowupView.removeView(videoView);
							gridWrapper.addView(videoView);
							blowupView.setVisibility(View.GONE);
						}
					});
				}
			}
		});
		
		mVideoGallery = (StaggeredGridView) view
				.findViewById(R.id.hlz_video_gallery);
		mVideoGallery.setColumnCount(2);
		
		mVideoGalleryAdapter = new CustomListBaseAdapter(mActivity,
				mImageFetcher, mS3RequestHelper, mWrapperInformation);
		
		mVideoGallery.setAdapter(mVideoGalleryAdapter);
		
		mVideoGalleryAdapter.setOnCustomItemClickListener(mListener);
		mReplyBtn = (ImageButton) view.findViewById(R.id.btn_video_reply);

		mReplyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecordVideoFragment fragment = RecordVideoFragment.newInstance(mConversationId, conversation.getConversationName());
				mActivity.getSupportFragmentManager()
				.beginTransaction().replace(R.id.fragment_holder, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		        .addToBackStack(null)
		        .commitAllowingStateLoss();

			}
		});
		
		mTxtVideoInfo = (TextView)mRootView.findViewById(R.id.txtVideoInfo);
	}

	int currentPlayingPosition;
	OnCustomItemClickListener mListener = new OnCustomItemClickListener() {

		@Override
		public void onItemClicked(int position, View convertView) {
			
			LogUtil.i("Clicked ON: " + position);
			currentPlayingPosition = position;
			VideoModel model = mVideoGalleryAdapter.getItem(position);
			
			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
				Date date = df.parse(model.getCreateDate());
				mTxtVideoInfo.setText(model.getUserName() + " " + ConversionUtil.timeAgo(date));

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			mWrapperInformation.setVisibility(View.VISIBLE);
			mWrapperInformation.setTag(convertView);
		}
	};

	/**
	 * Create a new instance of CountingFragment, providing "num" as an
	 * argument.
	 */
	public static ConversationFragment newInstance(String conversation_id) {

		ConversationFragment f = new ConversationFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("conv_id", conversation_id);
		f.setArguments(args);
		return f;
	}
	
	/**
	 * Create a new instance of CountingFragment, providing "num" as an
	 * argument.
	 */
	public static ConversationFragment newInstance(String conversation_id, boolean startRecording) {

		ConversationFragment f = new ConversationFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("conv_id", conversation_id);
		args.putBoolean("startRecording", startRecording);
		f.setArguments(args);
		return f;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(IABIntent.isIntent(intent, IABIntent.INTENT_UPLOAD_VIDEO_UPDATE))
			{
				/*
				ArrayList<VideoModel> tempVideos = (ArrayList<VideoModel>) QU
						.getDM()
						.getObjectForToken(
								intent.getStringExtra(IABIntent.PARAM_INTENT_DATA));
				
				//Currently that's only last one, going to update this logic later when it gets
				//more usage
				mVideos.remove(mVideos.size() - 1);
				mVideos.add(tempVideos.get(0));
				mVideoGalleryAdapter.notifyDataSetChanged();
				if (mVideoGalleryAdapter.getCount() > 0) {
					setGalleryToEnd();
				}
				*/
				
				//stops the progress
				for(int i=0; i < mVideoGalleryAdapter.mUploadingHelper.size(); i++)
				{
					mVideoGalleryAdapter.mUploadingHelper.get(i).getProgressHelper().hideLoader();
					mVideoGalleryAdapter.mUploadingHelper.get(i).getTxtSent().setVisibility(View.VISIBLE);
				}
				
				if(mVideos != null)
				{
					for(int i = 0; i < mVideos.size(); i++)
					{
					    //XXX: Why are we checking for is uploading and then setting the setUploading to false?
						if(mVideos.get(i).isUploading()) 
						{
							mVideos.get(i).setUploading(false);
							mVideos.get(i).setSent(true);
						}
					}
				}
				
				mVideoGalleryAdapter.mUploadingHelper.clear();
			}
			else if (IABIntent.isIntent(intent,
					IABIntent.INTENT_GET_CONVERSATION_VIDEOS)) {

				ArrayList<VideoModel> tempVideos = (ArrayList<VideoModel>) QU
						.getDM()
						.getObjectForToken(
								intent.getStringExtra(IABIntent.PARAM_INTENT_DATA));
				
				// helper.getS3URLParams(generateUploadParams(hash,
				// intent.getStringExtra(IABIntent.PARAM_ID)));

				//TODO - Sajjad : Review this!
				if (mVideos != null && mVideos.equals(tempVideos) && mVideoGalleryAdapter.getCount() != 0) {
					LogUtil.i("Setting: same data set");
					return;
				}

				mVideos = (ArrayList<VideoModel>) tempVideos.clone();

				//TODO: Come back here for review, what is being cached here, and how is it related to Upload.
				if(UploadCacheUtil.hasVideoCache(mActivity, mConversationId))
				{
					//upload service running
					if(isUploadRunning())
					{
						JSONArray caches = UploadCacheUtil.getCacheFlags(mActivity, mConversationId);
						for(int i = 0; i < caches.length(); i++)
						{
							try {
								VideoModel tmpmodel = JsonModelUtil
										.createVideo(caches.getJSONObject(i));							
								mVideos.add(tmpmodel);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else
					{
						//if service is no longer running, clean the cache.
						UploadCacheUtil.clearCache(mActivity, mConversationId);
					}
				}
				
				if (mVideos != null) {

					LogUtil.i("Setting Received videos: " + mVideos.size());

					
					if(mVideoGalleryAdapter.getCount() == 0)
					{
						mVideoGalleryAdapter.setListItems(mVideos);	
					}
					mVideoGalleryAdapter.notifyDataSetChanged();
					LogUtil.d("Setting new Videos size: " + mVideos.size());

					// TODO: Fix issues here
					LogUtil.i("Setting center index: " + mVideos.size());
					LogUtil.i("Setting count index: "
							+ mVideoGalleryAdapter.getCount());
					if (mVideoGalleryAdapter.getCount() > 0) {
						setGalleryToEnd();
					}
				}
				ConversationFragment.this.stopLoading();
			}
		}
	};
	
	/**
	 * check if service is running
	 * @param tmp
	 * @return
	 */
	public boolean isUploadRunning()
	{
	    ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (VideoUploadService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	private void setGalleryToEnd() {
		mVideoGallery.post(new Runnable() {
			@Override
			public void run() {
			    Log.d("sajjad", "scroll to bottom" );
//				mVideoGallery.scrollToBottom();
			}
		});
	}
}