package com.moziy.hollerback.model;

import com.moziy.hollerback.database.ActiveRecordFields;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = ActiveRecordFields.T_USERS)
public class UserModel extends BaseModel {
	// From Phone
	
	@Column(name = ActiveRecordFields.C_USER_ID)
	public long userId;
	
	public long contactId;

	@Column(name = ActiveRecordFields.C_USER_NAME)
	public String name;
	
	@Column(name = ActiveRecordFields.C_USER_USERNAME)
	public String userName;
	
	@Column(name = ActiveRecordFields.C_USER_PHONE)
	public String phone;
	
	@Column(name = ActiveRecordFields.C_USER_PHONE_NORMALIZED)
	public String phoneNormalized;
	
	@Column(name = ActiveRecordFields.C_USER_IS_VERIFIED)	
	public boolean isVerified;

	@Column(name = ActiveRecordFields.C_USER_HOLLERBACK_USER)
	public boolean isHollerbackUser;

	// From Server
	public boolean isRecentUser;

	public String getName() {
		return name;
	}

}
