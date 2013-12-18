package com.moziy.hollerback.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;

public class SettingPreferenceActivity extends SherlockPreferenceActivity {

    public interface Action {
        public static final String LOGOUT = "logout";
        public static final String FIND_FRIENDS = "find_friends";
    }

    private final String TWITTERURL = "https://twitter.com/hollerback";
    private final String FACEBOOKURL = "https://www.facebook.com/HollerbackApp";
    public static final int PREFERENCE_PAGE_REQUEST_CODE = 200;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
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
    Preference preference_terms;
    Preference preference_privacy;
    Preference preference_feedback;
    Preference preference_twitter;
    Preference preference_facebook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.action_settings);
        this.setResult(RESULT_CANCELED);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);

        View customView = inflater.inflate(R.layout.header_title, null);
        TextView txtTitle = (TextView) customView.findViewById(R.id.title);
        txtTitle.setText(this.getString(R.string.action_settings));

        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setCustomView(customView);
        this.getSupportActionBar().setIcon(R.drawable.icon_banana);

        addPreferencesFromResource(R.xml.app_preferences);

        preference_friends = (Preference) getPreferenceScreen().findPreference("preference_friends");
        preference_friends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(HollerbackApplication.getInstance(), "We are working hard to get this to you ASAP!", Toast.LENGTH_LONG).show();
                SettingPreferenceActivity.this.finish();
                return false;
            }
        });

        preference_logout = (Preference) getPreferenceScreen().findPreference("preference_logout");
        preference_logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                HollerbackAppState.logOut(SettingPreferenceActivity.this);

                Bundle args = new Bundle();
                args.putBoolean(Action.LOGOUT, true);

                Intent intent = new Intent();
                intent.putExtras(args);
                setResult(RESULT_OK, intent);
                SettingPreferenceActivity.this.finish();

                return true;
            }
        });

        preference_username = (Preference) getPreferenceScreen().findPreference("preference_username");
        preference_username.setTitle(PreferenceManagerUtil.getPreferenceValue(HBPreferences.USERNAME, ""));
        preference_username.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                return false;
            }
        });

        preference_phone = (Preference) getPreferenceScreen().findPreference("preference_phone");
        preference_phone.setTitle(PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, ""));
        preference_phone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                return false;
            }
        });

        preference_terms = (Preference) getPreferenceScreen().findPreference("preference_terms");
        preference_terms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(AppEnvironment.TERMS_OF_SERVICE_URL));
                startActivity(intent);
                return false;
            }
        });

        preference_privacy = (Preference) getPreferenceScreen().findPreference("preference_privacy");
        preference_privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(AppEnvironment.PRIVACY_POLICY_URL));
                startActivity(intent);
                return true;
            }
        });

        preference_feedback = (Preference) getPreferenceScreen().findPreference("preference_feedback");
        preference_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                return false;
            }
        });

        preference_twitter = (Preference) getPreferenceScreen().findPreference("preference_twitter");
        preference_twitter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(TWITTERURL));
                startActivity(i);
                return false;
            }
        });

        preference_facebook = (Preference) getPreferenceScreen().findPreference("preference_facebook");
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
