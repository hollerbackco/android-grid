package com.moziy.hollerback.model.web.response;

import java.util.ArrayList;

import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.PhoneInfo;
import com.moziy.hollerback.model.web.ResponseObject;

public class NewConvoResponse implements ResponseObject {

    public long id;

    public long unread_count;

    public ArrayList<UserModel> members; // TODO - sajjad: Re-evaluate this

    public ArrayList<PhoneInfo> invites;

    public ArrayList<VideoModel> videos; // TODO - sajjad: Re-evaluate this to see if "VideoModel" is the appropriate model to use here

}
