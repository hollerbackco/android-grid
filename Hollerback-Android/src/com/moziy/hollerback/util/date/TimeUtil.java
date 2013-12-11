package com.moziy.hollerback.util.date;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    public static final SimpleDateFormat LOCAL_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US); // used for displaying in client
    public static final SimpleDateFormat SERVER_TIME_FORMAT; // used for comparing times
    public static final int ONE_MINUTE_MILLIS = 60 * 1000;

    static {
        SERVER_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
        SERVER_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }
}
