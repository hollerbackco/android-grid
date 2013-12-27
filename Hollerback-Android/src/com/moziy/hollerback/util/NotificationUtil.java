package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.model.Sender;
import com.moziy.hollerback.model.VideoModel;

public class NotificationUtil {

    public interface Ids {
        public static final int SYNC_NOTIFICATION = 100;
    }

    public static Notification generateNotification(String title, String message) {
        Intent intent = new Intent();
        Context ctx = HollerbackApplication.getInstance();
        intent.setClass(ctx, HollerbackMainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        // builder.setDefaults(0);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setAutoCancel(true);
        builder.setLights(Color.argb(255, 255, 255, 0), 2000, 1000);
        builder.setVibrate(new long[] { // in millis
            300
        });

        builder.setSound(Uri.parse(AppEnvironment.NOTIF_SOUND_URI), AudioManager.STREAM_NOTIFICATION);
        builder.setOnlyAlertOnce(true);

        // TODO - sajjad: play around with the style and big content
        return builder.build();

    }

    public static void launchNotification(Context ctx, Notification notification, int id) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notification);

    }

    public static String generateNewVideoMessage(Context ctx, List<VideoModel> videos) {

        Map<String, String> uniqueUsersMap = new HashMap<String, String>();
        for (VideoModel v : videos) {
            uniqueUsersMap.put(v.getSenderName(), v.getSenderName());
        }

        List<String> userList = new ArrayList<String>(uniqueUsersMap.keySet());

        String format = ctx.getResources().getQuantityString(R.plurals.notif_new_message, userList.size());
        String message;
        switch (uniqueUsersMap.size()) {
            case 1:
                message = String.format(format, userList.get(0));
                break;
            default:
                message = format;
                break;
        }

        return message;
    }

    public static String generateNewVideoMessage(Context ctx, Sender sender) {
        String format = ctx.getResources().getQuantityString(R.plurals.notif_new_message, 1);
        String message = String.format(format, sender.getSenderName());

        return message;
    }
}
