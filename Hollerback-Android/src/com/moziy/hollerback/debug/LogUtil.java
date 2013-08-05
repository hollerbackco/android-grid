package com.moziy.hollerback.debug;

import android.util.Log;

public class LogUtil {

	static boolean logsOn = true;

	static final String APP_NAME = "Hollerback";

	public static void i(String tag, String msg) {
		if (logsOn) {
			Log.i(tag, msg);
		}
	}

	public static void i(Class<?> object, String msg) {
		if (logsOn) {
			Log.i(object.getSimpleName(), msg);
		}
	}

	public static void i(String msg) {
		if (logsOn) {
			Log.i(APP_NAME, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (logsOn) {
			Log.d(tag, msg);
		}
	}

	public static void d(Class<?> object, String msg) {
		if (logsOn) {
			Log.d(object.getSimpleName(), msg);
		}
	}

	public static void d(String msg) {
		if (logsOn) {
			Log.d(APP_NAME, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (logsOn) {
			Log.w(tag, msg);
		}
	}

	public static void w(Class<?> object, String msg) {
		if (logsOn) {
			Log.w(object.getSimpleName(), msg);
		}
	}

	public static void w(String msg) {
		if (logsOn) {
			Log.w(APP_NAME, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (logsOn) {
			Log.e(tag, msg);
		}
	}

	public static void e(Class<?> object, String msg) {
		if (logsOn) {
			Log.e(object.getSimpleName(), msg);
		}
	}

	public static void e(String msg) {
		if (logsOn) {
			Log.e(APP_NAME, msg);
		}
	}

}
