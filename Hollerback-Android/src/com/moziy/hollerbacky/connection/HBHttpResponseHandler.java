package com.moziy.hollerbacky.connection;

import java.io.IOException;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.util.QU;

/**
 * 
 * @author sajjad
 * @param <T> The response object type
 * 
 * TODO - Sajjad: Ensure that json deserialization is happening on the background thread!
 */
public abstract class HBHttpResponseHandler<T extends ResponseObject> extends AsyncHttpResponseHandler {
	private static final String TAG = HBHttpResponseHandler.class.getSimpleName();
	
	private TypeReference<T> mTypeReference;
	
	public HBHttpResponseHandler(TypeReference<T> typeReference){
		mTypeReference = typeReference;
	}
	
	public HBHttpResponseHandler(TypeReference<T> typeReference, boolean useSynchronous){
		mTypeReference = typeReference;
		setUseSynchronousMode(useSynchronous);
	}
	
	public abstract void onResponseSuccess(int statusCode, T response);
	
	public abstract void onApiFailure(Envelope.Metadata metaData);
	
	@Override
	public final void onSuccess(int statusCode, String content) {
		super.onSuccess(statusCode, content);
		
		ObjectMapper mapper = QU.getObjectMapper();
		Throwable exception;
		try {
			
			//deserialize the response to the appropriate class
			T response = mapper.readValue(content, mTypeReference);
			
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
			exception = e;
			e.printStackTrace();
		} catch (JsonMappingException e) {
			exception = e;
			e.printStackTrace();
		} catch (IOException e) {
			exception = e;
			e.printStackTrace();
		}
		
		//ok there was some sort of issue, lets dig in
		
		onFailure(exception, content);
	}
	
	@Override
	public final void onFailure(Throwable e, String content) {
		
		//attempt to deserialize the content and pass int to onApiFailure
		Metadata meta = extractMetaData(content);
		onApiFailure(meta);	//only if there's an api failure notify?
		
		
	}
	
	private Metadata extractMetaData(String content){
		
		ObjectMapper mapper = QU.getObjectMapper();
		
		Envelope<?> envelope = null;
		try {
			
			envelope = mapper.readValue(content, new TypeReference<Envelope<ResponseObject>>() {});
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(envelope != null && envelope.meta != null){
			return envelope.meta;
			
		}
		
		return null;
		
	}
	
	
	
}
