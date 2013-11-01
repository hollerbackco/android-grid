package com.moziy.hollerbacky.connection;

import java.io.IOException;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.util.QU;

public abstract class JacksonHttpResponseHandler<T extends ResponseObject> extends AsyncHttpResponseHandler {
	private static final String TAG = JacksonHttpResponseHandler.class.getSimpleName();
	
	private Class<T> mResponseClass;
	
	public JacksonHttpResponseHandler(Class<T> clazz){
		mResponseClass = clazz;
	}
	
	public abstract void onResponseSuccess(int statusCode, T response);
	
	public abstract void onApiFailure(Envelope.Metadata metaData);
	
	@Override
	public final void onSuccess(int statusCode, String content) {
		super.onSuccess(statusCode, content);
		
		ObjectMapper mapper = QU.getObjectMapper();
		try {
			
			//deserialize the response to the appropriate class
			T response = mapper.readValue(content, mResponseClass);
			
			//make sure that an envelope's status is ok before proceeding
			if(response instanceof Envelope<?>){ 
				if(((Envelope<?>)response).meta.code != 200){
					onApiFailure(((Envelope<?>) response).meta);
					return;
				}
			}
			
			//if all is good, then just return
			onResponseSuccess(statusCode, response);
			
			return;
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//ok there was some sort of issue, lets dig in
		
		try {
			//1. attempt to deserialize the content to an envelope, if it succeeds, then pass the meta to api failure
			Envelope<?> envelope = mapper.readValue(content, new TypeReference<Envelope<ResponseObject>>() {
			});
			
			onApiFailure(envelope.meta);
			
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
	
	@Override
	public void onFailure(Throwable e, String content) {
		super.onFailure(e, content);
		Log.w(TAG, e);
		
	}
	
	
	
}
