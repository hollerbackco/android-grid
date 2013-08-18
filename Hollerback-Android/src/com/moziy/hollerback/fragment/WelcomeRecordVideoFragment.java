package com.moziy.hollerback.fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.HollerbackAppState;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeRecordVideoFragment extends RecordVideoFragment{
	private String NEXT = "NEXT";
	private Boolean showNextButton = false;;
	public static WelcomeRecordVideoFragment newInstance()
	{
		WelcomeRecordVideoFragment fragment = new WelcomeRecordVideoFragment();
		fragment.setArguments(new Bundle());
		return fragment;
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    	
    	if(!showNextButton)
    	{
    		return;
    	}
    	
    	menu.add(NEXT)
		.setActionView(R.layout.button_next)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        ;
    	
    	for(int i = 0; i < menu.size(); i++)
    	{
    		if(menu.getItem(i).getTitle().toString().equalsIgnoreCase(NEXT))
    		{	//Finding the button from custom View
    			menu.getItem(i).getActionView().findViewById(R.id.btnSignUp)
    			.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startSignUpFragment();
					}
				});
    			break;
    		}
    	}
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		this.getSherlockActivity().getSupportActionBar().setTitle(R.string.record_message);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    protected void startRecording(){
    	super.startRecording();
    	this.getSherlockActivity().getSupportActionBar().setTitle(R.string.record_recording);
    }
    
    @Override
    protected void stopRecording() {
    	super.stopRecording();
    	this.getSherlockActivity().getSupportActionBar().setTitle(R.string.record_review);
    	mSendButton.setVisibility(View.GONE);
    	showNextButton = true;
    	mActivity.invalidateOptionsMenu();
    }
    

	public void startSignUpFragment() {
		this.getFragmentManager().popBackStack();
		if(!HollerbackAppState.isValidSession())
		{
			//this part is that when it's not signed in through;
			FragmentManager fragmentManager = getActivity()
					.getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			SignUpFragment fragment = SignUpFragment.newInstance(mFileDataName);
			fragmentTransaction.replace(R.id.fragment_holder, fragment);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction
					.addToBackStack(SignUpFragment.class.getSimpleName());
			fragmentTransaction.commit();
		}
		else
		{
			ContactsFragment fragment = ContactsFragment.newInstance(true, mFileDataName);
			mActivity.getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_holder, fragment)
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
	        .addToBackStack(ContactsFragment.class.getSimpleName())
	        .commitAllowingStateLoss();
		}
	}
}
