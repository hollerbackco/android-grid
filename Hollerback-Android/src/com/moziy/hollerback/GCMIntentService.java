package com.moziy.hollerback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.moziy.hollerback.activity.HollerbackBaseActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.debug.LogUtil;

public class GCMIntentService extends GCMBaseIntentService {

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.moziy.hollerback.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {

		LogUtil.i(TAG, "GCM Received message");
		displayMessage(context, "Message Received");
		String message = null;
		String title = null;
		String url = null;

		if (intent != null) {

			// Check the bundle for the pay load body and title
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				displayMessage(context, "Message bundle: " + bundle);

				LogUtil.i(TAG, "Message bundle: " + bundle);
				message = bundle.getString("sender_name");

				LogUtil.i("GCM Message data: " + message);
			}
		}
		// If there was no body just use a standard message
		if (message == null) {
			message = "Hollerback";
		}

		generateNotification(context, title, message, url);
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		/*
		 * Called when the GCM servers tells that app that pending messages have
		 * been deleted because the device was idle.
		 */
		LogUtil.i(TAG, "Received deleted messages notification");
		String message = "Deleted messages";
		displayMessage(context, message);
		// notifies user
		generateNotification(context, "", message);
	}

	private Notification prepareNotification(Context context, String msg) {
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.icon, msg, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, HollerbackBaseActivity.class);
		// Set a unique data uri for each notification to make sure the activity
		// gets updated
		intent.setData(Uri.parse(msg));
		intent.putExtra(IABIntent.GCM_MESSAGE, msg);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		String title = context.getString(R.string.app_name);
		notification.setLatestEventInfo(context, title, msg, pendingIntent);

		return notification;
	}

	@Override
	protected void onError(Context context, String errorId) {
		Toast.makeText(context, errorId, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Intent intent = new Intent(IABIntent.INTENT_GCM_REGISTERED);
		intent.putExtra(IABIntent.PARAM_GCM_REGISTRATION_ID, regId);
		context.sendBroadcast(intent);
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		LogUtil.i("onUnregistered: " + regId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String title,
			String message) {
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if ((title == null) || (title.equals(""))) {
			title = context.getString(R.string.app_name);
		}
		Intent notificationIntent = new Intent(context,
				HollerbackBaseActivity.class);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(context)
				.setContentTitle(title)
				.setContentText(message)
				.setContentIntent(intent)
				.setSmallIcon(icon)
				.setWhen(when)
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(message))
				.build();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

	private static void generateNotification(Context context, String title,
			String message, String url) {
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if ((title == null) || (title.equals(""))) {
			title = context.getString(R.string.app_name);
		}

		Intent notificationIntent = null;
		if ((url == null) || (url.equals(""))) {
			// just bring up the app
			notificationIntent = new Intent(context,
					HollerbackBaseActivity.class);
		} else {
			// Launch the URL
			notificationIntent = new Intent(Intent.ACTION_VIEW);
			notificationIntent.setData(Uri.parse(url));
			notificationIntent.addCategory(Intent.CATEGORY_BROWSABLE);
		}

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(context)
				.setContentTitle(title)
				.setContentText(message)
				.setContentIntent(intent)
				.setSmallIcon(icon)
				.setWhen(when)
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(message))
				.build();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

}
