package com.moziy.hollerback.communication;

import com.moziy.hollerback.HollerbackApplication;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class IABroadcastManager {

	public static void sendLocalBroadcast(Intent intent) {
		LocalBroadcastManager.getInstance(HollerbackApplication.getInstance())
				.sendBroadcast(intent);
	}

	public static void registerForLocalBroadcast(BroadcastReceiver receiver,
			String intent) {
		LocalBroadcastManager.getInstance(HollerbackApplication.getInstance())
				.registerReceiver(receiver, new IntentFilter(intent));
	}

	public static void unregisterLocalReceiver(BroadcastReceiver receiver) {
		LocalBroadcastManager.getInstance(HollerbackApplication.getInstance())
				.unregisterReceiver(receiver);
	}
	
	
}
