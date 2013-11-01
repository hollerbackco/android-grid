package com.moziy.hollerback.model.web.response;

import com.moziy.hollerback.model.web.ResponseObject;

public class SyncResponse implements ResponseObject{
	
	public static interface Type{
		public static final String CONVERSATION = "conversation";
		public static final String MESSAGE = "message";
	}
	
	public String type;
	
	//TODO - Sajjad: Resolve object based on the type to the correct item
	public SyncPayload sync;
	
}
