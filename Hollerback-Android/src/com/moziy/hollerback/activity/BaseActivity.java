package com.moziy.hollerback.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class BaseActivity extends SherlockFragmentActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";

    protected TextView mActionBarTitle;
    protected TextView mActionBarSubTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        if (checkPlayServices()) {

            if (GCMUtils.getRegistrationId(HollerbackApplication.getInstance()).isEmpty()) {
                GCMUtils.registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HollerbackApplication.getInstance().getAppLifecycle().setActive();
        if (checkPlayServices()) {
            if (!PreferenceManagerUtil.getPreferenceValue(HBPreferences.IS_GCM_REGISTERED, true)) { // assume we're registered
                GCMUtils.notifyServer(GCMUtils.getRegistrationId(this));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HollerbackApplication.getInstance().getAppLifecycle().setInactive();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    protected void setupActionBar() {
        ActionBar supportActionBar = getSupportActionBar();

        supportActionBar.setIcon(R.drawable.banana_medium);
        supportActionBar.setHomeButtonEnabled(true);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowTitleEnabled(false);

        // set custom view for the title
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.header_title, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.START);

        mActionBarTitle = (TextView) customView.findViewById(R.id.title);
        mActionBarSubTitle = (TextView) customView.findViewById(R.id.sub_title);
        supportActionBar.setCustomView(customView, params);
        supportActionBar.setDisplayShowCustomEnabled(true);
        supportActionBar.show();

    }

    public TextView getCustomActionBarTitle() {
        return mActionBarTitle;
    }

    public void setCustomActionBarSubTitle(String subtitle) {
        if (subtitle == null || "".equals(subtitle)) {
            mActionBarSubTitle.setVisibility(View.GONE);
            mActionBarSubTitle.setText("");
        } else {
            mActionBarSubTitle.setVisibility(View.VISIBLE);
            mActionBarSubTitle.setText(subtitle);
        }
    }

    public TextView getCustomActionBarSubTitle() {
        return mActionBarSubTitle;
    }

}
