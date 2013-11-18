package com.moziy.hollerback.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * This class listens to connectivity changes and then attempts to resolve pending tasks
 * @author sajjad
 *
 */
public class ConnectivityReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = ConnectivityReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            Log.d(TAG, "network state change; new state: connected");
        } else {
            Log.d(TAG, "network state change; new state: " + (cm.getActiveNetworkInfo() != null ? cm.getActiveNetworkInfo().getState() : " unknown"));
        }
    }
}
