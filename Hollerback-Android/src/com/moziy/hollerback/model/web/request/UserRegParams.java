package com.moziy.hollerback.model.web.request;

import com.moziy.hollerback.model.web.RequestParams;


/**
 * This class is used to authenticate over the registration endpoint 
 * @author sajjad
 *
 */
public class UserRegParams implements RequestParams {

	public String email;
	
	public String username;
	
	public String password;
	
	public String phone;
}
