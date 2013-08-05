package com.moziy.hollerback.util;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.moziy.hollerback.communication.IABIntent;

public class HashUtil {

	/**
	 * 
	 * @param value
	 * @return 128bit value at 4 bytes per char (length 32)
	 */
	public static String getMD5(String value) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(value.getBytes(), 0, value.length());
			String encoding = new BigInteger(1, m.digest()).toString(16);
			return encoding;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * On average SHA256 is about 40 times faster than using MD5
	 * 
	 * @param value
	 * @return 256bit value at 4 bytes per char (length 64)
	 */
	public static String getSHA256(String value) {
		try {
			MessageDigest m = MessageDigest.getInstance("SHA-256");
			m.update(value.getBytes(), 0, value.length());
			String encoding = new BigInteger(1, m.digest()).toString(16);
			return encoding;
		} catch (Exception e) {
			return null;
		}
	}

	public static String getDefaultHash(String value) {
		return getSHA256(value);
	}

	public static String generateHashFor(String s, String s1) {
		String hash = Integer.toString(s.hashCode())
				+ Integer.toString(s1.hashCode());
		HashUtil.getSHA256(hash);

		return hash;
	}

	public static String getConvHash() {
		return HashUtil.generateHashFor(IABIntent.INTENT_GET_CONVERSATIONS,
				IABIntent.VALUE_CONV_HASH);
	}
}
