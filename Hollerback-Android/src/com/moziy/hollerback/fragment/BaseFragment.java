package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.util.LoadingFragmentUtil;

public abstract class BaseFragment extends SherlockFragment {
    private static final String CHILD_FRAGMENT_BUNDLE_ARG_KEY = "CHILD_FRAGMENT";
    protected SherlockFragmentActivity mActivity;
    private LoadingFragmentUtil mLoading;
    // protected TextView mActionBarTitle;
    protected boolean mIsChildFragment;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // ((HollerbackMainActivity) getActivity()).initFragment();
                getFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CHILD_FRAGMENT_BUNDLE_ARG_KEY)) {
                mIsChildFragment = savedInstanceState.getBoolean(CHILD_FRAGMENT_BUNDLE_ARG_KEY);
            }
        }

        mActivity = this.getSherlockActivity();
        ActionBar supportActionBar = mActivity.getSupportActionBar();
        if (!(this instanceof RecordVideoFragment)) {

            if (!mIsChildFragment) {
                setHasOptionsMenu(true);
                //
                mLoading = new LoadingFragmentUtil(mActivity);
                supportActionBar.setIcon(R.drawable.banana_medium);
                supportActionBar.setHomeButtonEnabled(true);
                supportActionBar.setDisplayHomeAsUpEnabled(true);
                supportActionBar.setDisplayShowTitleEnabled(false);
                supportActionBar.setDisplayShowCustomEnabled(true);
                //
                // if (supportActionBar.getCustomView() == null) { // if there is no custom view, set it
                //
                // // set custom view for the title
                // LayoutInflater inflater = LayoutInflater.from(mActivity);
                // View customView = inflater.inflate(R.layout.header_title, null);
                // mActionBarTitle = (TextView) customView.findViewById(R.id.title);
                // mActionBarTitle.setText(getActionBarTitle());
                //
                // supportActionBar.setCustomView(customView);
                // supportActionBar.setDisplayShowCustomEnabled(true);
                // } else {
                // mActionBarTitle = (TextView) supportActionBar.getCustomView().findViewById(R.id.title);
                // }
                // supportActionBar.show();
            }

        } else {
            supportActionBar.hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Tracker tracker = EasyTracker.getInstance(mActivity);
        if (tracker != null) {
            tracker.set(Fields.SCREEN_NAME, getFragmentName());
            tracker.send(MapBuilder.createAppView().build());
        }
        // EasyTracker.getInstance(mActivity). .sendView("Home Screen"); // Where myTracker is an instance of Tracker.
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsChildFragment) {
            ((HollerbackMainActivity) getActivity()).getCustomActionBarTitle().setText(getActionBarTitle());
        }
    }

    @Override
    public void onPause() {
        if (!(this instanceof RecordVideoFragment)) {
            if (!mIsChildFragment) {
                mLoading.stopLoading();
            }
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CHILD_FRAGMENT_BUNDLE_ARG_KEY, mIsChildFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    protected String getActionBarTitle() {
        return "";
    }

    protected void initializeView(View view) {

    }

    protected void startLoading() {
        mLoading.startLoading();
    }

    protected void stopLoading() {
        mLoading.stopLoading();
    }

    public void setChildFragment() {
        mIsChildFragment = true;
    }

    public void clearChildFragment() {
        mIsChildFragment = false;
    }

    protected abstract String getFragmentName();
    /*
     * protected abstract void onActionBarIntialized( CustomActionBarHelper viewHelper);
     */
}
