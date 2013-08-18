package com.moziy.hollerback.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.FlurryC;
import com.moziy.hollerback.util.HollerbackAppState;

public class HollerbackMainActivity extends SherlockFragmentActivity {
	//this way the state is always available
	public HollerbackApplication application;
	
	boolean initFrag = false;
	String convId = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setTheme(R.style.Theme_Example); 

		super.onCreate(savedInstanceState);

		application = HollerbackApplication.getInstance();
				
		if (AppEnvironment.getInstance().LOG_CRASHES) {
			Crittercism.init(getApplicationContext(),
					AppEnvironment.getInstance().CRITTERCISM_ID);
		}
		
		
		LogUtil.i("Starting MainActivity");

		if (!HollerbackAppState.isValidSession()) {
			Intent i = new Intent(this,
					WelcomeFragmentActivity.class);
			startActivity(i);
			this.finish();
		}
		this.getSupportActionBar().show();

		setContentView(R.layout.hollerback_main);

		initFragment();
		LogUtil.i("Completed BaseActivity");

		FlurryAgent
				.onStartSession(this, AppEnvironment.getInstance().FLURRY_ID);

		FlurryAgent.logEvent(FlurryC.EVENT_STARTSESSION, AnalyticsUtil.getMap(
				FlurryC.PARAM_MODELNAME, AnalyticsUtil.getDeviceName(),
				FlurryC.PARAM_OS_VERISON,
				Integer.toString(Build.VERSION.SDK_INT)));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!HollerbackAppState.isValidSession()) {
			Intent i = new Intent(this,
					WelcomeFragmentActivity.class);
			startActivity(i);
			this.finish();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	public void initFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();

		int count = fragmentManager.getBackStackEntryCount();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		for (int i = 0; i < count; i++) {
			fragmentManager.popBackStackImmediate();
		}
		ConversationListFragment fragment = new ConversationListFragment();
		fragmentTransaction.add(R.id.fragment_holder, fragment);
		fragmentTransaction.commit();
	}
}
