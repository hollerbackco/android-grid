package com.moziy.hollerback.model.web;

public class Envelope {

	public Metadata meta;
	
	public ResponseObject data;	//the correct response object will be resolved at runtime
	
	public static class Metadata{
		public long code;
	}
}
