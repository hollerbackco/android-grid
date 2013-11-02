package com.moziy.hollerback.model;

import com.moziy.hollerback.database.ActiveRecordFields;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = ActiveRecordFields.T_USERS)
public class UserModel extends BaseModel {
	
	// Server Fields
	@Column(name = ActiveRecordFields.C_USER_ID)
	public long id = 0;
	
	@Column(name = ActiveRecordFields.C_USER_NAME)
	public String name;
	
	@Column(name = ActiveRecordFields.C_USER_USERNAME)
	public String username = "";
	
	@Column(name = ActiveRecordFields.C_USER_CREATED_AT)
	public String created_at; //TODO: Figure out the format
	
	@Column(name = ActiveRecordFields.C_USER_PHONE_HASHED)
	public String phone_hashed;
	
	@Column(name = ActiveRecordFields.C_USER_ISNEW)
	public boolean is_new;
	
	@Column(name = ActiveRecordFields.C_USER_PHONE)
	public String phone = "";
	
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
