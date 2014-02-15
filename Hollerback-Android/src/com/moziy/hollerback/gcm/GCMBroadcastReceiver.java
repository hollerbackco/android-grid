package com.moziy.hollerback.gcm;

import java.io.IOException;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.service.SyncService;
import com.moziy.hollerback.util.NotificationUtil;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final int USER_JOINED_NOTIF_ID = 100;
    private static final String TAG = GCMBroadcastReceiver.class.getSimpleName();
    public static final String TYPE_GCM_INTENT_ARG_KEY = "type";
    public static final String PAYLOAD_GCM_INTENT_ARG_KEY = "payload";

    public interface Type {
        public static final String SYNC = "sync";
        public static final String NOTIFICATION = "notification";
    }

    // type: boolean
    public static final String FROM_GCM_INTENT_ARG = "from_gcm";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "received gcm message");
        if (HollerbackAppState.isValidSession()) {

            if (intent.hasExtra(TYPE_GCM_INTENT_ARG_KEY)) {
                Log.d(TAG, intent.getStringExtra("type"));
                if (intent.getStringExtra(TYPE_GCM_INTENT_ARG_KEY).equals(Type.SYNC)) {

                    Intent serviceIntent = new Intent(context, SyncService.class);
                    serviceIntent.putExtra(FROM_GCM_INTENT_ARG, true);
                    startWakefulService(context, serviceIntent);

                } else if (intent.getStringExtra(TYPE_GCM_INTENT_ARG_KEY).equals(Type.NOTIFICATION)) {

                    Log.d(TAG, "received a notifiation");
                    if (intent.hasExtra(PAYLOAD_GCM_INTENT_ARG_KEY)) {

                        Log.d(TAG, intent.getStringExtra(PAYLOAD_GCM_INTENT_ARG_KEY));
                        try {
                            LinkedHashMap<String, Object> params = HollerbackApplication.getInstance().getObjectMapper()
                                    .readValue(intent.getStringExtra(PAYLOAD_GCM_INTENT_ARG_KEY), new TypeReference<LinkedHashMap<String, Object>>() {
                                    });
                            if (params.containsKey("message")) {
                                String message = (String) params.get("message");
                                NotificationUtil.launchNotification(HollerbackApplication.getInstance(), NotificationUtil.generateNotification(context.getString(R.string.app_name), message),
                                        USER_JOINED_NOTIF_ID);
                            }
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "invalid session");
        }

    }
}
