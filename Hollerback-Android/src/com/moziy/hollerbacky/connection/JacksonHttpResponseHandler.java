package com.moziy.hollerbacky.connection;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.util.QU;

public abstract class JacksonHttpResponseHandler<T extends ResponseObject> extends AsyncHttpResponseHandler {

	private Class<T> mResponseClass;
	
	public JacksonHttpResponseHandler(Class<T> clazz){
		mResponseClass = clazz;
	}
	
	public abstract void onResponseSuccess(int statusCode, T response);
	
	@Override
	public final void onSuccess(int statusCode, String content) {
		super.onSuccess(statusCode, content);
		
		ObjectMapper mapper = QU.getObjectMapper();
		try {
			onResponseSuccess(statusCode, mapper.readValue(content, mResponseClass));
		} catch (JsonParseException e) {
			onFailure(e, content);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			onFailure(e, content);
			e.printStackTrace();
		} catch (IOException e) {
			onFailure(e, content);
			e.printStackTrace();
		}
		
		
	}
	
	
	
}
