package com.moziy.hollerback.util.validators;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.QU;

public class ValidatorUtil {

    public final static boolean isValidEmail(CharSequence target, String[]... outMessage) {
        boolean status;
        if (target == null) {

            status = false;

        } else {

            status = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }

        if (!status && outMessage != null && outMessage.length > 0) {
            outMessage[0][0] = QU.s(R.string.error_email);
        }

        return status;
    }

    public static boolean isValidPhone(PhoneNumber phone, String[]... outMessage) {
        boolean status;
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();

        if (phone != null && AppEnvironment.getInstance().FORCE_PHONE_NUMBER_CHECK ? util.isValidNumber(phone) : true) {
            status = true;
        } else {
            status = false;
        }

        if (!status && outMessage != null && outMessage.length > 0) {
            outMessage[0][0] = QU.s(R.string.error_phone);
        }

        return status;
    }

    public static boolean isValidPassword(String password, String[]... outMessage) {

        boolean status;

        if (password != null && password.length() > 5) {
            status = true;
        } else {
            status = false;
        }

        if (!status && outMessage != null && outMessage.length > 0) {
            outMessage[0][0] = QU.s(R.string.password);
        }

        return status;
    }

    public static boolean isValidName(String name, String[]... outMessage) {
        boolean status;

        if (name == null || name.isEmpty() || name.trim().length() == 0) {
            status = false;
        } else {
            status = true;
        }

        if (!status && outMessage != null && outMessage.length > 0) {
            outMessage[0][0] = QU.s(R.string.error_name_null);
        }
        return true;
    }

}
