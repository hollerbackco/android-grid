package com.moziy.hollerback.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.WelcomeFragmentActivity;
import com.moziy.hollerback.helper.CustomActionBarHelper;
import com.moziy.hollerback.util.HollerbackAppState;

public class SettingsFragment extends BaseFragment {

	Button mLogoutBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.settings_fragment, null);
		initializeView(fragmentView);
		return fragmentView;
	}

	@Override
	protected void initializeView(View view) {
		mLogoutBtn = (Button) view.findViewById(R.id.btn_logout);
		mLogoutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (HollerbackAppState.isValidSession()) {
					HollerbackAppState.logOut();
					Intent intent = new Intent(getActivity(),
							WelcomeFragmentActivity.class);
					getActivity().startActivity(intent);
					getActivity().finish();
				}

			}
		});

	}

	/*
	@Override
	protected void onActionBarIntialized(CustomActionBarHelper viewHelper) {
		// TODO Auto-generated method stub
		viewHelper.setSettingsFragmentSettings();
	}
*/
}
