package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;

public class JSONUtil {

    /**
     * From this point, we changed architecture from broastcast based to loader based,
     * loader comes with fragment and lifecycle follows the fragment rather than being affected 
     * by anything else.  In the future change everything into loader or executerservice based
     * @param object
     * @return
     */
    public static void processVerify(JSONObject object) {

        LogUtil.i(object.toString());

        try {

            JSONObject user = object.getJSONObject("user");

            String access_token = "";
            if (object.has("access_token")) {
                access_token = user.getString("access_token");
            }

            PreferenceManagerUtil.setPreferenceValue(HBPreferences.ACCESS_TOKEN, access_token);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void processGetContacts(JSONObject json) {
        try {
            ArrayList<UserModel> users = new ArrayList<UserModel>();
            JSONArray dataObject = json.getJSONArray("data");

            ActiveAndroid.beginTransaction();

            for (int i = 0; i < dataObject.length(); i++) {
                JSONObject userObject = dataObject.getJSONObject(i);
                UserModel user = new UserModel();
                user.name = userObject.getString("name");
                user.phone = userObject.getString("phone_normalized");
                user.isHollerbackUser = true;

                List<Model> userLocal = (List<Model>) new Select().from(UserModel.class).where(ActiveRecordFields.C_USER_PHONE + " = ?", user.phone).execute();

                if (userLocal == null || userLocal.size() < 1) {
                    user.save();
                } else {
                    ((UserModel) userLocal.get(0)).isHollerbackUser = true;
                    ((UserModel) userLocal.get(0)).save();
                }

                users.add(user);
                if (TempMemoryStore.users.mUserModelHash.containsKey(user.phone)) {
                    TempMemoryStore.users.mUserModelHash.get(user.phone).isHollerbackUser = true;
                }
            }

            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();

            ArrayList<UserModel> valuesList = new ArrayList<UserModel>(TempMemoryStore.users.mUserModelHash.values());

            SortedArray array = CollectionOpUtils.sortContacts(valuesList);

            TempMemoryStore.users = array;

            // for (UserModel user : TempMemoryStore.users) {
            // LogUtil.i(user.mDisplayName + " hb: "
            // + Boolean.toString(user.isHollerbackUser));
            // }

            // TempMemoryStore.users = users;
            Intent intent = new Intent(IABIntent.GET_CONTACTS);
            IABroadcastManager.sendLocalBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the last one when you clean this up, I am working fast to get the other guy's parsing logic working
     * @param json
     * @param v2
     * @return
     */
    public static SortedArray processGetContacts(JSONObject json, boolean v2) {
        try {
            ArrayList<UserModel> users = new ArrayList<UserModel>();
            JSONArray dataObject = json.getJSONArray("data");

            ActiveAndroid.beginTransaction();

            for (int i = 0; i < dataObject.length(); i++) {
                JSONObject userObject = dataObject.getJSONObject(i);
                UserModel user = new UserModel();
                user.name = userObject.getString("name");
                user.phone = userObject.getString("phone_normalized");
                user.isHollerbackUser = true;

                List<Model> userLocal = (List<Model>) new Select().from(UserModel.class).where(ActiveRecordFields.C_USER_PHONE + " = ?", user.phone).execute();

                if (userLocal == null || userLocal.size() < 1) {
                    user.save();
                } else {
                    ((UserModel) userLocal.get(0)).isHollerbackUser = true;
                    ((UserModel) userLocal.get(0)).save();
                }

                users.add(user);
                if (TempMemoryStore.users.mUserModelHash.containsKey(user.phone)) {
                    TempMemoryStore.users.mUserModelHash.get(user.phone).isHollerbackUser = true;
                }
            }

            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();

            ArrayList<UserModel> valuesList = new ArrayList<UserModel>(TempMemoryStore.users.mUserModelHash.values());

            SortedArray array = CollectionOpUtils.sortContacts(valuesList);

            TempMemoryStore.users = array;

            return array;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new SortedArray();
    }

}
