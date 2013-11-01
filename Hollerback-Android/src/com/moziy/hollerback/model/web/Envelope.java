package com.moziy.hollerback.model.web;

import java.util.ArrayList;

public class Envelope<T extends ResponseObject> implements ResponseObject{

	public Metadata meta;
	
	public T data;	//the correct response object will be resolved at runtime
	
	public T getData(){
		return data;
	}
	
	public static class Metadata{
		public long code;
		public String message;
		public ArrayList<String> errors;
	}
}
