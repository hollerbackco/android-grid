package com.moziy.hollerback.model.web.response;

import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.web.ResponseObject;

public class LoginResponse implements ResponseObject{

	public String access_token;
	
	public UserModel user; //TODO - sajjad: Figure whether it's better to use the UserModel or to create an object representing the response
}
