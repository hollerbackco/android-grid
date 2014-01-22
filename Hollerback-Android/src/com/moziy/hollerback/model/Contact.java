package com.moziy.hollerback.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.util.Log;

import com.moziy.hollerback.util.security.HashUtil;

public class Contact implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = Contact.class.getSimpleName();

    public Friend mFriend;

    public long mAndroidContactId;

    public String mName;

    public String mPhoneLabel;

    public int mPhotoID;

    public boolean mIsOnHollerback;

    public String mUsername; // if an hb friend, the username

    public String mLastContactTime; // the last time the user was contacted

    public ArrayList<String> mPhones = new ArrayList<String>(); // an array of phones associated with this contact

    public ArrayList<String> mPhoneHashes = new ArrayList<String>(); // an array of phone hashes

    public Contact(Friend f) {
        this.mAndroidContactId = f.mAndroidContactId;
        this.mIsOnHollerback = f.mIsOnHollerback;
        this.mName = f.mName;
        this.mPhoneLabel = f.mPhoneLabel;
        this.mPhotoID = f.mPhotoID;
        this.mUsername = f.mUsername;
        this.mLastContactTime = f.mLastContactTime;
        this.mPhones = new ArrayList<String>(f.mPhones);
        this.mPhoneHashes = new ArrayList<String>(this.mPhoneHashes);
        this.mFriend = f;

        if (mName == null) {
            mName = mUsername;
        }
    }

    public Contact(long contactId, String mName, String mPhone, String mPhoneLabel, int mPhotoID) {
        super();
        this.mAndroidContactId = contactId;
        this.mName = mName;
        this.mPhoneLabel = mPhoneLabel;
        this.mPhotoID = mPhotoID;

        mPhones.add(mPhone);

        // Log.d(TAG, "creating contact: " + toString());
    }

    public void save() {
        if (mFriend != null) {
            mFriend.mAndroidContactId = mAndroidContactId;
            mFriend.mName = mName;
            mFriend.mPhoneLabel = mPhoneLabel;
            mFriend.mPhotoID = mPhotoID;
            mFriend.mPhones = new ArrayList<String>(mPhones);
            mFriend.mPhoneHashes = new ArrayList<String>(mPhoneHashes);
            mFriend.mLastContactTime = mLastContactTime;
            mFriend.mIsOnHollerback = mIsOnHollerback;
            mFriend.mUsername = mUsername;
            mFriend.save();
        } else {
            mFriend = new Friend(this);
            mFriend.save();
        }

        Log.d(TAG, "saved: " + toString());
    }

    public static List<Contact> getContactsFor(List<Friend> friends) {
        if (friends == null) {
            return null;
        }

        List<Contact> contacts = new ArrayList<Contact>();
        for (Friend f : friends) {
            contacts.add(new Contact(f));
        }

        return contacts;

    }

    public void generateHash(MessageDigest md5) {

        for (String phone : mPhones) {
            mPhoneHashes.add(HashUtil.generateHexStringMD5(phone.getBytes()));
        }

    }

    @Override
    public String toString() {
        return "Contact [mName=" + mName + ", mPhoneLabel=" + mPhoneLabel + "]";
    }

    /**
     * Sort based on the name field
     */
    public static Comparator<Contact> COMPARATOR = new Comparator<Contact>() {

        @Override
        public int compare(Contact lhs, Contact rhs) {

            return lhs.mName.compareToIgnoreCase(rhs.mName);
        }
    };

}
