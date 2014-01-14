package com.moziy.hollerback.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.util.security.HashUtil;

@Table(name = ActiveRecordFields.T_FRIENDS)
public class Contact extends BaseModel implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = Contact.class.getSimpleName();

    public long mAndroidContactId;

    @Column(name = ActiveRecordFields.C_FRIENDS_NAME)
    public String mName;

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONE_LABEL)
    public String mPhoneLabel;

    public int mPhotoID;

    @Column(name = ActiveRecordFields.C_FRIENDS_IS_ON_HOLLERBACK)
    public boolean mIsOnHollerback;

    @Column(name = ActiveRecordFields.C_FRIENDS_USERNAME)
    public String mUsername; // if an hb friend, the username

    @Column(name = ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME)
    public String mLastContactTime; // the last time the user was contacted

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONES)
    public ArrayList<String> mPhones = new ArrayList<String>(); // an array of phones associated with this contact

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONE_HASHES)
    public ArrayList<String> mPhoneHashes = new ArrayList<String>(); // an array of phone hashes

    public Contact() {
        super();
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
