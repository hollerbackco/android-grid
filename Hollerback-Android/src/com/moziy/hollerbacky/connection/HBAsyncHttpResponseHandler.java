package com.moziy.hollerbacky.connection;

import java.io.IOException;

import org.apache.http.Header;

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
 */
public abstract class HBAsyncHttpResponseHandler<T extends ResponseObject> extends HBHttpResponseHandler<T> {
	private static final String TAG = HBAsyncHttpResponseHandler.class.getSimpleName();

	public HBAsyncHttpResponseHandler(TypeReference<T> typeReference){
		super(typeReference, false);
	}
	
	/**
	 * In order to use this in an intent service, the code below becomes necessary
	 */
	@Override
	public final void onSuccess(final int statusCode, final String content) {
		super.onSuccess(statusCode, content);
		
		  
		//deserialization to be run in a background thread, but api callback on the original thread
		//that made the request
		final Runnable processResponse = new Runnable() {
			
			@Override
			public void run() {
				
					
				//deserialize the response to the appropriate class
				final T response = deserializeContent(content);
				
				if(response != null){
					//make sure that an envelope's status is ok before proceeding
					if(response instanceof Envelope<?>){ 
						if(((Envelope<?>)response).meta.code != 200){
								postRunnable(new Runnable() {
									@Override
									public void run() {
										onApiFailure(((Envelope<?>) response).meta);
									}
								});
							return;
						}
					}
					
					//if all is good, then just return
					postRunnable(new Runnable() {
						
						@Override
						public void run() {
							onResponseSuccess(statusCode, response);
						}
					});
					
				}else{
					
					//ok there was some sort of issue, lets dig in
					final Metadata meta = extractMetaData(content);
					postRunnable(new Runnable(){	//post on the same thread that initiated the requests
	
						@Override
						public void run() {
							onApiFailure(meta);	//only if there's an api failure notify?
							
						}
						
					});
				}
				
			}
		};
		
		//deserialize on the background thread and then post to the original thread
		new Thread(){
			public void run() {
				processResponse.run();
			};
		}.start();
		
 
	}
	
	@Override
	public final void onFailure(final Throwable e, final String content) {
		
		new Thread(){
			public void run() {
				final Metadata meta = extractMetaData(content);
				postRunnable(new Runnable(){	//post on the same thread that initiated the requests

					@Override
					public void run() {
						onApiFailure(meta);	//only if there's an api failure notify?
						
					}
					
				});
				
			};
		}.start();
		
	}
	
	
	
}
