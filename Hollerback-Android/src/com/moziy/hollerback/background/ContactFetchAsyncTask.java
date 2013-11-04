package com.moziy.hollerback.background;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.util.CollectionOpUtils;
import com.moziy.hollerback.util.NumberUtil;

public class ContactFetchAsyncTask extends AsyncTask<Void, Void, SortedArray> {
    private FragmentTransaction ft;
    private Activity activity;

    public ContactFetchAsyncTask(Activity activity, FragmentTransaction ft) {
        this.activity = activity;
        this.ft = ft;
    }

    public ContactFetchAsyncTask() {
    }

    @SuppressWarnings("unused")
    protected SortedArray doInBackground(Void... params) {
        Cursor c = activity.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data._ID, Data.DISPLAY_NAME, Phone.NUMBER, Data.CONTACT_ID, Phone.TYPE, Phone.LABEL
        }, Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'", null, Data.DISPLAY_NAME);

        int count = c.getCount();
        boolean b = c.moveToFirst();
        String[] columnNames = c.getColumnNames();
        int displayNameColIndex = c.getColumnIndex("display_name");
        int idColIndex = c.getColumnIndex("_id");
        // int contactIdColIndex = c.getColumnIndex("contact_id");
        int col2Index = c.getColumnIndex(columnNames[2]);
        int col3Index = c.getColumnIndex(columnNames[3]);
        int col4Index = c.getColumnIndex(columnNames[4]);

        ArrayList<UserModel> contactItemList = new ArrayList<UserModel>();

        ActiveAndroid.beginTransaction();
        new Delete().from(ConversationModel.class).execute();

        for (int i = 0; i < count; i++) {

            String displayName = c.getString(displayNameColIndex);
            String phoneNumber = c.getString(col2Index);
            int contactId = c.getInt(col3Index);
            String phoneType = c.getString(col4Index);

            long _id = c.getLong(idColIndex);
            UserModel contactItem = new UserModel();
            // contactItem.userId = _id;
            contactItem.contactId = contactId;
            contactItem.name = displayName;

            contactItem.phone = NumberUtil.getE164Number(phoneNumber);
            if (contactItem.phone != null) {
                contactItemList.add(contactItem);
            }

            contactItem.save();
            boolean b2 = c.moveToNext();
        }

        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();

        c.close();

        return CollectionOpUtils.sortContacts(contactItemList);

    }

    protected void onPostExecute(SortedArray result) {
        // ((HollerbackMainActivity) activity).addContactListFragment(ft, result);
    }
}
