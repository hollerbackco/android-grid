package com.moziy.hollerback.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.service.PassiveUploadService;

/**
 * This class will get notified when a video upload or post fails
 * Two intents trigger this broadcast receiver, one is the connectivity change intent
 * and the other is the alarm manager's pending intent.
 * The broadcast will fire off a task to the service when there's connectivity only
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
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, timeInMillis);
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

    private boolean isConnected(ConnectivityManager cm) {
        return !(cm.getActiveNetworkInfo() == null) && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

            // if it's a connectivity change and we're connected, and there was a pending flag set
            if (!isConnected(cm)) {
                return;
            }

            if (!PreferenceManagerUtil.getPreferenceValue(HBPreferences.PENDING_RECOVERY_ALARM, false)) {
                return;
            }

            // alright, there was a pending alarm and we're connected, lets launch the service

        } else { // this is an alarm manager
            if (!isConnected(cm)) {

                // we're not connected, so let's just mark it as pending
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.PENDING_RECOVERY_ALARM, true);
                return;
            }

        }

        PreferenceManagerUtil.setPreferenceValue(HBPreferences.PENDING_RECOVERY_ALARM, false); // clear the flag

        Log.d(TAG, "launching resource recovery service");
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, PassiveUploadService.class);

        startWakefulService(context, serviceIntent);

        // schedule the next one
        schedule(PreferenceManagerUtil.getPreferenceValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, ONE_MINUTE) * 2);
    }
}
