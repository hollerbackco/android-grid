package com.moziy.hollerback.fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.util.GifView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFinishFragment extends BaseFragment{
	private ViewGroup mRootView;
	private static SherlockFragmentActivity mActivity;
	private GifView mGifWrapper;
	
	private Button mBtnRecord;
	private Button mBtnConversations;
	
	public static WelcomeFinishFragment newInstance(){
		WelcomeFinishFragment fragment = new WelcomeFinishFragment();
		return fragment;
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		this.getSherlockActivity().getSupportActionBar().hide();
				
		mActivity = this.getSherlockActivity();
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_finish, null);
        initializeView(mRootView);
        return mRootView;
    }
	
	@Override
	protected void initializeView(View view) {
		mGifWrapper = (GifView)mRootView.findViewById(R.id.gifPenutButter);
		mGifWrapper.setGif(R.drawable.penut_butter);
		mGifWrapper.play();
		
		mBtnRecord = (Button)mRootView.findViewById(R.id.btnRecord);
		mBtnRecord.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				WelcomeRecordVideoFragment fragment = WelcomeRecordVideoFragment.newInstance();
				mActivity.getSupportFragmentManager()
				.beginTransaction().replace(R.id.fragment_holder, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		        .addToBackStack(WelcomeRecordVideoFragment.class.getSimpleName())
		        .commitAllowingStateLoss();
			}
		});
		
		mBtnConversations = (Button)mRootView.findViewById(R.id.btnConversations);
		mBtnConversations.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mActivity, HollerbackMainActivity.class); 
				mActivity.startActivity(intent);
				mActivity.finish();
			}
		});
	}
	
}
