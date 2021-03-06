package com.moziy.hollerback.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.service.SyncService;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = GCMBroadcastReceiver.class.getSimpleName();

    // type: boolean
    public static final String FROM_GCM_INTENT_ARG = "from_gcm";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "received gcm message");
        if (HollerbackAppState.isValidSession()) {
            Intent serviceIntent = new Intent(context, SyncService.class);
            serviceIntent.putExtra(FROM_GCM_INTENT_ARG, true);
            startWakefulService(context, serviceIntent);
        } else {
            Log.w(TAG, "invalid session");
        }

    }
}
