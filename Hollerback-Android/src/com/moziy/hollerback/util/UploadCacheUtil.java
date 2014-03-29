package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.moziy.hollerback.util.sharedpreference.JSONSharedPreferences;

import android.content.Context;

public class UploadCacheUtil {

    private static String UPLOADCACHING = "UPLOADCACHING";

    public static void clearCache(Context c, long conversationId) {
        JSONSharedPreferences.remove(c, UPLOADCACHING, String.valueOf(conversationId));
    }

    public static void setUploadCacheFlag(Context c, String conversationId, JSONObject object) {
        try {
            JSONArray videoArray = JSONSharedPreferences.loadJSONArray(c, UPLOADCACHING, conversationId);
            videoArray.put(object);
            JSONSharedPreferences.saveJSONArray(c, UPLOADCACHING, conversationId, videoArray);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void removeUploadedCache(Context c, String conversationId, JSONObject object) {
        try {
            JSONArray videoArray = JSONSharedPreferences.loadJSONArray(c, UPLOADCACHING, conversationId);

            for (int i = 0; i < videoArray.length(); i++) {
                if (videoArray.getJSONObject(i).getString("thumb_url").equalsIgnoreCase(object.getString("thumb_url"))) {
                    JSONSharedPreferences.saveJSONArray(c, UPLOADCACHING, conversationId, remove(i, videoArray));
                    break;
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static JSONArray getCacheFlags(Context c, long conversationId) {
        try {
            JSONArray videoArray = JSONSharedPreferences.loadJSONArray(c, UPLOADCACHING, String.valueOf(conversationId));
            return videoArray;
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean hasVideoCache(Context c, long conversationId) {
        try {
            JSONArray videoArray = JSONSharedPreferences.loadJSONArray(c, UPLOADCACHING, String.valueOf(conversationId));
            if (videoArray.length() > 0) {
                return true;
            }
        } catch (JSONException e) {

        }
        return false;
    }

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }
}
