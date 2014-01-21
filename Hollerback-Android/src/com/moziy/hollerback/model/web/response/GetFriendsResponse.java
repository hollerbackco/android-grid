package com.moziy.hollerback.model.web.response;

import java.util.ArrayList;

import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.web.ResponseObject;

public class GetFriendsResponse implements ResponseObject {

    public ArrayList<Friend> recent_friends;
    public ArrayList<Friend> friends;

}
