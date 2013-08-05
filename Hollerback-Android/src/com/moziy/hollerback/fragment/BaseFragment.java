package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public abstract class BaseFragment extends SherlockFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

	/*
	protected abstract void onActionBarIntialized(
			CustomActionBarHelper viewHelper);
*/
}
