package com.moziy.hollerback.model;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;

import android.util.Log;

import com.moziy.hollerback.util.security.HashUtil;

public class Contact {
    private static final String TAG = Contact.class.getSimpleName();
    public final String mName;
    public final String mPhone;
    public byte[] mPhoneHashed;
    public String mPhoneHashHexString;
    public final String mPhoneLabel;
    public final int mPhotoID;
    public boolean mIsOnHollerback;
    public String mUsername; // if an hb friend, the username

    public ArrayList<String> mPhones = new ArrayList<String>(); // an array of phones associated with this contact
    public ArrayList<String> mPhoneHashes = new ArrayList<String>(); // an array of phone hashes

    public Contact(String mName, String mPhone, String mPhoneLabel, int mPhotoID) {
        super();
        this.mName = mName;
        this.mPhone = mPhone;
        this.mPhoneLabel = mPhoneLabel;
        this.mPhotoID = mPhotoID;

        mPhones.add(mPhone);

        Log.d(TAG, "creating contact: " + toString());
    }

    public void generateHash(MessageDigest md5) {

        for (String phone : mPhones) {
            mPhoneHashes.add(HashUtil.generateHexStringMD5(phone.getBytes()));
        }

    }

    @Override
    public String toString() {
        return "Contact [mName=" + mName + ", mPhone=" + mPhone + ", mPhoneLabel=" + mPhoneLabel + "]";
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
