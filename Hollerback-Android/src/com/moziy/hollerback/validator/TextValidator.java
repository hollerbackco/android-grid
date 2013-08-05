package com.moziy.hollerback.validator;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.QU;

public class TextValidator {

	public static final int TYPE_EMAIL = 3;
	public static final int TYPE_NAME = 2;
	public static final int TYPE_PHONE = 4;
	public static final int TYPE_PASSWORD = 1;

	public static String isValid(String string, int type) {
		switch (type) {
		case TYPE_EMAIL:
			return isValidEmailAddress(string);
		case TYPE_NAME:
			return isValidName(string);
		default:
			return null;
		}
	}

	public static String isValidEmailAddress(String email) {

		EmailValidator validator = new EmailValidator();
		if (validator.validate(email)) {
			return null;
		} else {
			return QU.s(R.string.error_email);
		}

	}

	public static String isValidName(String name) {
		if (name == null || name.isEmpty() || name.trim().length() == 0) {
			return QU.s(R.string.error_name_null);
		}
		return null;
	}

	public static String isValidPhone(PhoneNumber phone) {
		PhoneNumberUtil util = PhoneNumberUtil.getInstance();
		if (phone != null
				&& AppEnvironment.getInstance().FORCE_PHONE_NUMBER_CHECK ? util
				.isValidNumber(phone) : true) {
			return null;
		}
		return QU.s(R.string.error_phone);
	}

	public static String isValidPassword(String password) {
		if (password != null && password.length() > 5) {
			return null;
		}
		return QU.s(R.string.error_password);
	}

}
