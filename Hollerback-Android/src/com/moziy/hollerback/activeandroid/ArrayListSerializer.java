package com.moziy.hollerback.activeandroid;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.activeandroid.serializer.TypeSerializer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.moziy.hollerback.util.QU;

public class ArrayListSerializer extends TypeSerializer {

    @Override
    public Class<?> getDeserializedType() {

        return ArrayList.class;
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
            Log.e(ArrayListSerializer.class.getSimpleName(), "FATAL: couldn't serialize");

        }
        return null;
    }

    @Override
    public Object deserialize(Object data) {
        try {
            return QU.getObjectMapper().readValue((String) data, new TypeReference<ArrayList<?>>() {
            });
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
        Log.e(ArrayListSerializer.class.getSimpleName(), "FATAL: couldn't deserialize");
        return null;
    }
}
