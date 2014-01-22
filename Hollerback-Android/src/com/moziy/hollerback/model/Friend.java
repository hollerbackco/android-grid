package com.moziy.hollerback.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moziy.hollerback.database.ActiveRecordFields;

@Table(name = ActiveRecordFields.T_FRIENDS)
public class Friend extends BaseModel implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = Contact.class.getSimpleName();

    public long mAndroidContactId;

    @JsonProperty("name")
    @Column(name = ActiveRecordFields.C_FRIENDS_NAME)
    public String mName;

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONE_LABEL)
    public String mPhoneLabel;

    public int mPhotoID;

    @Column(name = ActiveRecordFields.C_FRIENDS_IS_ON_HOLLERBACK)
    public boolean mIsOnHollerback;

    @JsonProperty("username")
    @Column(name = ActiveRecordFields.C_FRIENDS_USERNAME)
    public String mUsername; // if an hb friend, the username

    @JsonProperty("last_sent_at")
    @Column(name = ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME)
    public String mLastContactTime; // the last time the user was contacted

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONES)
    public ArrayList<String> mPhones = new ArrayList<String>(); // an array of phones associated with this contact

    @Column(name = ActiveRecordFields.C_FRIENDS_PHONE_HASHES)
    public ArrayList<String> mPhoneHashes = new ArrayList<String>(); // an array of phone hashes

    public Friend() {
        super();
    }

    public Friend(Contact c) {
        super();
        this.mAndroidContactId = c.mAndroidContactId;
        this.mName = c.mName;
        this.mPhoneLabel = c.mPhoneLabel;
        this.mPhotoID = c.mPhotoID;
        this.mPhones = new ArrayList<String>(c.mPhones);
        this.mPhoneHashes = new ArrayList<String>(mPhoneHashes);
        this.mLastContactTime = c.mLastContactTime;
        this.mIsOnHollerback = c.mIsOnHollerback;
        this.mUsername = c.mUsername;

    }

}
