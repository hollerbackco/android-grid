package com.moziy.hollerback.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;

public class BaseActivity extends SherlockFragmentActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";

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

}
