package com.moziy.hollerback.util;

import java.util.HashMap;

import android.os.Build;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.moziy.hollerback.HollerbackApplication;

public class AnalyticsUtil {

    public interface Category {
        public static final String Registration = "Registration";
        public static final String UI = "UserInterface";
    }

    public interface Action {
        public static final String SubmitRegInfo = "SubmitRegInfo";
        public static final String SubmitVerification = "SubmitVerfication";
    }

    public interface UiAction extends Action {
        public static final String ButtonPress = "ButtonPress";

    }

    public interface Label {
        public static final String ConvoListPlus = "convo_list_plus_btn";
        public static final String ConvoListNewConvo = "convo_list_new_convo_btn";
        public static final String ConvoListAddFriends = "convo_list_add_friends_btn";
        public static final String ConvoListHistory = "convo_list_watch_history_btn";
        public static final String ConvoListWatchUnread = "convo_list_watch_unread_btn";

    }

    public static void log(String category, String action, String label, Long value) {
        EasyTracker.getInstance(HollerbackApplication.getInstance()).send(MapBuilder.createEvent(category, action, label, value).build());
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static HashMap<String, String> getMap(String... strings) {
        if (strings.length % 2 != 0) {
            return null;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < strings.length - 1; i += 2) {
            params.put(strings[i], strings[i + 1]);
        }

        return params;
    }

}
