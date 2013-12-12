package com.moziy.hollerback;

import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;

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
        if (PreferenceManagerUtil.getPreferenceValue(HBPreferences.ACCESS_TOKEN, null) != null) {
            return true;
        }
        return false;
    }

    public static String getValidToken() {
        return PreferenceManagerUtil.getPreferenceValue(HBPreferences.ACCESS_TOKEN, null);
    }

    public static void logOut() {
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.ACCESS_TOKEN, null);
        // Delete other preferences
        // Delete databases
    }

}
