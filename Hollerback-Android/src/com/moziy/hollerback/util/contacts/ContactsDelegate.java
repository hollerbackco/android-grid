package com.moziy.hollerback.util.contacts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.fragment.workers.ActivityTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.CursorTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.util.HollerbackAPI;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.HBSyncHttpResponseHandler;

/**
 * This class is a delegate to contact related operations
 * @author sajjad
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ContactsDelegate implements TaskClient, ContactsInterface {
    private static final String TAG = ContactsDelegate.class.getSimpleName();

    private interface Workers {
        public static final String CONTACTS = "contacts-worker";
    }

    private HollerbackMainActivity mActivity;
    private Queue<Task> mTaskQueue = new LinkedList<Task>();
    private static final String PHONE_COLUMN = (Build.VERSION.SDK_INT >= 16 ? Phone.NORMALIZED_NUMBER : Phone.NUMBER);

    private List<Contact> mContacts;
    private boolean mContactsLoaded = false;

    public ContactsDelegate(HollerbackMainActivity activity) {
        mActivity = activity;

    }

    public void initWorkers() {
        Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(Workers.CONTACTS);
        if (f == null) {

            mTaskQueue.add(new GetUserContactsTask(mActivity.getContentResolver(), ContactsContract.Data.CONTENT_URI, new String[] {
                    Data._ID, Data.DISPLAY_NAME, PHONE_COLUMN, Data.CONTACT_ID, Phone.TYPE, Phone.LABEL, Data.PHOTO_ID
            }, Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'", null, ContactsContract.Data.DISPLAY_NAME));

            ActivityTaskWorker worker = ActivityTaskWorker.newInstance(false);
            mActivity.getSupportFragmentManager().beginTransaction().add(worker, Workers.CONTACTS).commit();
        }
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetUserContactsTask) {

            // alright we have our contacts
            mContacts = ((GetUserContactsTask) t).getContacts();
            mContactsLoaded = true;

            mTaskQueue.add(new GetHBContactsTask(mContacts));
            ActivityTaskWorker worker = ActivityTaskWorker.newInstance(false);
            mActivity.getSupportFragmentManager().beginTransaction().add(worker, "w1").commit();

            // lets see if we should launch our workers to check the contacts against the server

        } else {
            // contacts downloaded task
        }

    }

    @Override
    public void onTaskError(Task t) {

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    @Override
    public boolean deviceContactsLoaded() {
        return mContactsLoaded;
    }

    @Override
    public List<Contact> getDeviceContacts() {
        return mContacts;
    }

    /**
     * A task that gets the user contacts
     * @author sajjad
     *
     */
    private class GetUserContactsTask extends CursorTask {

        private List<Contact> mContacts = new ArrayList<Contact>();

        public GetUserContactsTask(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(resolver, uri, projection, selection, selectionArgs, sortOrder);

        }

        @Override
        public void run() {
            super.run();
            mIsFinished = false;

            Cursor c = getCursor();
            if (c.moveToFirst()) {
                do {

                    String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME));
                    String phone = c.getString(c.getColumnIndex(PHONE_COLUMN));
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

                    if (phone != null) {
                        mContacts.add(new Contact(name, phone, phoneLabel, photoId));
                    }

                } while (c.moveToNext());

                c.close();
            }

            mIsFinished = true;

        }

        public List<Contact> getContacts() {
            return mContacts;
        }

    }

    private class GetHBContactsTask extends AbsTask {

        private List<Contact> mListToCheck;
        private List<Contact> mHollerbackFriends;

        /*
         * list of contacts to check
         */
        public GetHBContactsTask(List<Contact> list) {
            mListToCheck = list;

        }

        @Override
        public void run() {

            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e1) {
                throw new IllegalStateException("MD5 Needed!");

            }

            // for each item in the list generate the phone hash
            ArrayList<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
            for (Contact c : mListToCheck) {

                Log.d(TAG, "will send off: " + c.toString());
                c.mPhoneHashed = md5.digest(c.mPhone.getBytes());
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < c.mPhoneHashed.length; i++) {
                    hexString.append(Integer.toHexString(0xFF & c.mPhoneHashed[i]));
                }
                c.mPhoneHashHexString = hexString.toString();

                Map<String, String> contact = new HashMap<String, String>();
                contact.put(HollerbackAPI.PARAM_CONTACTS_NAME, c.mName);
                contact.put(HollerbackAPI.PARAM_CONTACTS_PHONE, c.mPhoneHashHexString);

                contacts.add(contact);
            }

            HBRequestManager.getContacts(contacts, new HBSyncHttpResponseHandler<ResponseObject>(new TypeReference<ResponseObject>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, ResponseObject response) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    // TODO Auto-generated method stub

                }
            });

        }
    }

}
