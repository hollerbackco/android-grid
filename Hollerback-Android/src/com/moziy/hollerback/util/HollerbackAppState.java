package com.moziy.hollerback.util;

import com.moziy.hollerback.model.UserModel;

public class HollerbackAppState {

    private static HollerbackAppState sInstance;

    public boolean loadedContacts;

    private HollerbackAppState() {
    }

    public static HollerbackAppState getInstance() {
        if (sInstance == null) {
            sInstance = new HollerbackAppState();
        }
        return sInstance;
    }

    public static UserModel isUserLoggedIn() {
        return null;
    }

    public static boolean isValidSession() {
        if (PreferenceManagerUtil.getPreferenceValue(HollerbackPreferences.ACCESS_TOKEN, null) != null) {
            return true;
        }
        return false;
    }

    public static String getValidToken() {
        return PreferenceManagerUtil.getPreferenceValue(HollerbackPreferences.ACCESS_TOKEN, null);
    }

    public static void logOut() {
        PreferenceManagerUtil.setPreferenceValue(HollerbackPreferences.ACCESS_TOKEN, null);
        // Delete other preferences
        // Delete databases
    }

}
