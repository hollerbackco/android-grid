package com.moziy.hollerback.contacts.task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.connection.HollerbackAPI;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;

public class GetHBContactsTask extends AbsTask {
    private static final String TAG = GetHBContactsTask.class.getSimpleName();
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
            md5.reset();
        } catch (NoSuchAlgorithmException e1) {
            throw new IllegalStateException("MD5 Needed!");

        }

        // for each item in the list generate the phone hash
        final ArrayList<Map<String, String>> contacts = new ArrayList<Map<String, String>>(); // contacts going to the server
        for (Contact friend : mListToCheck) {

            friend.generateHash(md5);

            for (int i = 0; i < friend.mPhoneHashes.size(); i++) {

                mContactMap.put(friend.mPhoneHashes.get(i), friend); // create a map for each unique hash

                Map<String, String> contact = new HashMap<String, String>();
                contact.put(HollerbackAPI.PARAM_CONTACTS_NAME, friend.mName);
                contact.put(HollerbackAPI.PARAM_CONTACTS_PHONE, friend.mPhoneHashes.get(i));

                contacts.add(contact);

            }

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

                    Collections.sort(mHollerbackFriends, Contact.COMPARATOR); // sort the results in alphabetical based on name

                }

                mHttpDone = true;
                mIsSuccess = true;
            }

            @Override
            public void onApiFailure(Metadata metaData) {
                Log.d(TAG, "failure");
                mHttpDone = true;
                mIsSuccess = false;
            }

        });

        while (!mHttpDone) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mIsFinished = true;

    }

    public List<Contact> getHBContacts() {
        return mHollerbackFriends;
    }
}
