package com.moziy.hollerbacky.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.ResponseObject;
import com.moziy.hollerback.model.web.Envelope.Metadata;

/**
 * This class will deserialize the response synchronously 
 * @author sajjad
 * @param <T> The response object type
 */
public abstract class HBSyncHttpResponseHandler<T extends ResponseObject> extends HBHttpResponseHandler<T>{

private static final String TAG = HBSyncHttpResponseHandler.class.getSimpleName();
	
	
	public HBSyncHttpResponseHandler(TypeReference<T> typeReference){
		super(typeReference, true);
	}
	
	@Override
	public final void onSuccess(final int statusCode, final String content) {
		super.onSuccess(statusCode, content);
		
		
	
		//deserialize the response to the appropriate class
		T response = deserializeContent(content);
		
		if(response != null){
			
			//make sure that an envelope's status is ok before proceeding
			if(response instanceof Envelope<?>){ 
				if(((Envelope<?>)response).meta.code != 200){
					onApiFailure(((Envelope<?>) response).meta);
					return;
				}
			}
			
			//if all is good, then just return
			onResponseSuccess(statusCode, response);
		
		}else{
			//ok there was some sort of issue, lets dig in
			onFailure(null, content);
		}
	}
	
	@Override
	public final void onFailure(final Throwable e, final String content) {
		
		Metadata meta = extractMetaData(content);
		onApiFailure(meta);	//only if there's an api failure notify?
		
	}
	
}
