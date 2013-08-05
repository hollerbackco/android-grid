package com.moziy.hollerback.util;

import com.moziy.hollerback.debug.LogUtil;

public class AppEnvironment {

	// NEED NOT RESET //

	public static final String APP_PREF = "HollerbackAppPrefs";

	// DEV
	// http://lit-sea-1934.herokuapp.com/

	public String BASE_URL;

	// NEED NOT RESET //

	public String ACCESS_KEY_ID = "AKIAJX65IZWDWNJQVNIA";
	public String SECRET_KEY = "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";

	// public static final String PICTURE_NAME = null;

	public String UPLOAD_BUCKET = "hollerback-app-dev";

	public String PICTURE_BUCKET = "hollerback-app-dev";

	public static final int ENV_PRODUCTION = 0x9999;
	public static final int ENV_DEVELOPMENT = 0x1234;

	private int ENV = ENV_PRODUCTION;

	public final String IMAGE_THUMB_SUFFIX = "-thumb.png";

	public boolean ALLOW_UPLOAD_VIDEOS = true;
	public boolean FORCE_PHONE_NUMBER_CHECK;

	public static String GOOGLE_PROJECT_NUMBER;

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
			LogUtil.d("Setting Development Environment");
			BASE_URL = "http://lit-sea-1934.herokuapp.com";
			FORCE_PHONE_NUMBER_CHECK = false;
			GOOGLE_PROJECT_NUMBER = "69406303235";
			LOG_CRASHES = false;
			FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
			break;
		case ENV_PRODUCTION:
			LogUtil.d("Setting Production Environment");
			BASE_URL = "https://calm-peak-4397.herokuapp.com";
			FORCE_PHONE_NUMBER_CHECK = true;
			GOOGLE_PROJECT_NUMBER = "69406303235";
			LOG_CRASHES = true;
			FLURRY_ID = "FWC2TWGDJDYV7YR5SC8P";
			break;
		}
	}

}
