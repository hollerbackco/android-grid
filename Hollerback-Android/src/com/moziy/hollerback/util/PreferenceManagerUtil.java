package com.moziy.hollerback.util;

import com.moziy.hollerback.HollerbackApplication;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManagerUtil {

	private static SharedPreferences mPreferences;

	private static void initPreferences() {

		if (mPreferences == null) {
			mPreferences = HollerbackApplication.getInstance()
					.getSharedPreferences(AppEnvironment.APP_PREF,
							Context.MODE_PRIVATE);
		}
	}

	/**
	 * @return Instance of SharedPreferences.Editor from SharedPreferences.
	 */
	private static SharedPreferences.Editor getEditor() {
		initPreferences();
		return (mPreferences != null) ? mPreferences.edit() : null;

	}

	private static void commitPreferences(SharedPreferences.Editor editor) {

		if (editor != null) {

			editor.commit();
			editor = null;

		}
	}

	// all float, long, String, integer, boolean setter methods
	public void setPreferenceLongValue(String key, long value) {

		SharedPreferences.Editor editor = getEditor();

		editor.putLong(key, value);

		commitPreferences(editor);

	}

	public static void setPreferenceValue(String key, boolean value) {

		SharedPreferences.Editor editor = getEditor();

		editor.putBoolean(key, value);

		commitPreferences(editor);

	}

	public static void setPreferenceValue(String key, float value) {

		SharedPreferences.Editor editor = getEditor();

		editor.putFloat(key, value);

		commitPreferences(editor);

	}

	public static void setPreferenceValue(String key, int value) {

		SharedPreferences.Editor editor = getEditor();

		editor.putInt(key, value);

		commitPreferences(editor);

	}

	public static void setPreferenceValue(String key, String value) {

		SharedPreferences.Editor editor = getEditor();

		editor.putString(key, value);

		commitPreferences(editor);

	}

	// all long, int, flaot, String , boolean getter methods
	public static long getPreferenceLongValue(String key, long defValue) {
		initPreferences();
		return mPreferences.getLong(key, defValue);
	}

	public static boolean getPreferenceValue(String key, boolean defValue) {
		initPreferences();
		return mPreferences.getBoolean(key, defValue);
	}

	public static float getPreferenceValue(String key, float defValue) {
		initPreferences();
		return mPreferences.getFloat(key, defValue);
	}

	public static int getPreferenceValue(String key, int defValue) {
		initPreferences();
		return mPreferences.getInt(key, defValue);
	}

	public static String getPreferenceValue(String key, String defValue) {
		initPreferences();
		return mPreferences.getString(key, defValue);
	}

	public static void clearPreferences() {
		initPreferences();
		mPreferences.edit().clear();
	}

	public static void removeSelectedPreference(String strValue) {
		if (getPreferenceValue(strValue, "") != null && strValue.length() != 0) {
			SharedPreferences.Editor editor = getEditor();
			commitPreferences(editor.remove(strValue));

		}

	}

	public static void removeSelectedBooleanPreference(String strValue) {
		SharedPreferences.Editor editor = getEditor();
		commitPreferences(editor.remove(strValue));

	}
}
