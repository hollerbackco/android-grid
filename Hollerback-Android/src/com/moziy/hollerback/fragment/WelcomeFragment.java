package com.moziy.hollerback.fragment;

import com.moziy.hollerback.R;
import com.moziy.hollerback.helper.CustomActionBarHelper;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFragment extends BaseFragment implements OnClickListener {

	private Button mSignInBtn, mRegisterBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.welcome_fragment, null);

		initializeView(fragmentView);
		return fragmentView;
	}

	@Override
	protected void initializeView(View view) {
		// TODO Auto-generated method stub
		mSignInBtn = (Button) view.findViewById(R.id.btn_signin);
		mRegisterBtn = (Button) view.findViewById(R.id.btn_signup);

		mSignInBtn.setOnClickListener(this);
		mRegisterBtn.setOnClickListener(this);

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public static WelcomeFragment newInstance(int num) {

		WelcomeFragment f = new WelcomeFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_signin:
			startSignInFragment();
			break;
		case R.id.btn_signup:
			startSignUpFragment();
			break;
		}

	}

	public void startSignInFragment() {
		FragmentManager fragmentManager = getActivity()
				.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		SignInFragment fragment = new SignInFragment();
		fragmentTransaction.replace(R.id.fragment_holder, fragment);
		fragmentTransaction
				.addToBackStack(SignInFragment.class.getSimpleName());
		fragmentTransaction.commit();
	}

	public void startSignUpFragment() {
		FragmentManager fragmentManager = getActivity()
				.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		SignUpFragment fragment = new SignUpFragment();
		fragmentTransaction.replace(R.id.fragment_holder, fragment);
		fragmentTransaction
				.addToBackStack(SignUpFragment.class.getSimpleName());
		fragmentTransaction.commit();
	}

	/*
	@Override
	protected void onActionBarIntialized(CustomActionBarHelper viewHelper) {
		// TODO Auto-generated method stub

	}
	*/
}
