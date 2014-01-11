package com.moziy.hollerback.util.recovery;

import java.util.HashSet;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

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
    private static final long INITIAL_TIME = 30 * 1000;
    private static final long MAX_BACKOFF_TIME = 30 * 60 * 1000; // 30 minutes
    public static final String RECOVERY_PREF_FORMAT = "%s_PENDING_RECOVERY";

    public interface RecoveryClient {
        public String getFullyQualifiedClassName();
    }

    public static void init() {
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.RECOVERY_ALARM_TIME, Long.MAX_VALUE);
        schedule(INITIAL_TIME);
    }

    public static synchronized void requestRecovery(RecoveryClient client) {

        if (!PreferenceManagerUtil.getPreferenceValue(String.format(RECOVERY_PREF_FORMAT, client.getFullyQualifiedClassName()), false)) { // not in recovery then, lets register and kick off
            Log.d(TAG, "initiating recovery");
            register(client); // register the client

        }

        if ((PreferenceManagerUtil.getPreferenceValue(HBPreferences.RECOVERY_ALARM_TIME, Long.MAX_VALUE) - System.currentTimeMillis()) > INITIAL_TIME) // if it's less than a minute
            schedule(INITIAL_TIME);

    }

    public static synchronized void removeRecoveryRequest(RecoveryClient client) {
        Log.d(TAG, "unregister " + client.getFullyQualifiedClassName());
        unregister(client);
    }

    private static void schedule(long timeInMillis) {

        Log.d(TAG, "scheduling resource recovery in " + timeInMillis);
        PendingIntent pendingIntent = getPendingIntent();

        AlarmManager am = (AlarmManager) HollerbackApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent); // cancel any alarm with this pending intent
        long now = System.currentTimeMillis();
        am.set(AlarmManager.RTC, now + timeInMillis, pendingIntent);
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, timeInMillis);
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.RECOVERY_ALARM_TIME, now + timeInMillis);
    }

    /**
     * For now, the client must be a class that extends Service!
     * @param client
     */
    private static void register(RecoveryClient client) {

        Set<String> clients = new HashSet<String>(PreferenceManagerUtil.getPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, new HashSet<String>()));
        clients.add(client.getFullyQualifiedClassName());
        PreferenceManagerUtil.setPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, clients); // register the clients
        PreferenceManagerUtil.setPreferenceValue(String.format(RECOVERY_PREF_FORMAT, client.getFullyQualifiedClassName()), true);

        Log.d(TAG, "registering: " + client.getFullyQualifiedClassName());
        Log.d(TAG, "clients: " + clients.toString());

        // debug
        Set<String> clientel = PreferenceManagerUtil.getPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, new HashSet<String>());
        StringBuilder sb = new StringBuilder();
        for (String c : clientel) {
            sb.append(c).append(" ");
        }

        Log.d(TAG, "registered clientel: " + sb.toString());
    }

    private static void unregister(RecoveryClient client) {
        Set<String> clients = new HashSet<String>(PreferenceManagerUtil.getPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, new HashSet<String>()));
        clients.remove(client.getFullyQualifiedClassName());
        PreferenceManagerUtil.setPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, clients); // unregister the client
        PreferenceManagerUtil.setPreferenceValue(String.format(RECOVERY_PREF_FORMAT, client.getFullyQualifiedClassName()), false); // clear the flag

        Log.d(TAG, "unregistering: " + client.getFullyQualifiedClassName());
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

                Log.d(TAG, "no connectivity, setting pending alarm on connectivity");
                // we're not connected, so let's just mark it as pending
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.PENDING_RECOVERY_ALARM, true);
                return;
            }

        }

        initiateRecovery(context);
    }

    private void initiateRecovery(Context context) {

        PreferenceManagerUtil.setPreferenceValue(HBPreferences.PENDING_RECOVERY_ALARM, false); // clear the flag

        boolean recoveryInitiated = false;

        // get the set of all classes
        Set<String> clients = PreferenceManagerUtil.getPreferenceValueSet(HBPreferences.RECOVERY_CLIENTS, new HashSet<String>());

        for (final String clientClassName : clients) {

            final boolean startRecoveryForClient = PreferenceManagerUtil.getPreferenceValue(String.format(RECOVERY_PREF_FORMAT, clientClassName), false);
            if (startRecoveryForClient) {

                // PreferenceManagerUtil.setPreferenceValue(String.format(RECOVERY_PREF_FORMAT, clientClassName), false); // clear the recovery flag

                try {

                    Intent serviceIntent = new Intent();
                    serviceIntent.setClass(context, Class.forName(clientClassName));
                    startWakefulService(context, serviceIntent); // begin recovery for this client

                    Log.d(TAG, "launching recovery for: " + clientClassName);

                } catch (ClassNotFoundException e) {

                    e.printStackTrace();
                    throw new IllegalStateException("invalid class name");
                }
            }

            recoveryInitiated |= startRecoveryForClient;

        }

        if (recoveryInitiated) {
            Log.d(TAG, "reschedule recovery ");
            long recoveryTime = Math.min(PreferenceManagerUtil.getPreferenceValue(HBPreferences.RESOURCE_RECOVERY_BACKOFF_TIME, INITIAL_TIME) * 2, MAX_BACKOFF_TIME);

            // schedule the next one only if needed
            schedule(recoveryTime);

        } else {
            PreferenceManagerUtil.setPreferenceValue(HBPreferences.RECOVERY_ALARM_TIME, Long.MAX_VALUE);
            Log.d(TAG, "no recovery needed");
        }

    }

    /**
     * 
     * @param client the client wishing to know if it is in recovery mode
     * @return whether the client is in recovery mode or not
     */
    public boolean isInRecovery(RecoveryClient client) {
        return PreferenceManagerUtil.getPreferenceValue(String.format(RECOVERY_PREF_FORMAT, client.getFullyQualifiedClassName()), false);
    }

}
