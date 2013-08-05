package com.moziy.hollerback.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONObject;

import android.widget.EditText;

public class ConversionUtil {
	public static final float MLInCup = 240; // was 236.588237, but ios is using 240
	public static final long millisecondsInADay = 1000 * 60 * 60 * 24;
	
	public static String toString(Throwable value) {
		if (value == null) return "null error";
		
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		value.printStackTrace(printWriter);
		return result.toString();
	}

	public static String toString(Date value) {
		if (value == null)
			return "";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		return sdf.format(value);
	}

	public static String toString(int num) {
		return Integer.toString(num);
	}

	public static String toString(Double num) {
		if (Double.isNaN(num) || Double.isInfinite(num))
			return "0";

		return Double.toString(num);
	}
	
	public static String toString(JSONObject j, String key, String defaultValue) {
		String jsonValue = j.optString(key, defaultValue);
    	if (jsonValue == null || (jsonValue != null && jsonValue == "null")) jsonValue = "";
		return jsonValue;
	}	
	
	public static String join(String[] values, String joinValue) {
		return join(values, joinValue, false);
	}
	
	public static String join(String[] values, String joinValue, Boolean includeNulls) {
		if (values == null) return "";
		if (joinValue == null) joinValue = "";
		
		StringBuffer buff = new StringBuffer();
		
		for(int i=0;i<values.length;i++) {
			if (!includeNulls && values[i] == null) continue;
			
			if (values[i] != null) {
				buff.append(values[i]);
			}
			
			if (i < values.length-1){
				buff.append(joinValue);
			}
		}
		
		return buff.toString();
	}

	public static String toMillileter(int cups) {
		return toString((float)cups * MLInCup);
	}
	
	public static int fromMillileters(float ml) {
		return (int)(ml / MLInCup);
	}
	
	public static String toString(float value) {
		return Float.toString(value);
	}
	
	public static Integer parseInt(EditText value) {
		return parseInt(value.getText());
	}

	public static Integer parseInt(Object value) {
		if (value == null)
			return 0;

		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException nex) {
			return 0;
		}
	}

	public static Double parseDouble(EditText value) {
		return parseDouble(value.getText());
	}

	public static Double parseDouble(Object value) {
		if (value == null)
			return (double) 0;

		String svalue = value.toString();

		if (svalue == null || svalue == "")
			return (double) 0;

		try {
			return Double.parseDouble(svalue);
		} catch (NumberFormatException nex) {
			return (double) 0;
		}
	}

	public static Date parseDate(Object value) {
		if (value == null)
			return null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		try {
			return sdf.parse(value.toString());
		} catch (ParseException pex) {
			return null;
		}
	}
	
	public static Date parseLongDate(String value) {
		
		if (value == null || value.trim() == "") return null;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			return df.parse(value);
		} catch (ParseException e ) { 
			return null; 
		}
	}
	
	public static long milliUntil(int targetHourToday) {
		Calendar nowCal = Calendar.getInstance();
		
		return milliUntil(targetHourToday, nowCal);
	}

	public static long milliUntil(int targetHourToday, Calendar nowCal) {
		
		GregorianCalendar targetCal = new GregorianCalendar(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH), nowCal.get(Calendar.DAY_OF_MONTH), targetHourToday, 0);
		
		long nowMilli = nowCal.getTimeInMillis();
		long targetMilli = targetCal.getTimeInMillis();
		
		long diff = targetMilli - nowMilli;
		
		if (diff < 0) {
			diff = millisecondsInADay + diff;
		}
		
		return diff;
	}
	
	public static String timeAgo(Date sinceDate) {
		if (sinceDate == null) return "";
		
		Calendar sinceCal = Calendar.getInstance();
		sinceCal.setTime(sinceDate);
		Calendar nowCal = Calendar.getInstance();
		
		return timeAgo(sinceCal, nowCal);
	}

	public static String timeAgo(Calendar from, Calendar to) {
		if (from == null || to == null) return "";
		
		long nowMilli = to.getTimeInMillis();
		long sinceMilli = from.getTimeInMillis();
		
		if (nowMilli < sinceMilli) {
			long tmp = nowMilli;
			nowMilli = sinceMilli;
			sinceMilli = tmp;
		}
		
		long diff = nowMilli - sinceMilli;
		long diffSeconds = diff / 1000;
		long diffMinutes = diff / (60 * 1000);
		long diffHours = diff / (60 * 60 * 1000);
		long diffDays = diff / (24 * 60 * 60 * 1000);
		long diffMonths = diff / 1000 / 60 / 60 / 24 / 31; //yes, this is an approximation
		long diffYears = diff / 1000 / 60 / 60 / 24 / 31 / 12;
		
		if (diffYears == 1) {
			return "last year ";
		}
		else if (diffYears > 1) {
			return String.format("%1$s years ago ", diffYears);
		}
		else if (diffMonths == 1) {
			return "last month ";
		}
		else if (diffMonths > 1) {
			return String.format("%1$s months ago ", diffMonths);
		}
		else if (diffDays == 1) {
			return "yesterday ";
		}
		else if (diffDays > 1) {
			return String.format("%1$s days ago ", diffDays);
		}
		else if (diffHours == 1) {
			return "1 hour ago ";
		}
		else if (diffHours > 1) {
			return String.format("%1$s hours ago ", diffHours);
		}
		else if (diffMinutes == 1) {
			return "1 minute ago ";
		}
		else if (diffMinutes > 1) {
			return String.format("%1$s minutes ago ", diffMinutes);
		}
		else if (diffSeconds == 1) {
			return "1 second ago ";
		}
		else if (diffSeconds > 1) {
			return String.format("%1$s seconds ago ", diffSeconds);
		}
		else {
			return "just now ";
		}
	}
	
	public static int calculateAge(long dobstamp) {
		return calculateAge(new Date(dobstamp));
	}
	
	public static int calculateAge(Date dob) {
		if (dob == null) return 0;
		
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(dob);
		
		return calculateAge(cal);
	}
	
	public static int calculateAge(Calendar dob) {
		return calculateAge(dob, Calendar.getInstance());
	}
	
	public static int calculateAge(Calendar dob, Calendar today) {
		if (dob == null) return 0;
		if (today == null) today = Calendar.getInstance();
		
		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);  
		if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
		  age--;  
		} else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
		    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
		  age--;  
		}
		return age;
	}
	
	private final static String NON_THIN = "[^iIl1\\.,']";

	private static int textWidth(String str) {
	    return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public static String ellipsize(String text, int max) {
		return ellipsize(text, max, "?");
	}
	
	/**
	 * Helper method found from SO thread:
	 * http://stackoverflow.com/a/3657496/5416
	 */
	public static String ellipsize(String text, int max, String postFix) {

	    if (textWidth(text) <= max)
	        return text;

	    // Start by chopping off at the word before max
	    // This is an over-approximation due to thin-characters...
	    int end = text.lastIndexOf(' ', max - 3);

	    // Just one long word. Chop it off.
	    if (end == -1)
	        return text.substring(0, max-3) + postFix;

	    // Step forward as long as textWidth allows.
	    int newEnd = end;
	    do {
	        end = newEnd;
	        newEnd = text.indexOf(' ', end + 1);

	        // No more spaces.
	        if (newEnd == -1)
	            newEnd = text.length();

	    } while (textWidth(text.substring(0, newEnd) + postFix) < max);

	    return text.substring(0, end) + postFix;
	}

}
