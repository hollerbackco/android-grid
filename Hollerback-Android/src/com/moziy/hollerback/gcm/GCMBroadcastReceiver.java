package com.moziy.hollerback.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.moziy.hollerback.service.SyncService;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    // type: boolean
    public static final String FROM_GCM_INTENT_ARG = "from_gcm";

    @Override
    public void onReceive(Context context, Intent intent) {

        ComponentName component = new ComponentName(context, SyncService.class);
        intent.setComponent(component);
        intent.putExtra(FROM_GCM_INTENT_ARG, true);
        startWakefulService(context, intent);

    }
}
