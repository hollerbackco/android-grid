package com.moziy.hollerback.util;

import java.util.HashMap;

import android.os.Build;

public class AnalyticsUtil {
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static HashMap<String, String> getMap(String... strings) {
		if (strings.length % 2 != 0) {
			return null;
		}

		HashMap<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < strings.length - 1; i += 2) {
			params.put(strings[i], strings[i + 1]);
		}

		return params;
	}

}
