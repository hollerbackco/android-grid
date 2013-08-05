package com.moziy.hollerback.util;

import java.util.ArrayList;

import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.UserModel;

public class HBRequestUtil {

	public static String generatePhoneNumberArrayString(
			ArrayList<UserModel> items) {
		String start = "[";
		String end = "]";

		int count = 0;

		for (UserModel item : items) {

			if (count > 0) {
				start += ",";
			}
			start += item.phone;
			count++;
		}

		String numbers = start + end;
		LogUtil.i(numbers);
		return numbers;
	}

	public static ArrayList<String> generateStringArray(
			ArrayList<UserModel> items) {

		ArrayList<String> numbers = new ArrayList<String>();

		for (UserModel item : items) {
			numbers.add(item.phone);
		}
		return numbers;
	}
}
