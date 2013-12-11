package com.moziy.hollerback.model;

import java.security.MessageDigest;
import java.util.ArrayList;

import android.util.Log;

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
        Log.d(TAG, "created contact: " + toString());

        mPhones.add(mPhone);
    }

    public void generateHash(MessageDigest md5) {

        for (String phone : mPhones) {

            byte[] hash = md5.digest(phone.getBytes());
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }

            mPhoneHashes.add(hexString.toString());
        }

    }

    @Override
    public String toString() {
        return "Contact [mName=" + mName + ", mPhone=" + mPhone + ", mPhoneLabel=" + mPhoneLabel + "]";
    }

}
