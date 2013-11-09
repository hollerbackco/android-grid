package com.moziy.hollerback.model.web;

import java.util.ArrayList;

public class Envelope<T> implements ResponseObject {

    public Metadata meta;

    public T data; // the correct response object will be resolved at runtime

    public T getData() {
        return data;
    }

    public static class Metadata {
        public long code;
        public String last_sync_at; // sync time in
        public String message;
        public ArrayList<String> errors;
    }
}
