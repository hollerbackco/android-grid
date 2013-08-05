package com.moziy.hollerback.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moziy.hollerback.background.ContactFetchAsyncTask;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class HollerbackAppHelper {

	Activity mActivity;

	public HollerbackAppHelper(Activity activity) {
		mActivity = activity;
	}

	public boolean loadContacts(Activity activity) {
		ContactFetchAsyncTask mTask = new ContactFetchAsyncTask(activity, null);
		mTask.execute();
		return true;
	}

	public boolean processContacts(SortedArray result) {
		HBRequestManager.getContacts(result.array);
		return true;
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

		}

	};

}
