package com.moziy.hollerback.activeandroid;

import java.io.IOException;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.moziy.hollerback.util.QU;

public class StringArraySerializer extends TypeSerializer {

    @Override
    public Class<?> getDeserializedType() {
        return String[].class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Object serialize(Object data) {
        try {
            return QU.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            Log.e(StringArraySerializer.class.getSimpleName(), "FATAL: couldn't serialize type");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object deserialize(Object data) {
        try {
            return QU.getObjectMapper().readValue((String) data, String[].class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
