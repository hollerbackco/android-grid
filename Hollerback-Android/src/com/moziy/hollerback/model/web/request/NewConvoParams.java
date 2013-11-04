package com.moziy.hollerback.model.web.request;

import java.util.ArrayList;

import com.moziy.hollerback.model.web.PhoneInfo;
import com.moziy.hollerback.model.web.RequestParams;

public class NewConvoParams implements RequestParams {

	public String access_token;

	public ArrayList<PhoneInfo> invites;

	public ArrayList<String> part_urls;
}
