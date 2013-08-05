package com.moziy.hollerback.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.AddConversationFragment;
import com.moziy.hollerback.fragment.ConversationFragment;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.helper.CustomActionBarHelper;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.FlurryC;
import com.moziy.hollerback.util.HollerbackAppState;

/**
 * Main Activity that gets initiated when user is signed in
 * 
 * @author jianchen
 * 
 */
public class HollerbackBaseActivity extends SherlockFragmentActivity {

	private static CustomActionBarHelper mActionBarView;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		if (AppEnvironment.getInstance().LOG_CRASHES) {
			Crittercism.init(getApplicationContext(),
					AppEnvironment.getInstance().CRITTERCISM_ID);
		}
		
		setTheme(R.style.HollerbackTheme_ActionBarStyle); 

		setContentView(R.layout.hollerback_main);
//        getSupportActionBar().show();

		mActionBarView = new CustomActionBarHelper(
				findViewById(R.id.action_bar_parent));

		LogUtil.i("Starting BaseActivity");

		if (!HollerbackAppState.isValidSession()) {
			Intent i = new Intent(HollerbackBaseActivity.this,
					WelcomeFragmentActivity.class);
			startActivity(i);
			this.finish();
		}

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
	protected void onStop() {
		// TODO Auto-generated method stub
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	public static CustomActionBarHelper getCustomActionBar() {
		return mActionBarView;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (initFrag) {
			startConversationFragment(convId);
			initFrag = false;
		} else if (getIntent().getStringExtra(IABIntent.PARAM_CONVERSATION_ID) != null) {

			// TODO: to get this correctly implemented, you need to make sure
			// that the converastion model already exists, if not fetch it
			startConversationFragment(getIntent().getStringExtra(
					IABIntent.PARAM_CONVERSATION_ID));
			getIntent().removeExtra(IABIntent.PARAM_CONVERSATION_ID);
		}
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

	public void startConversationFragment(String conversationId) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		int count = fragmentManager.getBackStackEntryCount();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		for (int i = 0; i < count; i++) {
			fragmentManager.popBackStackImmediate();
		}
		ConversationListFragment fragment = new ConversationListFragment();
		fragmentTransaction.add(R.id.fragment_holder, fragment);

		ConversationFragment convfragment = ConversationFragment
				.newInstance(conversationId);
		fragmentTransaction.replace(R.id.fragment_holder, convfragment);
		fragmentTransaction.addToBackStack(AddConversationFragment.class
				.getSimpleName());

		fragmentTransaction.commit();
	}

	public void addContactListFragment(android.app.FragmentTransaction ft,
			SortedArray result) {
		TempMemoryStore.users = result;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		AddConversationFragment fragment = new AddConversationFragment();
		fragmentTransaction.replace(R.id.fragment_holder, fragment);
		fragmentTransaction.addToBackStack(AddConversationFragment.class
				.getSimpleName());
		fragmentTransaction.commit();
	}

	boolean initFrag = false;
	String convId = null;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.i("Receiving onActivityResult: " + requestCode);
		if (requestCode == IABIntent.REQUEST_NEW_CONVERSATION) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// The user picked a contact.
				// The Intent's data Uri identifies which contact was selected.
				// initFragment();
				initFrag = true;
				convId = data.getStringExtra(IABIntent.PARAM_INTENT_MSG);
				// Do something with the contact here (bigger example below)
			}
		}
	}
}
