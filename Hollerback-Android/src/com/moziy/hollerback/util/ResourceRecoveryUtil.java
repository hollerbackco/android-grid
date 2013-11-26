package com.moziy.hollerback.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.service.ResourceRecoveryService;

/**
 * This class will get notified when a video upload or post fails
 * @author sajjad
 *
 */
public class ResourceRecoveryUtil extends WakefulBroadcastReceiver {
    private static final String TAG = ResourceRecoveryUtil.class.getSimpleName();
    private static final long ONE_MINUTE = 60 * 1000;

    /**
     * This method will schedule a pending intent to be delivered
     * by the alarm manager to upload failed resources
     */
    public static void schedule() {

        schedule(ONE_MINUTE);

    }

    private static void schedule(long timeInMillis) {

        Log.d(TAG, "scheduling resource recovery in " + timeInMillis);
        PendingIntent pendingIntent = getPendingIntent();

        AlarmManager am = (AlarmManager) HollerbackApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent); // cancel any alarm with this pending intent
        am.set(AlarmManager.RTC, System.currentTimeMillis() + timeInMillis, pendingIntent);
        PreferenceManagerUtil.setPreferenceLongValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, timeInMillis);
    }

    /**
     * This method will unschedule the alarm
     */
    public static void cancel() {

        PendingIntent pendingIntent = getPendingIntent();
        AlarmManager am = (AlarmManager) HollerbackApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent); // cancel any alarm with this pending intent
    }

    private static PendingIntent getPendingIntent() {
        Intent intent = new Intent();
        intent.setClass(HollerbackApplication.getInstance(), ResourceRecoveryUtil.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(HollerbackApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "launching resource recovery service");
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, ResourceRecoveryService.class);

        startWakefulService(context, serviceIntent);

        // schedule the next one
        schedule(PreferenceManagerUtil.getPreferenceLongValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, ONE_MINUTE) * 2);
    }

}
