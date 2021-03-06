package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.moziy.hollerback.util.LoadingFragmentUtil;

public abstract class BaseFragment extends SherlockFragment {
    protected SherlockFragmentActivity mActivity;
    private LoadingFragmentUtil mLoading;

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

        mActivity = this.getSherlockActivity();
        if (!(this instanceof RecordVideoFragment)) {
            setHasOptionsMenu(true);

            mLoading = new LoadingFragmentUtil(mActivity);

            mActivity.getSupportActionBar().show();
            mActivity.getSupportActionBar().setIcon(R.drawable.banana_medium);
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            mActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
        } else {
            mActivity.getSupportActionBar().hide();
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
    public void onPause() {
        if (!(this instanceof RecordVideoFragment)) {
            mLoading.stopLoading();
        }
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        if (!(this instanceof RecordVideoFragment)) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View customView = inflater.inflate(R.layout.header_title, null);
            TextView txtTitle = (TextView) customView.findViewById(R.id.title);
            txtTitle.setText(mActivity.getSupportActionBar().getTitle().toString());

            mActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
            mActivity.getSupportActionBar().setCustomView(customView);
        }
        super.onResume();
    }

    protected void initializeView(View view) {

    }

    protected void startLoading() {
        mLoading.startLoading();
    }

    protected void stopLoading() {
        mLoading.stopLoading();
    }

    protected abstract String getFragmentName();
    /*
     * protected abstract void onActionBarIntialized( CustomActionBarHelper viewHelper);
     */
}
