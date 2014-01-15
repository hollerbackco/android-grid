package com.moziy.hollerback.contacts.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.moziy.hollerback.contacts.ContactsDelegate;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.CursorTask;

/**
 * A task that gets the user contacts
 * @author sajjad
 *
 */
public class GetUserContactsTask extends AbsTask {
    private static final String TAG = GetUserContactsTask.class.getSimpleName();
    private LinkedHashMap<Long, Contact> mContactMap = new LinkedHashMap<Long, Contact>();
    private ContentResolver mContentResolver;

    public GetUserContactsTask(Context context) {
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void run() {

        // perform an inner join on the raw contacts to get the unique contact_id the data is associated with
        CursorTask getContactsTask = new CursorTask(mContentResolver, ContactsContract.Data.CONTENT_URI, new String[] {
                ContactsContract.RawContacts.CONTACT_ID, Data._ID, Data.DISPLAY_NAME, ContactsDelegate.PHONE_COLUMN, Data.CONTACT_ID, Phone.TYPE, Phone.LABEL, Data.PHOTO_ID
        }, Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'", null, ContactsContract.Data.DISPLAY_NAME);

        getContactsTask.run(); // run the contacts task

        if (!getContactsTask.isSuccess()) {
            Log.w(TAG, "couldn't get contacts!");
            mIsSuccess = false;
            mIsFinished = true;
        }
        Cursor c = getContactsTask.getCursor(); // get the cursor

        if (c.moveToFirst()) {
            do {

                long contactId = c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME));
                String phone = c.getString(c.getColumnIndex(ContactsDelegate.PHONE_COLUMN));
                String phoneLabel = c.getString(c.getColumnIndex(Phone.LABEL));
                int photoId = c.getInt(c.getColumnIndex(Data.PHOTO_ID));

                if (Build.VERSION.SDK_INT < 16) { // normalize phone numbers to e164
                    try {
                        phone = PhoneNumberUtil.getInstance().format(PhoneNumberUtil.getInstance().parse(phone, Locale.getDefault().getCountry()), PhoneNumberFormat.E164);
                    } catch (NumberParseException e) {
                        phone = null; // if we can't parse it, then foget about it
                        e.printStackTrace();
                    }
                }

                if (mContactMap.containsKey(contactId)) {
                    if (phone != null) {
                        Contact existingContact = mContactMap.get(contactId);
                        Log.d(TAG, "found another phone for contact: " + existingContact.toString());
                        existingContact.mPhones.add(phone); // add the phone
                    }

                } else { // new contact

                    if (phone != null) {
                        Contact contact = new Contact(contactId, name, phone, phoneLabel, photoId);
                        mContactMap.put(contactId, contact);
                    }
                }

            } while (c.moveToNext());

            c.close();
        }

        mIsFinished = true;

    }

    public List<Contact> getContacts() {
        List<Contact> l = new ArrayList<Contact>(mContactMap.values());
        return l;
    }

}
