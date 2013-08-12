package com.moziy.hollerback.activity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

public class SettingPreferenceActivity extends SherlockPreferenceActivity{
	private final String TWITTERURL = "https://twitter.com/hollerback";
	private final String FACEBOOKURL = "https://www.facebook.com/HollerbackApp";
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		finish();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
	
	Preference preference_friends;
	Preference preference_logout;
	Preference preference_username;
	Preference preference_phone;
	SwitchPreference preference_save;
	Preference preference_terms;
	Preference preference_privacy;
	Preference preference_feedback;
	Preference preference_twitter;
	Preference preference_facebook;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Example); 
    	super.onCreate(savedInstanceState);
    	this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	
    	addPreferencesFromResource(R.xml.app_preferences);
    	
    	preference_friends = (Preference)getPreferenceScreen().findPreference("preference_friends");
		preference_friends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingPreferenceActivity.this, ContactsActivity.class);
				SettingPreferenceActivity.this.startActivity(intent);
				return false;
			}
		});
		
		preference_logout = (Preference)getPreferenceScreen().findPreference("preference_logout");
		preference_logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_username = (Preference)getPreferenceScreen().findPreference("preference_username");
		preference_username.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_phone = (Preference)getPreferenceScreen().findPreference("preference_phone");
		preference_phone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_save = (SwitchPreference)getPreferenceScreen().findPreference("preference_save");
		preference_save.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_terms = (Preference)getPreferenceScreen().findPreference("preference_terms");
		preference_terms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_privacy = (Preference)getPreferenceScreen().findPreference("preference_privacy");
		preference_privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_feedback = (Preference)getPreferenceScreen().findPreference("preference_feedback");
		preference_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				return false;
			}
		});
		
		preference_twitter = (Preference)getPreferenceScreen().findPreference("preference_twitter");
		preference_twitter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
		        Intent i = new Intent(Intent.ACTION_VIEW);  
		        i.setData(Uri.parse(TWITTERURL));  
		        startActivity(i);  
				return false;
			}
		});
		
		preference_facebook = (Preference)getPreferenceScreen().findPreference("preference_facebook");
		preference_facebook.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
		        Intent i = new Intent(Intent.ACTION_VIEW);  
		        i.setData(Uri.parse(FACEBOOKURL));  
		        startActivity(i);
				return false;
			}
		});
	}
}
