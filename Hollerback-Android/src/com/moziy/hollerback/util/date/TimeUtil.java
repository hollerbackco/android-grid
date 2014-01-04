package com.moziy.hollerback.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Build;
import android.util.Log;

public class TimeUtil {
    private static final String TAG = TimeUtil.class.getSimpleName();
    public static final SimpleDateFormat LOCAL_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZZ", Locale.US); // used for displaying in client
    private static final SimpleDateFormat SERVER_TIME_FORMAT; // used for comparing times
    public static final int ONE_MINUTE_MILLIS = 60 * 1000;

    static {
        SERVER_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZZ", Locale.US);
        SERVER_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }

    public static String FORMAT_ISO8601(Date d) {
        if (Build.VERSION.SDK_INT >= 18) { // supports ZZZZZZ
            return SERVER_TIME_FORMAT.format(d);
        } else {
            String dateString = SERVER_TIME_FORMAT.format(d);
            Log.d(TAG, "datestring: " + dateString);

            if (dateString.substring(22).indexOf(':', 0) == -1) { // couldn't find ':' in +00:00 timzone string
                dateString = dateString.substring(0, 22) + ":" + dateString.substring(22);
                Log.d(TAG, "modified datestring: " + dateString);
            }

            return dateString;
        }
    }

    public static Date PARSE(String formattedDate) {
        try {
            return SERVER_TIME_FORMAT.parse(formattedDate);
        } catch (ParseException e) {
            throw new IllegalStateException("fix me");
        }

    }
}
