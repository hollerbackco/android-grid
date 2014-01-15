package com.moziy.hollerback.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.contacts.task.ContactsTaskGroup;
import com.moziy.hollerback.contacts.task.GetHBContactsTask;
import com.moziy.hollerback.contacts.task.GetUserContactsTask;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.workers.ActivityTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.util.CollectionOpUtils;

/**
 * This class is a delegate to contact related operations
 * @author sajjad
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ContactsDelegate implements TaskClient, ContactsInterface {
    static final String TAG = ContactsDelegate.class.getSimpleName();

    private interface Workers {
        public static final String CONTACTS = "contacts-worker";
    }

    private HollerbackMainActivity mActivity;
    private Queue<Task> mTaskQueue = new LinkedList<Task>();
    public static final String PHONE_COLUMN = (Build.VERSION.SDK_INT >= 16 ? Phone.NORMALIZED_NUMBER : Phone.NUMBER);

    private List<Contact> mContacts; // all phone contacts
    private List<Contact> mContactsExcludingHbFriends; // contacts excluding hollerback friends
    private List<Contact> mHBContacts; // hollerback contacts
    private List<Contact> mRecents; // recents
    private List<Contact> mFriends; // friends

    private List<Contact> mPendingTransferToFriends;
    private List<Contact> mPendingRemovalFromFriends;

    private LOADING_STATE mContactsLoadState = LOADING_STATE.IDLE;
    private LOADING_STATE mHBContactsLoadState = LOADING_STATE.IDLE;

    public ContactsDelegate(HollerbackMainActivity activity) {
        mActivity = activity;
        mPendingRemovalFromFriends = new ArrayList<Contact>();
        mPendingTransferToFriends = new ArrayList<Contact>();

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
            mContactsExcludingHbFriends = new ArrayList<Contact>(mContacts); // add all for now
            mContactsLoadState = LOADING_STATE.DONE;

            // XXX: fill in later
            // mRecents = new ArrayList<Contact>(mContacts.subList(0, Math.min(3, mContacts.size())));
            mRecents = new ArrayList<Contact>();
            mRecents = new Select().from(Contact.class).where(ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME + " IS NOT NULL ")
                    .orderBy("strftime('%s'," + ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME + ") DESC").limit(3).execute();

            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));

            // lets see if we should launch our workers to check the contacts against the server

        } else if (t instanceof GetHBContactsTask) {
            Log.d(TAG, "got hb contacts");
            mHBContacts = ((GetHBContactsTask) t).getHBContacts();

            // remove all of hb contacts from contacts

            if (mHBContacts != null && mContactsExcludingHbFriends != null) {
                for (Contact hbContact : mHBContacts) {
                    Iterator<Contact> itr = mContactsExcludingHbFriends.iterator();
                    while (itr.hasNext()) {

                        if (hbContact.mAndroidContactId == itr.next().mAndroidContactId) {
                            itr.remove();
                            break;
                        }
                    }
                }
            }

            // mFriends = new ArrayList<Contact>(mContacts.subList(0, Math.min(10, mContacts.size())));
            mFriends = new Select().from(Contact.class).orderBy(ActiveRecordFields.C_FRIENDS_NAME).execute();
            Log.d(TAG, "friends size: " + mFriends.size());
            if (mFriends != null) {
                // lets remove the friends from the contacts excluding hb and from the hbcontacts
                for (Contact friend : mFriends) {
                    Iterator<Contact> itr = mHBContacts.iterator();
                    while (itr.hasNext()) {
                        if (CollectionOpUtils.intersects(friend.mPhoneHashes, itr.next().mPhoneHashes)) {
                            itr.remove();
                        }
                    }

                    itr = mContactsExcludingHbFriends.iterator();
                    while (itr.hasNext()) {
                        if (CollectionOpUtils.intersects(friend.mPhoneHashes, itr.next().mPhoneHashes)) {
                            itr.remove();
                        }
                    }

                }
            }

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

    @Override
    public List<Contact> getRecentContacts() {
        return mRecents;
    }

    @Override
    public List<Contact> getFriends() {
        return mFriends;
    }

    @Override
    public List<Contact> getContactsExcludingHBContacts() {
        return mContactsExcludingHbFriends;
    }

    private boolean removeContactFrom(Contact contact, List<Contact> list) {
        Iterator<Contact> itr = list.iterator();
        while (itr.hasNext()) {
            if (CollectionOpUtils.intersects(contact.mPhoneHashes, itr.next().mPhoneHashes)) {
                itr.remove();
                Log.d(TAG, "removed");
            }
        }

        return false;
    }

    public Transaction beginTransaction() {
        return new TransactionImpl();
    }

    public interface Transaction {

        public void commit();

        public void addToFriends(Contact c);

        public void removeFromFriends(Contact c);
    }

    private class TransactionImpl implements Transaction {

        private Set<Contact> mPendingAdd;
        private Set<Contact> mPendingRemove;

        public TransactionImpl() {
            mPendingAdd = new HashSet<Contact>();
            mPendingRemove = new HashSet<Contact>();
        }

        @Override
        public void commit() {

            // add friends to the friends list
            if (mFriends != null && !mPendingAdd.isEmpty()) {
                // TODO: combine the transactions
                ActiveAndroid.beginTransaction();
                try {

                    for (Contact newFriend : mPendingAdd) {

                        mFriends.add(newFriend);

                        if (mHBContacts != null) {
                            removeContactFrom(newFriend, mHBContacts);
                        }

                        if (mContactsExcludingHbFriends != null) {
                            removeContactFrom(newFriend, mContactsExcludingHbFriends);
                        }

                        newFriend.save();
                    }

                    ActiveAndroid.setTransactionSuccessful();

                } finally {

                    ActiveAndroid.endTransaction();

                }

                Collections.sort(mFriends, Contact.COMPARATOR); // sort the friends

            }

            // remove friends from the friends list
            if (mFriends != null && !mPendingRemove.isEmpty()) {

                ActiveAndroid.beginTransaction();
                try {

                    for (Contact existingFriend : mPendingRemove) {

                        removeContactFrom(existingFriend, mFriends);

                        if (mHBContacts != null && existingFriend.mIsOnHollerback) {
                            removeContactFrom(existingFriend, mHBContacts);
                            mHBContacts.add(existingFriend);
                        }

                        if (mContactsExcludingHbFriends != null && !existingFriend.mIsOnHollerback) {
                            removeContactFrom(existingFriend, mContactsExcludingHbFriends);
                            mContactsExcludingHbFriends.add(existingFriend);
                        }

                        existingFriend.delete();
                    }

                    ActiveAndroid.setTransactionSuccessful();

                } finally {

                    ActiveAndroid.endTransaction();

                }

            }

        }

        @Override
        public void addToFriends(Contact c) {
            mPendingAdd.add(c);
            mPendingRemove.remove(c);
        }

        @Override
        public void removeFromFriends(Contact c) {
            mPendingRemove.add(c);
            mPendingAdd.remove(c);
        }

    }

}
