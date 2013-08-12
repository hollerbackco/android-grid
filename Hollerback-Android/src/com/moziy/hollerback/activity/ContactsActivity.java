package com.moziy.hollerback.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.crittercism.app.Crittercism;
import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.ContactsFragment;
import com.moziy.hollerback.util.AppEnvironment;

public class ContactsActivity extends SherlockFragmentActivity {

	boolean initFrag = false;
	String convId = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setTheme(R.style.Theme_Example); 

		super.onCreate(savedInstanceState);
	
		this.getSupportActionBar().show();
		
		if (AppEnvironment.getInstance().LOG_CRASHES) {
			Crittercism.init(getApplicationContext(),
					AppEnvironment.getInstance().CRITTERCISM_ID);
		}
		
		setContentView(R.layout.hollerback_main);
		
		ContactsFragment fragment = ContactsFragment.newInstance();
		this.getSupportFragmentManager()
		.beginTransaction().add(R.id.fragment_holder, fragment)
		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .commitAllowingStateLoss();
	}
}
