package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.LoadingFragmentUtil;

public abstract class BaseFragment extends SherlockFragment {
	protected SherlockFragmentActivity mActivity;
	private LoadingFragmentUtil mLoading;
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		this.getFragmentManager().popBackStack();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = this.getSherlockActivity();
        mLoading = new LoadingFragmentUtil(mActivity);
        
        mActivity.getSupportActionBar().show();
        mActivity.getSupportActionBar().setIcon(R.drawable.icon);
        mActivity.getSupportActionBar().setHomeButtonEnabled(true);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
		mLoading.stopLoading();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
       menu.clear();
    }
	
	@Override
	public void onResume() {
		//onActionBarIntialized(HollerbackBaseActivity.getCustomActionBar());
		super.onResume();
	}

	protected abstract void initializeView(View view);
	
	protected void startLoading()
	{
		mLoading.startLoading();
	}
	
	protected void stopLoading()
	{
		mLoading.stopLoading();
	}
	/*
	protected abstract void onActionBarIntialized(
			CustomActionBarHelper viewHelper);
*/
}
