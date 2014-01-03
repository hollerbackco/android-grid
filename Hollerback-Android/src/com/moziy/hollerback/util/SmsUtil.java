package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.model.Contact;

public class SmsUtil {
    private static final String TAG = SmsUtil.class.getSimpleName();

    public static void invite(Context context, List<Contact> friends, String body, Uri extraUriStream, String type) {
        // build the uri
        StringBuilder sb = new StringBuilder();
        for (Contact c : friends) {

            for (String phone : c.mPhones)
                sb.append(phone).append(";");
        }

        PackageManager pm = HollerbackApplication.getInstance().getPackageManager();

        // HTC SENSE SPECIFIC
        Intent htcMsgIntent = new Intent("android.intent.action.SEND_MSG");
        htcMsgIntent.putExtra("sms_body", body);
        htcMsgIntent.putExtra("address", sb.toString());
        if (extraUriStream != null && type != null) {
            htcMsgIntent.putExtra(Intent.EXTRA_STREAM, extraUriStream);
            htcMsgIntent.setType(type);
        }
        List<ResolveInfo> resolves = pm.queryIntentActivities(htcMsgIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolves.size() > 0) {
            // This branch is followed only for HTC
            context.startActivity(htcMsgIntent);
            return;
        }

        // GENERAL DEVICES
        Intent resolveSmsIntent = new Intent(Intent.ACTION_SENDTO);
        resolveSmsIntent.setData(Uri.parse("mmsto:"));

        // lets resolve all sms apps

        List<ResolveInfo> mmsResolveInfo = pm.queryIntentActivities(resolveSmsIntent, 0);

        List<Intent> resolvingIntents = new ArrayList<Intent>();

        if (!mmsResolveInfo.isEmpty()) {
            for (ResolveInfo ri : mmsResolveInfo) {

                Log.d(TAG, "whitelisted: " + ri.activityInfo.packageName + " targetActivity: " + ri.activityInfo.targetActivity);

                Intent targetIntent = new Intent();
                if (extraUriStream != null && type != null) {

                    targetIntent.setAction(Intent.ACTION_SEND);
                    targetIntent.putExtra("sms_body", body);
                    targetIntent.putExtra("address", sb.toString());
                    targetIntent.putExtra(Intent.EXTRA_STREAM, extraUriStream);
                    targetIntent.setType(type);
                } else {
                    targetIntent.setAction(Intent.ACTION_SEND);
                    // targetIntent.setData(Uri.parse("smsto:///" + sb.toString()));
                    targetIntent.putExtra("sms_body", body);
                    targetIntent.putExtra("address", sb.toString());
                    // targetIntent.putExtra(Intent.EXTRA_TEXT, body);
                    // targetIntent.setType("text/plain");
                    // targetIntent.setData(Uri.parse("smsto:"));
                    // targetIntent.setType("vnd.android-dir/mms-sms");

                    targetIntent.putExtra(Intent.EXTRA_TEXT, body);
                    targetIntent.setType("text/plain");
                    Log.d(TAG, "body: " + body + " addr: " + sb.toString());

                }

                targetIntent.setPackage(ri.activityInfo.packageName);

                resolvingIntents.add(targetIntent);
            }

            Intent chooserIntent = Intent.createChooser(resolvingIntents.remove(0), HollerbackApplication.getInstance().getString(R.string.invite_activity_chooser));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, resolvingIntents.toArray(new Parcelable[] {}));
            context.startActivity(chooserIntent);
        }

    }
}
