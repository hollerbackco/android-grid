package com.moziy.hollerback.util;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.PageLoadingFragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public class LoadingFragmentUtil {
    private String TAG = "LoadingFragmentUtil";

    private Fragment mFragment;
    private SherlockFragmentActivity mActivity;

    private boolean isLoading = false;

    public LoadingFragmentUtil(SherlockFragmentActivity activity) {
        mFragment = new PageLoadingFragment();
        mActivity = activity;
    }

    /**
     * Starting to loading the loading fragment
     */
    public void startLoading() {
        ViewGroup viewgroup = (ViewGroup) mActivity.findViewById(R.id.llWaitView);
        if (!isLoading && mActivity != null && viewgroup != null) {
            try {
                isLoading = true;
                mFragment = new PageLoadingFragment();
                viewgroup.setVisibility(View.VISIBLE);
                viewgroup.removeAllViewsInLayout();
                mActivity.getSupportFragmentManager().beginTransaction().add(R.id.llWaitView, mFragment, TAG).commitAllowingStateLoss();
            } catch (java.lang.IllegalStateException e) {
                isLoading = false;

                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * Stops loading the loading fragment
     */
    public void stopLoading() {
        View waitView = mActivity.findViewById(R.id.llWaitView);
        if (isLoading && mFragment != null && mActivity != null && waitView != null && !mActivity.isFinishing()) {
            try {
                isLoading = false;
                mActivity.findViewById(R.id.llWaitView).setVisibility(View.GONE);
                mActivity.getSupportFragmentManager().beginTransaction().remove(mFragment).commitAllowingStateLoss();
                mFragment = null;
            } catch (java.lang.IllegalStateException e) {
                isLoading = true;
                e.printStackTrace();
            }
        }
        return;
    }
}
