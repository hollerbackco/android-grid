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
import android.support.v4.app.NotificationCompat;

import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.model.VideoModel;

public class NotificationUtil {

    public interface Ids {
        public static final int SYNC_NOTIFICATION = 100;
    }

    public static Notification generateNotification(Context ctx, String title, String message) {
        Intent intent = new Intent();
        intent.setClass(ctx, HollerbackMainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setDefaults(0);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setAutoCancel(true);
        builder.setLights(Color.argb(255, 255, 255, 0), 2000, 1000);
        builder.setVibrate(new long[] { // in millis
            300
        });
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
}