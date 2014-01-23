package com.moziy.hollerback.contacts;

import java.util.ArrayList;
import java.util.Collection;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.contacts.task.ContactsTaskGroup;
import com.moziy.hollerback.contacts.task.GetFriendsTask;
import com.moziy.hollerback.contacts.task.GetHBContactsTask;
import com.moziy.hollerback.contacts.task.GetUserContactsTask;
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
        public static final String FRIENDS = "friends-worker";
    }

    private HollerbackMainActivity mActivity;
    private Queue<Task> mTaskQueue = new LinkedList<Task>();
    public static final String PHONE_COLUMN = (Build.VERSION.SDK_INT >= 16 ? Phone.NORMALIZED_NUMBER : Phone.NUMBER);

    private List<Contact> mContacts; // all phone contacts
    private List<Contact> mContactsExcludingHbContacts; // contacts excluding hollerback friends
    private List<Contact> mHBContactsExludingFriends;
    private List<Contact> mHBContacts; // hollerback contacts
    private List<Contact> mRecents; // recents
    private List<Contact> mFriends; // friends
    private Set<Contact> mInviteList; // users that we plan on inviting

    private LOADING_STATE mContactsLoadState = LOADING_STATE.IDLE;
    private LOADING_STATE mHBContactsLoadState = LOADING_STATE.IDLE;
    private LOADING_STATE mFriendsLoadState = LOADING_STATE.IDLE;

    public ContactsDelegate(HollerbackMainActivity activity) {
        mActivity = activity;

    }

    public void initWorkers() {
        Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(Workers.CONTACTS);
        if (f == null) {

            mTaskQueue.add(new ContactsTaskGroup(mActivity));

            ActivityTaskWorker worker = ActivityTaskWorker.newInstance(false);
            FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction().add(worker, Workers.CONTACTS);

            mTaskQueue.add(new GetFriendsTask());
            worker = ActivityTaskWorker.newInstance(false);
            transaction.add(worker, Workers.FRIENDS).commit();

            mContactsLoadState = LOADING_STATE.LOADING;
            mHBContactsLoadState = LOADING_STATE.LOADING;
            mFriendsLoadState = LOADING_STATE.LOADING;
        }
    }

    @Override
    public void onTaskComplete(Task t) {
        if (t instanceof GetUserContactsTask) {
            Log.d(TAG, "got user contacts");
            // alright we have our contacts
            mContacts = ((GetUserContactsTask) t).getContacts();
            mContactsExcludingHbContacts = new ArrayList<Contact>(mContacts); // add all for now
            mInviteList = new HashSet<Contact>();
            mContactsLoadState = LOADING_STATE.DONE;

        } else if (t instanceof GetHBContactsTask) {

            Log.d(TAG, "got hb contacts");
            mHBContacts = ((GetHBContactsTask) t).getHBContacts();

            mHBContactsLoadState = LOADING_STATE.DONE;

        } else if (t instanceof GetFriendsTask) {

            if (t.isSuccess()) {

                mFriends = ((GetFriendsTask) t).getFriends();
                mRecents = ((GetFriendsTask) t).getRecentFriends();

            }

            mFriendsLoadState = LOADING_STATE.DONE;

        }

        setupContactsAfterLoad();

        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));

    }

    @Override
    public void onTaskError(Task t) {
        if (t instanceof GetHBContactsTask) {
            mHBContactsLoadState = LOADING_STATE.FAILED;

        }

        if (t instanceof GetUserContactsTask) {
            mContactsLoadState = LOADING_STATE.FAILED;
        }

        if (t instanceof GetFriendsTask) {

            mFriends = ((GetFriendsTask) t).getFriends();
            mRecents = ((GetFriendsTask) t).getRecentFriends();

            mFriendsLoadState = LOADING_STATE.FAILED;
        }

        setupContactsAfterLoad();

        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(IABIntent.CONTACTS_UPDATED));

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    private void setupContactsAfterLoad() {

        if (mContacts != null && mHBContacts != null && mContactsExcludingHbContacts == null) {
            mContactsExcludingHbContacts = new ArrayList<Contact>(mContacts);
            for (Contact hbContact : mHBContacts) {

                Iterator<Contact> itr = mContactsExcludingHbContacts.iterator();

                while (itr.hasNext()) {
                    if (CollectionOpUtils.intersects(itr.next().mPhoneHashes, hbContact.mPhoneHashes)) {
                        itr.remove();
                    }
                }
            }
        }

        if (mHBContacts != null && mFriends != null && mHBContactsExludingFriends == null) {
            mHBContactsExludingFriends = new ArrayList<Contact>(mHBContacts);
            for (Contact friend : mFriends) {

                Iterator<Contact> itr = mHBContactsExludingFriends.iterator();
                while (itr.hasNext()) {
                    if (itr.next().mUsername.equals(friend.mUsername)) {
                        itr.remove();
                    }
                }

            }
        }

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
        return mContactsExcludingHbContacts;
    }

    @Override
    public LOADING_STATE getFriendsLoadState() {
        return mFriendsLoadState;
    }

    @Override
    public List<Contact> getHBContactsExcludingFriends() {
        return mHBContactsExludingFriends;
    }

    @Override
    public Set<Contact> getInviteList() {
        return mInviteList;
    }

    @Override
    public boolean removeContactFrom(Contact src, Collection<Contact> list) {
        Iterator<Contact> itr = list.iterator();
        Contact dest;
        while (itr.hasNext()) {
            dest = itr.next();
            if (CollectionOpUtils.intersects(src.mPhoneHashes, dest.mPhoneHashes) || src.mUsername.equals(dest.mUsername)) {
                itr.remove();
                return true;

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
                ArrayList<String> usernames = new ArrayList<String>();
                // TODO: combine the transactions
                ActiveAndroid.beginTransaction();
                try {

                    for (Contact newFriend : mPendingAdd) {

                        boolean isFriend = false;
                        // only add if the user is not a friend already
                        for (Contact c : mFriends) {
                            if (c.mUsername.equals(newFriend.mUsername)) {
                                isFriend = true;
                                break;
                            }
                        }

                        if (isFriend) {
                            continue;
                        }

                        mFriends.add(newFriend);
                        usernames.add(newFriend.mUsername);

                        if (mHBContactsExludingFriends != null) {
                            removeContactFrom(newFriend, mHBContactsExludingFriends);
                        }

                        if (mContactsExcludingHbContacts != null) {
                            removeContactFrom(newFriend, mContactsExcludingHbContacts);
                        }

                        newFriend.save(); // save to friends
                    }

                    ActiveAndroid.setTransactionSuccessful();

                } finally {

                    ActiveAndroid.endTransaction();

                }

                // for all the pending friends being added lets add them
                HBRequestManager.addFriends(new ArrayList<String>(usernames), new AsyncHttpResponseHandler());

            }

            // remove friends from the friends list
            if (mFriends != null && !mPendingRemove.isEmpty()) {

                ArrayList<String> usernames = new ArrayList<String>(); // used for network operation

                ActiveAndroid.beginTransaction();
                try {

                    for (Contact existingFriend : mPendingRemove) {

                        removeContactFrom(existingFriend, mFriends);

                        removeContactFrom(existingFriend, mRecents);

                        usernames.add(existingFriend.mUsername);

                        if (mHBContactsExludingFriends != null) {
                            removeContactFrom(existingFriend, mHBContactsExludingFriends);
                            mHBContactsExludingFriends.add(existingFriend);
                        }

                        // TODO: work out..sajjad - NOT correct since the id is not transferred this will always fail
                        if (existingFriend.mFriend != null) {
                            existingFriend.mFriend.delete();
                            existingFriend.mFriend = null;

                        }
                    }

                    ActiveAndroid.setTransactionSuccessful();

                } finally {

                    ActiveAndroid.endTransaction();

                }

                // for all the pending friends being added lets add them
                HBRequestManager.removeFriends(new ArrayList<String>(usernames), new AsyncHttpResponseHandler());

            }

            if (mFriends != null) {
                Collections.sort(mFriends, Contact.COMPARATOR); // sort the friends
            }
            if (mHBContacts != null) {
                Collections.sort(mHBContacts, Contact.COMPARATOR);
            }
            if (mContactsExcludingHbContacts != null) {
                Collections.sort(mContactsExcludingHbContacts, Contact.COMPARATOR);
            }
            if (mHBContactsExludingFriends != null) {
                Collections.sort(mHBContactsExludingFriends, Contact.COMPARATOR);
            }

        }

        @Override
        public void addToFriends(Contact c) {
            mPendingAdd.add(c);
            mPendingRemove.remove(c);
        }

        @Override
        public void removeFromFriends(Contact c) {

            if (!mPendingAdd.remove(c))
                mPendingRemove.add(c);

        }

    }

    @Override
    public boolean hasFriend(String username) {
        for (Contact f : mFriends) {
            if (f.mUsername.equals(username)) {
                return true;
            }

        }
        return false;
    }

    public Contact getFriendByUsername(String username) {

        for (Contact f : mFriends) {
            if (f.mUsername.equals(username)) {
                return f;
            }

        }

        return null;
    }

}
