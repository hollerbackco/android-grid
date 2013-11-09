package com.moziy.hollerback.model.web.request;

import java.util.ArrayList;

import com.moziy.hollerback.model.web.RequestParams;

public class PostToConvoParams implements RequestParams {

    public String access_token;

    public ArrayList<String> urls; // presigned urls

    public ArrayList<String> part_urls; // bucket/key

    public String subtitle;

    public ArrayList<String> watched_ids;
}
