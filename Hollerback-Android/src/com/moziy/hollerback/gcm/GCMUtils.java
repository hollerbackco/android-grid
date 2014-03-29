package com.moziy.hollerback.gcm;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class GCMUtils {
    private static final String TAG = GCMUtils.class.getSimpleName();

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context mContext) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mContext.getSharedPreferences(GCMUtils.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public static void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String regid = null;
                Context context = HollerbackApplication.getInstance();
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(AppEnvironment.GOOGLE_PROJECT_NUMBER);
                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);

                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return regid;
            }

            protected void onPostExecute(String regId) {
                notifyServer(regId);
            };

        }.execute(null, null, null);

    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public static void notifyServer(String regId) {
        if (regId != null && !regId.isEmpty() && HollerbackAppState.isValidSession()) {
            HBRequestManager.postMe(HollerbackAppState.getValidToken(), regId, new HBAsyncHttpResponseHandler<Envelope<Object>>(new TypeReference<Envelope<Object>>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<Object> response) {
                    if (statusCode == 200) {
                        PreferenceManagerUtil.setPreferenceValue(HBPreferences.IS_GCM_REGISTERED, true);
                    }

                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.IS_GCM_REGISTERED, false);
                }
            });
        }
    }
}
