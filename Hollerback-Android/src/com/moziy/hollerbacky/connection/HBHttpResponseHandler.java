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

public abstract class HBHttpResponseHandler<T extends ResponseObject> extends AsyncHttpResponseHandler {

    private static final String TAG = HBHttpResponseHandler.class.getSimpleName();

    protected TypeReference<T> mTypeReference;

    public abstract void onResponseSuccess(int statusCode, T response);

    public abstract void onApiFailure(Envelope.Metadata metaData);

    public HBHttpResponseHandler(TypeReference<T> typeReference, boolean isSynchronous) {
        mTypeReference = typeReference;
        setUseSynchronousMode(isSynchronous);
    }

    protected T deserializeContent(String content) {
        ObjectMapper mapper = QU.getObjectMapper();

        T response = null;

        // deserialize the response to the appropriate class
        try {
            long start = System.currentTimeMillis();
            response = mapper.readValue(content, mTypeReference);
            Log.d("performance", "deserialization time: " + (System.currentTimeMillis() - start));
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    protected Metadata extractMetaData(String content) {

        // don't even bother trying to deserialize, besides..it seems this version of jackson borks on null
        if (content == null) {
            return null;
        }

        ObjectMapper mapper = QU.getObjectMapper();

        Envelope<?> envelope = null;
        try {
            envelope = mapper.readValue(content, new TypeReference<Envelope<ResponseObject>>() {
            });

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (envelope != null && envelope.meta != null) {
            return envelope.meta;

        }

        return null;

    }
}
