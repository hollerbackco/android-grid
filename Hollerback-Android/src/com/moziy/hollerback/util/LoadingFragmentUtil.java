package com.moziy.hollerback.util;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.AbstractActivity;
import com.moziy.hollerback.fragment.PageLoadingFragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public class LoadingFragmentUtil {
	private String TAG = "LoadingFragmentUtil";
	
	private Fragment mFragment;
	private SherlockFragmentActivity mActivity;
	
	public LoadingFragmentUtil(SherlockFragmentActivity activity)
	{
		mFragment =  new PageLoadingFragment();
		mActivity = activity;
	}
	
	/**
	 * Starting to loading the loading fragment
	 */
	public void startLoading()
	{
		ViewGroup viewgroup = (ViewGroup)mActivity.findViewById(R.id.llWaitView);
		if(mActivity != null && viewgroup != null && viewgroup.getVisibility() != View.VISIBLE)
		{
			viewgroup.removeAllViewsInLayout();
			mActivity.findViewById(R.id.llWaitView).setVisibility(View.VISIBLE);
			mActivity.getSupportFragmentManager().beginTransaction().
			add(R.id.llWaitView, mFragment, TAG).
			commitAllowingStateLoss();
		}
		return;
	}
	
	/**
	 * Stops loading the loading fragment
	 */
	public void stopLoading()
	{
		View waitView = mActivity.findViewById(R.id.llWaitView);
		if(mActivity != null && waitView != null && waitView.getVisibility() != View.GONE && !mActivity.isFinishing())
		{
			try
			{
				mActivity.findViewById(R.id.llWaitView).setVisibility(View.GONE);
				mActivity.getSupportFragmentManager().beginTransaction().
				remove(mFragment).
				commitAllowingStateLoss();
			}
			catch(java.lang.IllegalStateException e)
			{
				e.printStackTrace();
			}
		}
		return;
	}
}
