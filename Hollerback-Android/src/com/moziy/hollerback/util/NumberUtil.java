package com.moziy.hollerback.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class NumberUtil {

	public static String getE164Number(String number) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		if (number != null && !number.isEmpty()) {
			try {
				PhoneNumber phoneNumber;
				phoneNumber = phoneUtil.parse(number, "US");
				return phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
			} catch (NumberParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	public static PhoneNumber getPhoneNumber(String number) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		if (number != null && !number.isEmpty()) {
			try {
				return phoneUtil.parse(number, "US");
			} catch (NumberParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
