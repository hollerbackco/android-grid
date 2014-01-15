package com.moziy.hollerback;

import java.util.concurrent.Semaphore;

import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.network.VolleySingleton;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class HollerbackAppState {

    public static Semaphore sSyncSemaphore = new Semaphore(1); // only a single client can run

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

    public static boolean isValidSession() {
        if (PreferenceManagerUtil.getPreferenceValue(HBPreferences.ACCESS_TOKEN, null) != null) {
            return true;
        }
        return false;
    }

    public static String getValidToken() {
        return PreferenceManagerUtil.getPreferenceValue(HBPreferences.ACCESS_TOKEN, null);
    }

    public static void logOut(Context ctx) {
        PreferenceManagerUtil.clearPreferences();
        ActiveAndroid.beginTransaction();
        new Delete().from(ConversationModel.class).execute();
        new Delete().from(VideoModel.class).execute();
        new Delete().from(UserModel.class).execute();
        new Delete().from(Friend.class).execute();
        VolleySingleton.getInstance(ctx).getRequestQueue().getCache().clear(); // clear everything
        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
    }

}
