package com.moziy.hollerback.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.provider.Settings.Secure;

import com.activeandroid.ActiveAndroid;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;

public class AppEnvironment {

    // NEED NOT RESET //

    public static final String APP_PREF = "HollerbackAppPrefs";

    public static final String DB_NAME = "hollerback.db";

    private static final String SDCARD_DIRECTORY_NAME = "Hollerback";

    public static final String HB_SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SDCARD_DIRECTORY_NAME;

    public static final String HB_DISK_CACHE_PATH = HB_SDCARD_PATH + "/" + "Cache";

    public static final String ANDROID_ID = Secure.getString(HollerbackApplication.getInstance().getContentResolver(), Secure.ANDROID_ID);

    public static final String APP_VERSION_CODE;

    public static final String APP_VERSION_NAME;

    static {
        PackageInfo pi = null;
        try {
            pi = HollerbackApplication.getInstance().getPackageManager().getPackageInfo(HollerbackApplication.getInstance().getPackageName(), 0);

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (pi != null) {
            APP_VERSION_CODE = String.valueOf((pi.versionCode));
            APP_VERSION_NAME = pi.versionName;
        } else {
            APP_VERSION_CODE = null;
            APP_VERSION_NAME = null;
        }

    }

    // Analytics
    public final boolean GA_IS_DRY_RUN;

    // DEV
    // http://lit-sea-1934.herokuapp.com/

    public final String BASE_URL;

    public static final String ACCESS_KEY_ID = "AKIAJX65IZWDWNJQVNIA";
    public static final String SECRET_KEY = "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";

    public static final String ASSETS_DIRECTORY = "file:///android_asset/";
    public static final String ANDROID_RESOURCE_PATH = "android.resource://" + HollerbackApplication.getInstance().getPackageName() + "/";
    public static final String NOTIF_SOUND_URI = ANDROID_RESOURCE_PATH + R.raw.default_notification;

    public static final int MEMORY_CACHE_SIZE = 30;
    public static final int DISK_CACHE_SIZE = 100;

    // public static final String PICTURE_NAME = null;

    // policy links
    public static final String PRIVACY_POLICY_URL = "http://www.hollerback.co/privacy";
    public static final String TERMS_OF_SERVICE_URL = "http://www.hollerback.co/terms";

    public static final String UPLOAD_BUCKET_DEV = "hb-tmp-dev";
    public static final String UPLOAD_BUCKET_PROD = "hb-tmp";
    public final String UPLOAD_BUCKET;

    public static final int ENV_PRODUCTION = 0x9999;
    public static final int ENV_DEVELOPMENT = 0x1234;

    public final int ENV = ENV_DEVELOPMENT; // ENV FLAG

    public final String IMAGE_THUMB_SUFFIX = "-thumb.png";

    public boolean FORCE_PHONE_NUMBER_CHECK;

    public static final String GOOGLE_PROJECT_NUMBER = "69406303235";

    public static boolean LOG_CRASHES;

    public static final String CRITTERCISM_ID = "51a94f4d1386206f31000002";

    public final String FLURRY_ID;

    private static AppEnvironment sInstance;

    public static AppEnvironment getInstance() {
        if (sInstance == null) {
            sInstance = new AppEnvironment();
        }
        return sInstance;
    }

    private AppEnvironment() {
        switch (ENV) {

            case ENV_PRODUCTION:
                // DBUtil.copyDbToSdcard();
                LogUtil.d("Setting Production Environment");
                BASE_URL = "https://calm-peak-4397.herokuapp.com";
                FORCE_PHONE_NUMBER_CHECK = true;
                LOG_CRASHES = true;
                FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
                UPLOAD_BUCKET = UPLOAD_BUCKET_PROD;
                GA_IS_DRY_RUN = false;
                break;
            case ENV_DEVELOPMENT:
            default:
                DBUtil.copyDbToSdcard();
                LogUtil.d("Setting Development Environment");
                BASE_URL = "http://lit-sea-1934.herokuapp.com";
                FORCE_PHONE_NUMBER_CHECK = true;
                LOG_CRASHES = true;
                FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
                UPLOAD_BUCKET = UPLOAD_BUCKET_DEV;
                GA_IS_DRY_RUN = true;
                ActiveAndroid.setLoggingEnabled(true); // only enable on dev mode
                break;
        }
    }

}
