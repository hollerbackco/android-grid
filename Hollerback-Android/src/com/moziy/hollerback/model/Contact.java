package com.moziy.hollerback.model;

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

    public Contact(String mName, String mPhone, String mPhoneLabel, int mPhotoID) {
        super();
        this.mName = mName;
        this.mPhone = mPhone;
        this.mPhoneLabel = mPhoneLabel;
        this.mPhotoID = mPhotoID;
        Log.d(TAG, "created contact: " + toString());
    }

    @Override
    public String toString() {
        return "Contact [mName=" + mName + ", mPhone=" + mPhone + ", mPhoneLabel=" + mPhoneLabel + "]";
    }

    /**
     * The contact that will be sent accross the wire
     * @author sajjad
     *
     */
    public static class NetworkContact {
        public String n;
        public String p;

    }

}
