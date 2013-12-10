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
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.fragment.workers.ActivityTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.CursorTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskGroup;
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
    private List<Contact> mHBContacts;
    private LOADING_STATE mContactsLoadState = LOADING_STATE.IDLE;
    private LOADING_STATE mHBContactsLoadState = LOADING_STATE.IDLE;

    public ContactsDelegate(HollerbackMainActivity activity) {
        mActivity = activity;

    }

    public void initWorkers() {
        Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(Workers.CONTACTS);
        if (f == null) {

            mTaskQueue.add(new ContactsTaskGroup(mActivity));

            ActivityTaskWorker worker = ActivityTaskWorker.newInstance(false);
            mActivity.getSupportFragmentManager().beginTransaction().add(worker, Workers.CONTACTS).commit();
            mContactsLoadState = LOADING_STATE.LOADING;
            mHBContactsLoadState = LOADING_STATE.LOADING;
        }
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetUserContactsTask) {
            Log.d(TAG, "got user contacts");
            // alright we have our contacts
            mContacts = ((GetUserContactsTask) t).getContacts();
            mContactsLoadState = LOADING_STATE.DONE;
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));

            // lets see if we should launch our workers to check the contacts against the server

        } else if (t instanceof GetHBContactsTask) {
            Log.d(TAG, "got hb contacts");
            mHBContacts = ((GetHBContactsTask) t).getHBContacts();
            // remove all of hb contacts from contacts
            mContacts.removeAll(mHBContacts);
            mHBContactsLoadState = LOADING_STATE.DONE;
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));
        }

    }

    @Override
    public void onTaskError(Task t) {
        if (t instanceof GetHBContactsTask) {
            mHBContactsLoadState = LOADING_STATE.FAILED;
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));
        }

        if (t instanceof GetUserContactsTask) {
            mContactsLoadState = LOADING_STATE.FAILED;
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));
        }

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    @Override
    public LOADING_STATE getDeviceContactsLoadState() {
        return mContactsLoadState;
    }

    @Override
    public LOADING_STATE getHbContactsLoadState() {

        return mHBContactsLoadState;
    }

    @Override
    public List<Contact> getDeviceContacts() {
        return mContacts;
    }

    @Override
    public List<Contact> getHollerbackContacts() {
        return mHBContacts;
    }

    /**
     * A task that gets the user contacts
     * @author sajjad
     *
     */
    private static class GetUserContactsTask extends CursorTask {

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

    private static class GetHBContactsTask extends AbsTask {

        private List<Contact> mListToCheck;
        private List<Contact> mHollerbackFriends;

        private Map<String, Contact> mContactMap;

        private GetUserContactsTask mGetContactsTask;

        private boolean mHttpDone = false;

        /*
         * list of contacts to check
         */
        public GetHBContactsTask(GetUserContactsTask getContactsTask) {
            mGetContactsTask = getContactsTask;
        }

        @Override
        public void run() {

            if (!mGetContactsTask.isSuccess()) {
                Log.w(TAG, "not running GetHBTask because of failure in GetUserTask");
                mIsSuccess = false;
                mIsFinished = true;
                return;
            }

            mListToCheck = mGetContactsTask.getContacts();
            mContactMap = new HashMap<String, Contact>();

            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e1) {
                throw new IllegalStateException("MD5 Needed!");

            }

            // for each item in the list generate the phone hash
            final ArrayList<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
            for (Contact c : mListToCheck) {

                c.mPhoneHashed = md5.digest(c.mPhone.getBytes());
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < c.mPhoneHashed.length; i++) {
                    hexString.append(Integer.toHexString(0xFF & c.mPhoneHashed[i]));
                }

                c.mPhoneHashHexString = hexString.toString();

                mContactMap.put(c.mPhoneHashHexString, c); // create a map while we're calculating

                Map<String, String> contact = new HashMap<String, String>();
                contact.put(HollerbackAPI.PARAM_CONTACTS_NAME, c.mName);
                contact.put(HollerbackAPI.PARAM_CONTACTS_PHONE, c.mPhoneHashHexString);

                contacts.add(contact);
            }

            // now that we have the hash, lets get the contacts
            HBRequestManager.getContacts(contacts, new HBSyncHttpResponseHandler<Envelope<ArrayList<UserModel>>>(new TypeReference<Envelope<ArrayList<UserModel>>>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<ArrayList<UserModel>> response) {
                    if (response.data != null) {
                        mHollerbackFriends = new ArrayList<Contact>();
                        for (UserModel u : response.data) {
                            Contact hbFriend = mContactMap.get(u.phone_hashed);
                            hbFriend.mIsOnHollerback = true; // mark the contact as an hb friend
                            hbFriend.mUsername = u.username;
                            mHollerbackFriends.add(hbFriend);
                        }

                    }

                    mHttpDone = true;
                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    Log.d(TAG, "failure");
                    mHttpDone = true;
                }

            });

            while (!mHttpDone) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            mIsSuccess = true;
            mIsFinished = true;

        }

        public List<Contact> getHBContacts() {
            return mHollerbackFriends;
        }
    }

    /**
     * This class chains the tasks together
     * @author sajjad
     *
     */
    private static class ContactsTaskGroup extends TaskGroup {

        public ContactsTaskGroup(Activity activity) {
            GetUserContactsTask contactsTask = new GetUserContactsTask(activity.getContentResolver(), ContactsContract.Data.CONTENT_URI, new String[] {
                    Data._ID, Data.DISPLAY_NAME, PHONE_COLUMN, Data.CONTACT_ID, Phone.TYPE, Phone.LABEL, Data.PHOTO_ID
            }, Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'", null, ContactsContract.Data.DISPLAY_NAME);

            addTask(contactsTask);

            // this task relies on the first one to successfully execute
            addTask(new GetHBContactsTask(contactsTask));

        }
    }

}
