package com.moziy.hollerback.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moziy.hollerback.database.ActiveRecordFields;

@Table(name = ActiveRecordFields.T_USERS)
public class UserModel extends BaseModel {

    // Server Fields
    @Column(name = ActiveRecordFields.C_USER_ID)
    @JsonProperty("id")
    public long id = 0;

    @Column(name = ActiveRecordFields.C_USER_NAME)
    @JsonProperty("name")
    public String name;

    @Column(name = ActiveRecordFields.C_USER_USERNAME)
    @JsonProperty("username")
    public String username = "";

    @Column(name = ActiveRecordFields.C_USER_CREATED_AT)
    @JsonProperty("created_at")
    public String created_at; // TODO: Figure out the format

    @Column(name = ActiveRecordFields.C_USER_PHONE_HASHED)
    @JsonProperty("phone_hashed")
    public String phone_hashed;

    @Column(name = ActiveRecordFields.C_USER_ISNEW)
    @JsonProperty("is_new")
    public boolean is_new;

    @Column(name = ActiveRecordFields.C_USER_PHONE)
    @JsonProperty("phone")
    public String phone = "";

    @JsonProperty("phone_normalized")
    public String phone_normalized;

    @JsonProperty("is_verified")
    public boolean is_verified;

    @Deprecated
    public long contactId;

    @Deprecated
    public String device_token;

    @Deprecated
    @Column(name = ActiveRecordFields.C_USER_PHOTO)
    public String photourl;

    @Deprecated
    @Column(name = ActiveRecordFields.C_USER_IS_VERIFIED)
    public boolean isVerified;

    @Deprecated
    @Column(name = ActiveRecordFields.C_USER_HOLLERBACK_USER)
    public boolean isHollerbackUser;

    // From Server
    @Deprecated
    public boolean isRecentUser;

    public String getName() {
        return name;
    }

}
