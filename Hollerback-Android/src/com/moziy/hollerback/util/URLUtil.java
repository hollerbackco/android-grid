package com.moziy.hollerback.util;

import com.moziy.hollerback.debug.LogUtil;

public class URLUtil {

	public static String stripAWSParams(String url) {
		if (url == null) {
			return null;
		}

		if (url.contains("amazonaws") && url.contains("thumb")) {

			String[] split = url.split("\\?");
			LogUtil.i("Stripping: " + url + " to: " + split[0]);
			return split[0];
		}
		return url;

	}

}
