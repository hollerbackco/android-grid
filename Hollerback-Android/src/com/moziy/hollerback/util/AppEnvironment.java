package com.moziy.hollerback.util;

import android.os.Environment;
import android.provider.Settings.Secure;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;

public class AppEnvironment {

    // NEED NOT RESET //

    public static final String APP_PREF = "HollerbackAppPrefs";

    public static final String DB_NAME = "hollerback.db";

    private static String SDCARD_DIRECTORY_NAME = "Hollerback";

    public static final String HB_SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SDCARD_DIRECTORY_NAME;

    public static final String ANDROID_ID = Secure.getString(HollerbackApplication.getInstance().getContentResolver(), Secure.ANDROID_ID);

    // DEV
    // http://lit-sea-1934.herokuapp.com/

    public String BASE_URL;

    // NEED NOT RESET //

    public String ACCESS_KEY_ID = "AKIAJX65IZWDWNJQVNIA";
    public String SECRET_KEY = "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";

    public static final String ASSETS_DIRECTORY = "file:///android_asset/";
    public static final String ANDROID_RESOURCE_PATH = "android.resource://" + HollerbackApplication.getInstance().getPackageName() + "/";
    public static final String NOTIF_SOUND_URI = ANDROID_RESOURCE_PATH + R.raw.default_notification;

    // public static final String PICTURE_NAME = null;

    // policy links
    public static final String PRIVACY_POLICY_URL = "http://www.hollerback.co/privacy";
    public static final String TERMS_OF_SERVICE_URL = "http://www.hollerback.co/terms";

    public String UPLOAD_BUCKET;
    public static final String UPLOAD_BUCKET_DEV = "hb-tmp-dev";
    public static final String UPLOAD_BUCKET_PROD = "hb-tmp";
    public String PICTURE_BUCKET;

    public static final int ENV_PRODUCTION = 0x9999;
    public static final int ENV_DEVELOPMENT = 0x1234;

    public final int ENV = ENV_DEVELOPMENT;

    public final String IMAGE_THUMB_SUFFIX = "-thumb.png";

    public boolean ALLOW_UPLOAD_VIDEOS = true;
    public boolean FORCE_PHONE_NUMBER_CHECK;

    public static final String GOOGLE_PROJECT_NUMBER = "69406303235";

    public static AppEnvironment sInstance;

    public static boolean LOG_CRASHES;

    public static final String CRITTERCISM_ID = "51a94f4d1386206f31000002";

    public static String FLURRY_ID;

    public static AppEnvironment getInstance() {
        if (sInstance == null) {
            sInstance = new AppEnvironment();
            sInstance.setEnvironment();
        }
        return sInstance;
    }

    // TODO: Setup Environments
    public void setEnvironment() {
        switch (ENV) {
            case ENV_DEVELOPMENT:
                DBUtil.copyDbToSdcard();
                LogUtil.d("Setting Development Environment");
                BASE_URL = "http://lit-sea-1934.herokuapp.com";
                FORCE_PHONE_NUMBER_CHECK = true;
                LOG_CRASHES = false;
                FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
                UPLOAD_BUCKET = UPLOAD_BUCKET_DEV;
                break;
            case ENV_PRODUCTION:
                // DBUtil.copyDbToSdcard();
                LogUtil.d("Setting Production Environment");
                BASE_URL = "https://calm-peak-4397.herokuapp.com";
                FORCE_PHONE_NUMBER_CHECK = true;
                LOG_CRASHES = true;
                FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
                UPLOAD_BUCKET = UPLOAD_BUCKET_PROD;
                break;
        }
    }

}
