package com.moziy.hollerback.util;

import java.text.SimpleDateFormat;

import android.widget.EditText;

public class StringUtil {
    public static SimpleDateFormat CurrentSelectedDateDisplayFormat = new SimpleDateFormat("E MMM d");

	/**
	 * Checking whether string is empty or not
	 * @param arg
	 * @return whether string is empty
	 */
	public static boolean isEmptyOrNull(String arg)
	{
		if(	arg == null ||
			arg.equals(null) || 
			arg.trim().equalsIgnoreCase("") ||
			arg.trim().equalsIgnoreCase("null") ||
			arg.trim().length() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Checking whether textbox has a string
	 * @param textbox
	 * @return
	 */
	public static boolean isEmptyOrNull(EditText textbox)
	{
		String arg = textbox.getText().toString();
		return isEmptyOrNull(arg);
	}
	
	/**
	 * Returns empty string
	 * @return
	 */
	public static String empty ()
	{
		return "";
	}
}
